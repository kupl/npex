import csv
from dataclasses import dataclass, asdict
from typing import Any, Dict, List, Optional, Tuple
from dacite import from_dict as _from_dict
import dataclasses
import pprint
import pickle
import json
from re import finditer

from dacite.exceptions import MissingValueError


def camel_case_split(identifier):
    matches = finditer('.+?(?:(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|$)',
                       identifier)
    return [m.group(0) for m in matches]


@dataclass
class JSONData:
    @classmethod
    def from_dict(klass, d):
        return d if (d == None or d == []) else _from_dict(data_class=klass,
                                                           data=d)

    @staticmethod
    def read_json_from_file(json_filename: str):
        with open(json_filename, "r") as f:
            return json.load(f)

    @classmethod
    def __get_primitive_fields(cls):
        fields = set()
        for (k, v) in cls.__annotations__:
            if type(v) in ['int', 'str']:
                fields.add(k)
        return fields

    def asdict(self):
        return dataclasses.asdict(self)

@dataclass(frozen=True)
class InvocationKey(JSONData):
    method_name: str
    null_pos: int
    actuals_length: int
    return_type: str
    raw_return_type: str
    invo_kind: str
    callee_defined: bool

    def matches_up_to_sub_camel_case(self, key):
        if self.null_pos != key.null_pos or self.actuals_length != key.actuals_length or self.return_type != key.return_type or self.callee_defined != key.callee_defined:
            return False

        lhs_camels = camel_case_split(self.method_name)
        rhs_camels = camel_case_split(key.method_name)

        for i in range(0, min(len(lhs_camels), len(rhs_camels))):
            if lhs_camels[i] == rhs_camels[i]:
                return True

        return False


@dataclass(frozen=True)
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
    CallerMethodIsConstructor: bool
    CallerMethodIsPrivate: bool
    CallerMethodIsPublic: bool
    CallerMethodIsStatic: bool
    VariableIsObjectType: bool
    VariableIsFinal: bool
    InvocationIsIsolated: bool
    InvocationIsBase: bool
    InvocationIsConstructorArgument: bool
    CalleeMethodReturnsVoid: Optional[bool] = None
    CalleeMethodReturnsLiteral: Optional[bool] = None
    CalleeMethodThrows: Optional[bool] = None
    CalleeMethodChecksNull: Optional[bool] = None
    CalleeMethodChecksNullForNullParameter: Optional[bool] = None

    def to_boolean_vector(self):
        return [1 if b else 0 for b in self.__dict__.values()]


@dataclass
class NullModel(JSONData):
    invocation_key: Optional[InvocationKey]
    null_value: Optional[str]
    sink_body: str
    contexts: Optional[Contexts]


@dataclass
class NullHandle(JSONData):
    source_path: str
    lineno: int
    handle: str
    model: NullModel


class DB:
    handles: List[NullHandle]

    @classmethod
    def create_from_handles(cls, handles_json):
        handles = []
        for h in JSONData.read_json_from_file(handles_json):
            for m in h['models']:
                try:
                    model = NullModel.from_dict(m)
                except MissingValueError:
                    print(m)
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
        with open(path, 'rb') as f:
            return pickle.load(f)
