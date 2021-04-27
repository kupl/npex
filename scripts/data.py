import csv
from dataclasses import dataclass, asdict
from typing import Any, Dict, List, Optional, Tuple
from dacite import from_dict as _from_dict
import pprint
import pickle
import json

@dataclass
class JSONData:
  @classmethod
  def from_dict(klass, d):
      return d if (d == None or d == []) else _from_dict(data_class=klass, data=d)

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

@dataclass
class InvocationSite(JSONData):
  lineno: int
  source_path : str
  deref_field : str

@dataclass(frozen=True)
class InvocationKey(JSONData):
  method_name: str
  null_pos : int
  actuals_length : int
  return_type : str

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
  source_path : str
  lineno : int
  handle : str
  model : NullModel

class DB:
  handles : List[NullHandle]

  @classmethod
  def create_from_handles(cls, handles_json):
    handles = []
    for h in JSONData.read_json_from_file(handles_json):
      for m in h['models']:
         model = NullModel.from_dict(m)
         handle = NullHandle(h['source_location'], h['line_no'], h['handle'], model) 
         handles.append(handle)

    return DB(handles)
      
  def __init__(self, handles):
    self.handles = handles

  def __add__(self, db):
    return DB(self.handles + db.handles)

  def to_json(self):
    return json.dumps([asdict(h) for h in self.handles], indent=4)

  def serialize(self, path):
    with open(path, 'wb') as f:
      pickle.dump(self, f)

  @classmethod
  def deserialize(cls, path):
    with open(path, 'rb') as f:
      return pickle.load(f)