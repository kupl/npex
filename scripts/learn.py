#!/usr/bin/python3.8
import sklearn  #type: ignore
import os

import pickle
from sklearn.ensemble import RandomForestClassifier  #type: ignore
from data import InvocationKey, InvocationSite, Contexts
from typing import Dict, Tuple, List
import json
import data
from multiprocessing import Pool
import gc
import time

# For optimization
gc.disable()


def multiprocess(f, args, n_cpus):
    p = Pool(n_cpus)
    ret = p.map(f, args)
    p.close()
    return ret


class Model:
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


def construct_training_data(db):
    data = dict()
    for h in db.handles:
        if h.model.invocation_key:
            value, contexts = h.model.null_value, h.model.contexts
            d_key = data.setdefault(h.model.invocation_key, dict())
            d_key.setdefault(value, []).append(contexts)
    return data


def train_classifiers(db, model_output_dir, classifier_out_path):
    model = Model()
    args = []
    for key, d in construct_training_data(db).items():
        X, Y = [], []
        for value, contexts in d.items():
            for ctx in contexts:
                X.append(ctx.to_boolean_vector())
                Y.append(value)

        args.append([key, X, Y, model_output_dir])

    #TODO: n_cpus from argument
    results = multiprocess(train_classifier, args, n_cpus=30)

    for key, classifier, labels in results:
        model.classifiers[key], model.labels[key] = classifier, labels

    model.serialize(classifier_out_path)


def train_classifier(arg):
    key, X, _Y, model_output_dir = arg[0], arg[1], arg[2], arg[3]
    labeldict = {l: i for (i, l) in enumerate(set(_Y))}
    Y = [labeldict[l] for l in _Y]

    clf = RandomForestClassifier()
    clf.fit(X, Y)

    model_name = f'{key.method_name}_{key.actuals_length}_{key.null_pos}'

    # pickle learned classifer
    model_classifier = f'{model_output_dir}/{model_name}.classifier'
    model_file = open(model_classifier, 'wb')
    pickle.dump(clf, model_file, protocol=5)
    print(f'{model_name}: {clf.score(X, Y)}, # data: {len(Y)}')
    return (key, clf, list(labeldict.keys()))


def model_keys_match_up_to_sub_camel_case(arg):
    model_keys, key = arg
    if key in model_keys:
        return key, [key]
    else:
        return key, [model_key for model_key in model_keys if key.matches_up_to_sub_camel_case(model_key)]


def generate_answer_sheet(project_dir, model_path, outpath):
    print(f"Deserializing {model_path}...")
    model = Model.deserialize(model_path)
    invo_contexts = data.JSONData.read_json_from_file(f'{project_dir}/invo-ctx.npex.json')
    answers = []
    print(f"Done ...!")

    time_to_predict = 0.0
    time_to_find_alternative_models = 0.0

    # inputs: (entry, key_contexts, key, contexts) list
    # key_to_classifiers: key -> (model_key, classifier) list
    items = model.classifiers.items()
    model_keys = [model_key for (model_key, _) in items]
    invo_keys = set()
    inputs = []
    _time = time.time()
    for entry in invo_contexts:
        for key_contexts in entry['keycons']:
            key, contexts = InvocationKey.from_dict(key_contexts['key']), Contexts.from_dict(key_contexts['contexts'])
            classifiers = []
            inputs.append((entry, key_contexts, key, contexts))
            invo_keys.add(key)

    key_model_keys = multiprocess(model_keys_match_up_to_sub_camel_case, [(model_keys, key) for key in invo_keys],
                                  n_cpus=30)
    key_to_classifiers = {
        key: [(model.classifiers[model_key], model_key) for model_key in model_keys]
        for (key, model_keys) in key_model_keys
    }
    time_to_find_alternative_models = time.time() - _time

    # outputs: classifier * context -> (model_value * prob) list
    to_computes = {}
    for (_, _, key, contexts) in inputs:
        classifiers = key_to_classifiers[key]
        for (classifier, _) in classifiers:
            if classifier not in to_computes:
                to_computes[classifier] = []
            to_computes[classifier].append(contexts)

    outputs = {}
    for classifier, contexts_list in to_computes.items():
        _time = time.time()
        output = classifier.predict_proba([contexts.to_boolean_vector() for contexts in contexts_list])
        time_to_predict += time.time() - _time
        outputs[classifier] = {}
        for i in range(0, len(contexts_list)):
            outputs[classifier][contexts_list[i]] = output[i]

    # (site * pos * key * (value -> prob)) list
    for (entry, key_contexts, key, contexts) in inputs:
        abs_src_path = entry['site']['source_path']
        rel_src_path = os.path.relpath(abs_src_path, start=project_dir)
        d_site = {
            'lineno': entry['site']['lineno'],
            'source_path': rel_src_path,
            'deref_field': entry['site']['deref_field']
        }

        d = {'site': d_site, 'null_pos': key.null_pos, 'key': key_contexts['key']}

        probas = [{model.labels[model_key][idx]: prob
                   for (idx, prob) in enumerate(outputs[classifier][contexts])}
                  for (classifier, model_key) in key_to_classifiers[key]]

        num_of_matched = len(probas)
        labels = set()
        for proba in probas:
            labels |= set(proba.keys())

        ret = {}
        for label in labels:
            _sum = 0.0
            for proba in probas:
                _sum += proba[label] if label in proba else 0.0
            ret[label] = _sum / num_of_matched
        d['proba'] = ret

        keys = list(d['proba'].keys())
        for key in keys:
            if d['proba'][key] == 0.0:
                del d['proba'][key]

        answers.append(d)

    print(f"time to predict: {time_to_predict}")
    print(f"time to find alternative-models: {time_to_find_alternative_models}")
    print(f"generate answer sheets for {len(inputs)} key contexts")

    with open(outpath, 'w') as f:
        f.write(json.dumps(answers, indent=4))
