#!/usr/bin/python3.8
from re import I
from scipy.sparse import construct
import sklearn  # type: ignore
import os
from functools import partial
from collections import defaultdict

import pickle
import glob
from sklearn.ensemble import RandomForestClassifier  # type: ignore
from data import InvocationKey, Context, DB, JSONData
from typing import Dict, Tuple, List
import json
import gc
import time
import xml.etree.ElementTree as ET

from multiprocessing import Pool

# For optimization
gc.disable()


def multiprocess(f, args, n_cpus):
    p = Pool(n_cpus)
    ret = p.map(f, args)
    p.close()
    return ret


class Model:
    null_classifier_wcallee: RandomForestClassifier
    null_classifier_wocallee: RandomForestClassifier
    classifiers: Dict[InvocationKey, RandomForestClassifier]
    labels: Dict[InvocationKey, List[str]]

    def __init__(self):
        self.classifiers = dict()
        self.labels = dict()

    def serialize(self, path):
        _time = time.time()
        file = open(path, 'wb')
        pickle.dump(self, file, protocol=5)
        print(f"{time.time() - _time} elapsed to serialize to {path}")

    @classmethod
    def deserialize(cls, path):
        _time = time.time()
        file = open(path, 'rb')
        ret = pickle.load(file)
        print(f"{time.time() - _time} elapsed to deserialize to {path}")
        return ret


def construct_training_data(db, is_data_for_null_classifier):
    # Data cleansing: exclude non-void SKIP models
    models = [h.model for h in db.handles if not (
        h.model.invocation_key.return_type != 'void' and h.model.null_value == 'NPEX_SKIP_VALUE')]

    if is_data_for_null_classifier:
        data = defaultdict(lambda: defaultdict(list))

        # Filter out models with non-object type values
        object_types = ['java.lang.String', 'java.lang.Object',
                        'java.util.Collection', 'java.lang.Class', 'OTHERS']
        models = [m for m in models if m.invocation_key.return_type in object_types]

        for m in models:
            category = 'wcallee' if m.invocation_key.callee_defined else 'wocallee'
            data[category]['X'].append(m.contexts)
            data[category]['Y'].append(0 if m.null_value == "null" else 1)
        return data

    else:
        data = defaultdict(lambda: defaultdict(list))

        # Filter out models with null value
        models = [m for m in models if m.null_value != 'null']

        for m in models:
            value, contexts = m.null_value, m.contexts
            data[m.invocation_key.abstract()][value].append(contexts)
        return data


def train_classifiers(db, model_output_dir, classifier_out_path, keys=set()):
    model = Model()
    args = []

    # Train non-null classifiers
    training_data = construct_training_data(
        db, is_data_for_null_classifier=False)
    if len(keys) > 0:
        training_data = {key: training_data[key]
                         for key in training_data.keys() if key in keys}

    for key, d in training_data.items():
        X, Y = [], []
        for value, contexts in d.items():
            for ctx in contexts:
                X.append(ctx)
                Y.append(value)

        args.append([key, X, Y, model_output_dir])

    # TODO: n_cpus from argument
    # results = multiprocess(train_classifier, args, n_cpus=40)
    results = [train_classifier(arg) for arg in args]

    for key, classifier, labels in results:
        model.classifiers[key], model.labels[key] = classifier, labels

    # Train null classifiers
    datasets = construct_training_data(db, is_data_for_null_classifier=True)
    cls_wcallee, cls_wocallee = RandomForestClassifier(), RandomForestClassifier()
    cls_wcallee.fit(datasets['wcallee']['X'], datasets['wcallee']['Y'])
    cls_wocallee.fit(datasets['wocallee']['X'], datasets['wocallee']['Y'])
    model.null_classifier_wcallee = cls_wcallee
    model.null_classifier_wocallee = cls_wocallee

    # Serialize classifiers
    model.serialize(classifier_out_path)


def train_classifier(arg):
    key, X, _Y, model_output_dir = arg[0], arg[1], arg[2], arg[3]
    labeldict = {l: i for (i, l) in enumerate(set(_Y))}
    Y = [labeldict[l] for l in _Y]

    clf = RandomForestClassifier()
    clf.fit(X, Y)

    return (key, clf, list(labeldict.keys()))


def model_keys_match_up_to_sub_camel_case(arg):
    model_keys, key = arg
    if key in model_keys:
        return key, [key]
    else:
        return key, [model_key for model_key in model_keys if key.matches_up_to_sub_camel_case(model_key)]


def generate_answer_sheet(project_dir, model_path, outpath):
    # model : AbstractKey -> classifier
    # invo_contexts : InvocationKey -> Contexts list
    # inputs: (entry, key_contexts, key, contexts, classifier) list

    model = Model.deserialize(model_path)
    invo_contexts = JSONData.read_json_from_file(
        f'{project_dir}/invo-ctx.npex.json')
    inputs = []
    answers = []
    for entry in invo_contexts:
        for key_contexts in entry['keycons']:
            key, contexts = InvocationKey.from_dict(
                key_contexts['key']), Context.to_feature_vector(key_contexts['contexts'])

            if key.abstract() in model.classifiers:
                inputs.append((entry, key_contexts, key, contexts,
                               model.classifiers[key.abstract()]))

    # For optimization, collect contexts to predict for each classifier
    # to_computes : classifier -> context list
    # outputs: classifier * context -> (model_value * prob) list
    to_computes = {}
    for (_, _, _, contexts, classifier) in inputs:
        if classifier not in to_computes:
            to_computes[classifier] = []
        to_computes[classifier].append(contexts)

    outputs = {}
    time_to_predict = 0.0
    for classifier, contexts_list in to_computes.items():
        _time = time.time()
        # optimization: predict contexts at once
        output = classifier.predict_proba(contexts_list)
        time_to_predict += time.time() - _time
        outputs[classifier] = {}
        for i in range(0, len(contexts_list)):
            outputs[classifier][str(contexts_list[i])] = output[i]

    # Final output: (site * pos * key * (value -> prob)) list
    for (entry, key_contexts, key, contexts, classifier) in inputs:
        # Non-null classifier prediction
        abs_src_path = entry['site']['source_path']
        rel_src_path = os.path.relpath(abs_src_path, start=project_dir)
        d_site = {
            'lineno': entry['site']['lineno'],
            'source_path': rel_src_path,
            'deref_field': entry['site']['deref_field']
        }

        d = {'site': d_site, 'null_pos': key.null_pos,
             'key': key_contexts['key']}

        # proba : Label -> float
        proba = {model.labels[key.abstract()][idx]: prob
                 for (idx, prob) in enumerate(outputs[classifier][str(contexts)])}

        d['proba'] = proba

        # Null classifier prediction
        clf = model.null_classifier_wcallee if key.callee_defined else model.null_classifier_wocallee
        proba = clf.predict_proba([contexts])[0]
        labeled_proba = {
            'null': proba[0],
            'nonnull': proba[1]
        }

        d['null_proba'] = labeled_proba

        answers.append(d)

    print(f"time to predict: {time_to_predict}")
    print(f"generate answer sheets for {len(inputs)} key contexts")

    with open(outpath, 'w') as f:
        f.write(json.dumps(answers, indent=4))

    return answers
