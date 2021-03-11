import dataclasses
import glob
import csv
import os
import pickle
import json
import typing
from dacite import from_dict as _from_dict
from dataclasses import dataclass
from typing import List, Optional, Set, get_type_hints
from collections import defaultdict
import pprint

from sklearn import preprocessing, tree
from sklearn import datasets
from sklearn.tree import export_graphviz
import joblib

from slacker import Slacker
import typing_extensions


class JSONData:
    _dependent_classes: List[typing.Any] = []

    @classmethod
    def from_dict(klass, d):
        return d if (d == None or d == []) else _from_dict(data_class=klass, data=d)

    @staticmethod
    def read_json_from_file(json_filename: str):
        with open(json_filename, "r") as f:
            return json.load(f)

    @classmethod
    def get_primitive_fields(cls):
        return [
            k for (k, v) in cls.__annotations__.items()
            if v in [str, int, bool, List[str], Optional[str], Optional[int]]
        ]

    @classmethod
    def _flatten_attributes(cls):
        primitives = cls.get_primitive_fields()
        return sum([klass._flatten_attributes() for klass in cls._dependent_classes], primitives)

    def flatten(self):
        d = dict()
        for f in self.get_primitive_fields():
            d[f] = vars(self)[f]

        for (k, v) in self.__dict__.items():
            if isinstance(v, JSONData):
                d.update(v.flatten())
        return d


@dataclass
class Contexts(JSONData):
    UsedAsReturnExpression: bool
    UsedAsArgument: bool
    UsedAsOperand: bool
    NullCheckingExists: bool
    IsField: bool
    IsParameter: bool
    IsVariable: bool
    LHSIsField: bool
    LHSIsPrivate: bool
    LHSIsPublic: bool
    SinkExprIsAssigned: bool
    SinkExprIsExceptionArgument: bool
    SinkMethodIsConstructor: bool
    SinkMethodIsPrivate: bool
    SinkMethodIsPublic: bool
    SinkMethodIsStatic: bool
    VariableIsObjectType: bool
    VariableIsFinal: bool

    def astuple(self):
        return tuple(self.__dict__.values())


@dataclass
class InvoSignature(JSONData):
    method_name: str
    null_idx: int
    return_type: Optional[str]
    actual_return_type: Optional[str]
    arguments_types: List[str]
    invo_kind: str

    def askey(self):
        return (self.method_name, self.null_idx, self.return_type, len(self.arguments_types), self.invo_kind)


@dataclass
class NullModel(JSONData):
    sink_body: str
    null_value: Optional[str]
    invocation_info: Optional[InvoSignature]
    contexts: Optional[Contexts]
    _dependent_classes = [InvoSignature, Contexts]


@dataclass
class NullHandle(JSONData):
    project: str
    source_location: str
    line_no: int
    handle: str
    models: List[NullModel]
    _dependent_classes = [NullModel]

    @classmethod
    def from_results_json(cls, results_json_file):
        handles = JSONData.read_json_from_file(results_json_file)
        return [cls.from_dict({'project': os.path.basename(results_json_file).split('.')[0], **h}) for h in handles]


@dataclass
class Row(JSONData):
    project: str
    source_location: str
    line_no: int
    handle: str
    model: Optional[NullModel]
    _dependent_classes = [NullModel]

    @classmethod
    def from_null_handle(cls, handle):
        h_wo_models = {k: v for (k, v) in handle.__dict__.items() if k != 'models'}
        return [cls.from_dict({'model': m, **h_wo_models}) for m in handle.models]

    @classmethod
    def get_headers(cls):
        return cls._flatten_attributes()


class RawDB:
    handles: List[NullHandle]
    rows: List[Row]

    @classmethod
    def from_result_json(cls, handle_json_file):
        return RawDB(handles=NullHandle.from_results_json(handle_json_file))

    def __init__(self, handles=[], rows=[]):
        self.handles = handles
        self.rows = rows if rows != [] else sum([Row.from_null_handle(h) for h in handles], [])

    def __add__(self, db2):
        return RawDB(self.handles + db2.handles, self.rows + db2.rows)

    def split_db_by_invo_signature(self):
        d = defaultdict(list)

        row_with_invocation_info = [r for r in self.rows if r.model and r.model.invocation_info]
        for r in row_with_invocation_info:
            d[r.model.invocation_info.askey()].append(r.model)

        dbs = [LearningDB(invocation_info, rows) for (invocation_info, rows) in d.items()]
        dbs = [d for d in dbs if d.num_of_labels > 1]
        return dbs

    def print_statistics(self):
        print(f"# total handles: {len(self.handles)}")
        print(f"# total handles with at least one model: {len([h for h in self.handles if h.models != []])}")
        print(f"# total rows: {len(self.rows)}")

    def writeToCSV(self, filepath):
        with open(filepath, 'w', newline='') as f:
            writer = csv.DictWriter(f, fieldnames=Row.get_headers(), extrasaction='ignore')
            writer.writeheader()
            for row in self.rows:
                writer.writerow(row.flatten())

    def serialize(self, path):
        with open(path, 'wb') as file:
            pickle.dump(self, file)

    @classmethod
    def deserialize(cls, path):
        with open(path, 'rb') as f:
            return pickle.load(f)


class LearningDB:
    def __init__(self, key, rows):
        self.key = key
        self.datasize = len(rows)
        self.d = defaultdict(list)
        for r in rows:
            assert (r.invocation_info.askey() == key)
            self.d[r.null_value].append(r.contexts)
        self.num_of_labels = len(self.d.keys())

        self.labeldict = dict()
        for (idx, label) in enumerate(self.d.keys()):
            self.labeldict[label] = idx

    def stat(self):
        print(f'Key: {self.key}')
        print(f'# of data: {self.datasize}')
        print(f'# of labels (null-values): {self.num_of_labels}')

    def train(self, model_dir, model_name):
        X, Y = [], []

        for (null_value, contexts) in self.d.items():
            for ctx in contexts:
                feature_vector = [1 if b else 0 for b in ctx.astuple()]
                X.append(feature_vector)
                Y.append(self.labeldict[null_value])

        clf = tree.DecisionTreeClassifier()
        clf.fit(X, Y)

        model_dot = f'{model_dir}/{model_name}.dot'
        model_classifier = f'{model_dir}/{model_name}.classifier'
        export_graphviz(clf, out_file=model_dot, feature_names=list(Contexts.__annotations__.keys()), impurity=False)
        joblib.dump(clf, model_classifier)

        model_pdf = f"{model_dir}/{model_name}.pdf"
        os.system(f"dot -Tpdf {model_dot} > {model_pdf}")
