import sklearn
import os
import joblib
from sklearn import preprocessing, tree
from sklearn.tree import export_graphviz
from sklearn.tree import DecisionTreeClassifier
from data import InvocationKey, InvocationSite, Contexts
from typing import Dict, Tuple, List
import json
import data

import pprint

class Model:
  classifiers : Dict[InvocationKey, DecisionTreeClassifier]
  labels: Dict[InvocationKey, List[str]]

  def __init__(self):
    self.classifiers = dict()
    self.labels = dict()

  def serialize(self, path):
    joblib.dump(self, path)

  @classmethod
  def deserialize(cls, path):
    return joblib.load(path)

def construct_training_data(db):
  data = dict()
  for h in db.handles:
    if (key := h.model.invocation_key):
      value, contexts = h.model.null_value, h.model.contexts
      d_key = data.setdefault(key, dict())
      d_key.setdefault(value, []).append(contexts)
  return data


def train_classifiers(db, model_output_dir, classifier_out_path) -> Dict[InvocationKey, DecisionTreeClassifier]:
  model = Model()
  for key, d in construct_training_data(db).items():
    X, Y = [], []
    for value, contexts in d.items():
      for ctx in contexts:
        X.append(ctx.to_boolean_vector())
        Y.append(value)

    model.classifiers[key], model.labels[key] = train_classifier(key, X, Y, model_output_dir)

  model.serialize(classifer_out_path)


def train_classifier(key, X, Y, model_output_dir, visualize=False) -> Tuple[DecisionTreeClassifier, Dict[str, int]]:
  labeldict = { l : i for (i, l) in enumerate(set(Y)) }

  clf = tree.DecisionTreeClassifier()
  clf.fit(X, (Y:= [labeldict[l] for l in Y]))

  model_name = f'{key.method_name}_{key.actuals_length}_{key.null_pos}'

  # visualize learned classifier to pdf
  if visualize:
    model_dot = f'{model_output_dir}/{model_name}.dot'
    export_graphviz(clf, out_file=model_dot, feature_names=list(data.Contexts.__annotations__.keys()), impurity=False)
    os.system(f'dot -Tpdf {model_dot} > {model_output_dir}/{model_name}.pdf')

  # pickle learned classifer
  model_classifier = f'{model_output_dir}/{model_name}.classifier'
  joblib.dump(clf, model_classifier)
  print(f'{model_name}: {clf.score(X, Y)}, # data: {len(Y)}')
  return (clf, list(labeldict.keys()))


def generate_answer_sheet(project_dir, model_path, outpath) -> Dict[Tuple[InvocationSite, int], Dict[str, float]] :
  model = Model.deserialize(model_path)
  invo_contexts = data.JSONData.read_json_from_file(f'{project_dir}/invo-ctx.npex.json')
  answers = []

  for entry in invo_contexts:
    for key_contexts in entry['keycons']:
      key, contexts = InvocationKey.from_dict(key_contexts['key']), Contexts.from_dict(key_contexts['contexts'])

      #make source_path relative
      abs_src_path = entry['site']['source_path']
      rel_src_path = os.path.relpath(abs_src_path, start=project_dir)
      d_site = {'lineno': entry['site']['lineno'], 'source_path':rel_src_path, 'deref_field': entry['site']['deref_field']}
      
      d = {'site': d_site, 'null_pos': key.null_pos, 'key': key_contexts['key']}
      if key not in model.classifiers:
        d['proba'] = {}
      else:
        classifier = model.classifiers[key]
        proba = model.classifiers[key].predict_proba([contexts.to_boolean_vector()])
        d['proba'] = {model.labels[key][idx]: prob for (idx, prob) in enumerate(proba[0])}
      answers.append(d)

  with open(outpath, 'w') as f:
    f.write(json.dumps(answers, indent=4))
    