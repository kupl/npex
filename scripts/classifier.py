#!/usr/bin/python3.8

import csv
import os
import graphviz
from sklearn import tree
from sklearn.tree import export_graphviz
import joblib
from null_handle import Contexts

CLASSIFIER_PATH = '.npex.classifier'

def load_data(csvfile):
  with open(csvfile, newline='') as f:
    reader = csv.DictReader(f)
    return [r for r in reader]


def row2vec(row):
  label = 0 if row['null_value'] == '""' else 1
  features = [1 if (row[fld]) == 'True' else 0 for fld in Contexts.get_feature_names()]
  return (label, features)

#TODO: Model should be learend separately by each methods? (current: care about toString only)
def prepare_data(rows):
  X, Y = [], []
  for (id, row) in enumerate(rows):
    if row['method_name'] != "toString" or not row['null_value'] in ['""', 'null'] : continue
    label, vector = row2vec(row)
    X.append(vector)
    Y.append(label)

  return (X, Y)

def train(data_csv, classifier_path):
  rows = load_data(data_csv)
  (X,Y) = prepare_data(rows)
  clf = tree.DecisionTreeClassifier()
  clf.fit(X, Y)

  out_file = 'tree.dot'
  export_graphviz(clf, out_file, feature_names=Contexts.get_feature_names(),  impurity=False)
  joblib.dump(clf, classifier_path)

  pdf = os.path.basename(out_file).split('.')[0] + '.pdf'
  os.system(f"dot -Tpdf {out_file} > {pdf}")
  return clf.score(X, Y), out_file, classifier_path

def see_conflict(data, classifier_path):
  classifier : tree.DecisionTreeClassifier = joblib.load(classifier_path)
  for (id, row) in enumerate(load_data(data)):
    if row['method_name'] != "toString" or not row['null_value'] in ['""', 'null'] : continue
    (label, vector) = row2vec(row)
    print(f'id: {id}, line:{row["line_no"]}, sink_body: {row["sink_body"]}, label: {label}:, predicted: {classifier.predict([vector])}')

