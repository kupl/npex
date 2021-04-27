import sklearn
import os
import joblib
from sklearn.tree import export_graphviz
from sklearn import preprocessing, tree
from data import InvocationKey
from typing import Dict
import data

def construct_training_data(db):
  data = dict()
  for h in db.handles:
    if (key := h.model.invocation_key):
      value, contexts = h.model.null_value, h.model.contexts
      d_key = data.setdefault(key, dict())
      d_key.setdefault(value, []).append(contexts)
  return data


def train_classifiers(db, model_output_dir) -> Dict[InvocationKey, sklearn.tree.DecisionTreeClassifier]:
  for key, d in construct_training_data(db).items():
    X, Y = [], []
    for value, contexts in d.items():
      for ctx in contexts:
        X.append(ctx.to_boolean_vector())
        Y.append(value)

    tree = train_classifier(key, X, Y, model_output_dir)

def train_classifier(key, X, Y, model_output_dir) -> sklearn.tree:
  labeldict = { l : i for (i, l) in enumerate(Y) }

  clf = tree.DecisionTreeClassifier()
  clf.fit(X, (Y:= [labeldict[l] for l in Y]))


  model_name = f'{key.method_name}_{key.actuals_length}_{key.null_pos}'

  # visualize learned classifier to pdf
  model_dot = f'{model_output_dir}/{model_name}.dot'
  export_graphviz(clf, out_file=model_dot, feature_names=list(data.Contexts.__annotations__.keys()), impurity=False)
  os.system(f'dot -Tpdf {model_dot} > {model_output_dir}/{model_name}.pdf')

  # pickle learned classifer
  model_classifier = f'{model_output_dir}/{model_name}.classifier'
  joblib.dump(clf, model_classifier)
  print(f'{model_name}: {clf.score(X, Y)}, # data: {len(Y)}')