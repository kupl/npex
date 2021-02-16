#!/usr/bin/python3.8

import csv
import graphviz
from sklearn import tree
from sklearn.tree import export_graphviz


from null_handle import Contexts

def row2vec(row):
  label = 0 if row['null_value'] == '""' else 1
  features = [1 if (row[fld]) == 'True' else 0 for fld in Contexts.get_feature_names()]
  return (label, features)

def prepare_data(rows):
  X, Y = [], []
  for row in rows:
    if row['method_name'] != "toString" or not row['null_value'] in ['""', 'null'] : continue
    label, vector = row2vec(row)
    X.append(vector)
    Y.append(label)

  return (X, Y)

def train(csvfile):
  with open(csvfile, newline='') as f:
    reader = csv.DictReader(f)
    (X, Y) = prepare_data([row for row in reader])
    print(X, Y)

  clf = tree.DecisionTreeClassifier()
  clf.fit(X, Y)

  out_file = 'tree.dot'
  export_graphviz(clf, out_file, feature_names=Contexts.get_feature_names(),  impurity=False)
  return clf.score(X, Y), out_file