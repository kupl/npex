import csv
from dataclasses import dataclass, asdict
from typing import Any, Dict, List, Optional, Tuple
from dacite import from_dict as _from_dict
import dataclasses
import pprint
import pickle
import os
import json
from re import finditer

from dacite.exceptions import MissingValueError


@dataclass(frozen=True)
class JSONData:
    @classmethod
    def from_dict(klass, d):
        return d if (d == None or d == []) else _from_dict(data_class=klass,
                                                           data=d)

    @staticmethod
    def read_json_from_file(json_filename: str):
        with open(json_filename, "r") as f:
            return json.load(f)

    def asdict(self):
        return dataclasses.asdict(self)


@dataclass(frozen=True)
class MethodSignature(JSONData):
    method_name: str
    return_type: str
    null_pos: int


@dataclass(frozen=True)
class InvocationKey(JSONData):
    method_name: str
    null_pos: int
    actuals_length: int
    return_type: str
    invo_kind: str
    callee_defined: bool
    method_signature: MethodSignature

    def abstract(self):
        null_pos_is_base = self.null_pos == -1
        return AbstractKey(null_pos_is_base, self.return_type, self.invo_kind, self.callee_defined)

# A classifier identifes abstract keys only


class Context:
    ordered_context_names = ["StringInMethodName", "LHSIsArray", "CalleeMethodUsedAsBase", "NameInMethodName", "setInMethodName", "addInMethodName", "ClassInMethodName", "<init>InMethodName", "getInMethodName", "CalleeMethodReturnsNew", "equalsInMethodName", "CalleeMethodReturnsLiteral", "hashInMethodName", "ValueInMethodName", "toInMethodName",
                             "CalleeMethodReturnsVoid", "CalleeMethodThrows", "CallerMethodIsPrivate", "LHSIsField", "stopInMethodName", "CalleeMethodReturnsField", "EmptyInMethodName", "LHSIsPublic", "isInMethodName", "writeInMethodName", "cloneInMethodName", "CodeInMethodName", "closeInMethodName", "CalleeMethodChecksNull", "containsInMethodName", "removeInMethodName"]

    @classmethod
    def to_feature_vector(cls, context_map):
        return [1 if context_map[k] else 0 for k in cls.ordered_context_names if k in context_map.keys()]


@dataclass(frozen=True)
class AbstractKey(JSONData):
    nullpos_is_base: bool  # is null_pos base?
    return_type: str
    invo_kind: str
    callee_defined: bool

@dataclass(frozen=True)
class NullModel(JSONData):
    invocation_key: Optional[InvocationKey]
    null_value: Optional[str]
    null_value_kind: str
    raw: Optional[str]
    raw_type: Optional[str]
    sink_body: Optional[str]
    contexts: List[int]

    @classmethod
    def from_dict2(klass, d):
        nvd = d['null_value']
        exprs = nvd['exprs']
        if nvd['kind'] == 'BINARY':
            opkind = exprs[0]
            # sorting the exprs here is for normalization purpose: e.g.) EQ, x, y == EQ, y, x
            null_value = ', '.join([opkind] + sorted(exprs[1:]))
        else:
            null_value = exprs[0]

        d['null_value'] = null_value
        d['null_value_kind'] = nvd['kind']
        d['raw'] = nvd['raw']
        d['raw_type'] = nvd['raw_type']
        return klass.from_dict(d)

    @classmethod
    def from_dict(klass, d):
        invocation_key = InvocationKey.from_dict(d['invocation_key'])
        return NullModel(invocation_key, d['null_value'], d['null_value_kind'], d['raw'], d['raw_type'], d['sink_body'], Context.to_feature_vector(d['contexts']))


@dataclass(frozen=True)
class NullHandle(JSONData):
    source_path: str
    lineno: int
    handle: str
    model: NullModel

    def __str__(self):
        return json.dumps(self.asdict(), indent=4)


class DB:
    handles: List[NullHandle]

    @classmethod
    def create_from_handles(cls, handles_json):
        handles = []
        for h in JSONData.read_json_from_file(handles_json):
            for m in h['models']:
                try:
                    model = NullModel.from_dict2(m)
                except MissingValueError as e:
                    continue

                handle = NullHandle(h['source_path'], h['lineno'], h['handle'],
                                    model)
                handles.append(handle)
        return DB(handles)

    def __init__(self, handles):
        self.handles = handles

    def __add__(self, db):
        return DB(self.handles + db.handles)

    def get_all_keys(self):
        return set([h.model.invocation_key for h in self.handles])

    def to_json(self):
        return json.dumps([asdict(h) for h in self.handles], indent=4)

    def serialize(self, path, json=False):
        if json:
            with open(path, 'w') as f:
                f.write(self.to_json())
        else:
            with open(path, 'wb') as f:
                pickle.dump(self, f)

    @classmethod
    def deserialize(cls, path):
        try:
            with open(path, 'rb') as f:
                return pickle.load(f)
        except AttributeError:
            print(f'{path}: Could not deserialize DB, check its version!')
            os.remove(path)
            return DB(handles=[])
