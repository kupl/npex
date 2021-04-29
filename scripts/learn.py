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

def construct_training_data(db):
  data = dict()
  for h in db.handles:
    if (key := h.model.invocation_key):
      value, contexts = h.model.null_value, h.model.contexts
      d_key = data.setdefault(key, dict())
      d_key.setdefault(value, []).append(contexts)
  return data


def train_classifiers(db, model_output_dir) -> Dict[InvocationKey, DecisionTreeClassifier]:
  model = Model()
  for key, d in construct_training_data(db).items():
    X, Y = [], []
    for value, contexts in d.items():
      for ctx in contexts:
        X.append(ctx.to_boolean_vector())
        Y.append(value)

    model.classifiers[key], model.labels[key] = train_classifier(key, X, Y, model_output_dir)

  joblib.dump(model, f'{model_output_dir}/classifiers')


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


def generate_answer_sheet(project_dir, model) -> Dict[Tuple[InvocationSite, int], Dict[str, float]] :
  model = joblib.load(model)
  invo_contexts = data.JSONData.read_json_from_file(f'{project_dir}/invo-ctx.npex.json')
  answers = []

  for entry in invo_contexts:
    for key_contexts in entry['keycons']:
      key, contexts = InvocationKey.from_dict(key_contexts['key']), Contexts.from_dict(key_contexts['contexts'])
      d = {'site': entry['site'], 'null_pos': key.null_pos, 'key': key_contexts['key']}
      if key not in model.classifiers:
        d['proba'] = {}
      else:
        classifier = model.classifiers[key]
        proba = model.classifiers[key].predict_proba([contexts.to_boolean_vector()])
        d['proba'] = {model.labels[key][idx]: prob for (idx, prob) in enumerate(proba[0])}
      answers.append(d)

  with open('sheet', 'w') as f:
    pprint.PrettyPrinter(indent=4, stream=f).pprint(answers)

  with open('sheet.json', 'w') as f:
    f.write(json.dumps(answers, indent=4))
    