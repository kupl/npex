#!/usr/bin/python3.8
from re import I
import sklearn  #type: ignore
import os
from functools import partial

import pickle
import glob
from sklearn.ensemble import RandomForestClassifier  #type: ignore
from data import InvocationKey, DB, JSONData
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
            d_key = data.setdefault(h.model.invocation_key.abstract(), dict())
            d_key.setdefault(value, []).append(contexts)
    return data


def train_classifiers(db, model_output_dir, classifier_out_path, keys=set()):
    model = Model()
    args = []
    training_data = construct_training_data(db)
    if len(keys) > 0:
        old_length = len(training_data)
        training_data = { key: training_data[key] for key in training_data.keys() if key in keys }

    for key, d in training_data.items():
        X, Y = [], []
        for value, contexts in d.items():
            for ctx in contexts:
                X.append(ctx)
                Y.append(value)

        args.append([key, X, Y, model_output_dir])

    #TODO: n_cpus from argument
    # results = multiprocess(train_classifier, args, n_cpus=40)
    results = [train_classifier(arg) for arg in args]

    for key, classifier, labels in results:
        model.classifiers[key], model.labels[key] = classifier, labels

    model.serialize(classifier_out_path)


def train_classifier(arg):
    key, X, _Y, model_output_dir = arg[0], arg[1], arg[2], arg[3]
    labeldict = {l: i for (i, l) in enumerate(set(_Y))}
    Y = [labeldict[l] for l in _Y]

    clf = RandomForestClassifier()
    clf.fit(X, Y)

    # model_name = f'{key.method_name}_{key.actuals_length}_{key.null_pos}'

    # pickle learned classifer
    # if model_output_dir != None:
    #     model_classifier = f'{model_output_dir}/{model_name}.classifier'
    #     model_file = open(model_classifier, 'wb')
    #     pickle.dump(clf, model_file, protocol=5)
    # print(f'{model_name}: {clf.score(X, Y)}, # data: {len(Y)}')
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
    invo_contexts = JSONData.read_json_from_file(f'{project_dir}/invo-ctx.npex.json')
    inputs = []
    answers = []
    print(model.classifiers.keys())
    for entry in invo_contexts:
        for key_contexts in entry['keycons']:
            key, contexts = InvocationKey.from_dict(key_contexts['key']), [ 1 if v else 0 for v in key_contexts['contexts'].values()]
           
            if key.abstract() in model.classifiers:
                inputs.append((entry, key_contexts, key, contexts, model.classifiers[key.abstract()]))

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
        abs_src_path = entry['site']['source_path']
        rel_src_path = os.path.relpath(abs_src_path, start=project_dir)
        d_site = {
            'lineno': entry['site']['lineno'],
            'source_path': rel_src_path,
            'deref_field': entry['site']['deref_field']
        }

        d = {'site': d_site, 'null_pos': key.null_pos, 'key': key_contexts['key']}

        # proba : Label -> float
        proba = {model.labels[key.abstract()][idx]: prob
                   for (idx, prob) in enumerate(outputs[classifier][str(contexts)])}
                 
        d['proba'] = proba
        answers.append(d)

    print(f"time to predict: {time_to_predict}")
    print(f"generate answer sheets for {len(inputs)} key contexts")

    with open(outpath, 'w') as f:
        f.write(json.dumps(answers, indent=4))


################## Target-project-aware DB construction ###################
def extract_artifact_name(dir):
  if os.path.exists(pom := f'{dir}/pom.xml'):
    tree = ET.parse(pom)
    group = tree.findtext('{*}groupId') # asterisk here resolves namespace issues
    id = tree.findtext('{*}artifactId')
    return f'{group}.{id}' if group != None else id
  elif os.path.exists(artifact_name_file:= f'{dir}/.artifact-id'):
    with open(artifact_name_file, 'r') as f:
      id = (f.readlines()[0]).strip()
      return id

def get_project_root(dir):
    if os.path.exists(f'{dir}/pom.xml'):
        return dir
    elif os.path.exists(f'{dir}/.artifact-id'):
        return f'{dir}/source'
    else:
        assert(False)



# learning DB for target project t = U - {d | d \in U whose artifact_id = T's one } + t,
# where U = directories in learning_benchmarks_dir whose null handles are extracted
def construct_learning_database_for_target_project(entire_benchmarks_directories, target_project_dir, db_outfile=None):
    print(f'Constructing learning DB for {target_project_dir}')
    if not os.path.exists(f'{get_project_root(target_project_dir)}/handles.npex.json'):
        print(f'Could not find handle for {target_project_dir}')
        return None

    db_outfile = f'{get_project_root(target_project_dir)}/db.npex' if db_outfile  == None else db_outfile
    U = set([dir for dir in entire_benchmarks_directories if os.path.exists(f'{dir}/handles.npex.json')])
    artifact_id = extract_artifact_name(target_project_dir)
    same_project = set([d for d in U if extract_artifact_name(d) == artifact_id])
    same_project_removed = list(U - same_project)
    handles_existing_projects = [d for d in same_project_removed if os.path.exists(f'{get_project_root(target_project_dir)}/handles.npex.json')]
    handles_existing_projects.append(target_project_dir)
    db = DB(handles=[])
    for dir in handles_existing_projects:
        db += DB.create_from_handles(f'{get_project_root(dir)}/handles.npex.json')
    db.serialize(db_outfile)
    db.serialize(f'{db_outfile}.json', json=True)

def construct_learning_database_for_entire_evaluation_set(crawled_benchmarks_dir, evaluation_benchmarks_dir, ncpus):
    evaluation_benchmarks = [dir for dir in glob.glob(f'{evaluation_benchmarks_dir}/*') if os.path.exists(f'{get_project_root(dir)}/handles.npex.json')]
    entire_benchmarks_directories = []
    entire_benchmarks_directories.extend(glob.glob(f'{crawled_benchmarks_dir}/*'))
    entire_benchmarks_directories.extend(glob.glob(f'{evaluation_benchmarks_dir}/*'))
    print(evaluation_benchmarks)
    with Pool(processes=ncpus) as pool:
        job = partial(construct_learning_database_for_target_project, entire_benchmarks_directories)
        pool.map(job, evaluation_benchmarks)

def train_all(evaluation_benchmarks_dir, models_output_dir):
    for bench in glob.glob(f'{evaluation_benchmarks_dir}/*'):
        bug_id = os.path.basename(bench)
        db, model_dir = DB.deserialize(f'{bench}/db.npex'), f'{models_output_dir}/{bug_id}'
        os.makedirs(model_dir)
        classifier_path = f'{models_output_dir}/{bug_id}.classifier'
        print(f'Learning model for {bug_id}')
        train_classifiers(db, model_dir, classifier_path)

    
