import json
import os
import typing
from dataclasses import dataclass
from typing import Dict, List, Any, Optional
from dacite import from_dict as _from_dict

def from_dict(klass, d):
  if d == None or d== []:
      return d
  else:
      return _from_dict(data_class=klass, data=d)

def read_json_from_file(json_filename: str):
  with open(json_filename, "r") as f:
    return json.load(f)

def get_primitive_fields(cls):
  return [k for (k, v) in cls.__annotations__.items() if v in [str, int, bool, List[str], Optional[str], Optional[int]]]


@dataclass
class JSONData:
  @classmethod
  def from_dict(klass, d):
    if d == None or d== []:
        return d
    else:
        return _from_dict(data_class=klass, data=d)

  @classmethod
  def _from_json(cls, jsonfile):
    return cls.from_dict(cls._read_json_from_file(jsonfile))

  @classmethod
  def _read_json_from_file(cls, json_filename: str):
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
class InvoInfo:
  null_invo: str
  null_idx: int
  method_name: str
  return_type: Optional[str]
  arguments_types : List[str]
  invo_kind : str 
  target_type : Optional[str]

  @classmethod
  def get_columns(cls):
    return get_primitive_fields(cls)

  def flatten(self):
    return self.__dict__

@dataclass
class Contexts:
  UsedAsReturnExpression : bool
  UsedAsArgument : bool
  NullCheckingExists : bool
  IsField : bool
  IsParameter : bool
  IsVariable : bool

  @classmethod
  def get_columns(cls):
    return get_primitive_fields(cls)

  def flatten(self):
    return self.__dict__

@dataclass
class NullModel:
  sink_body : str
  null_value : Optional[str]
  invocation_info : Optional[InvoInfo]
  contexts : Optional[Contexts]

  @classmethod
  def get_columns(cls):
    return get_primitive_fields(cls) + InvoInfo.get_columns() + Contexts.get_columns()

  def flatten(self):
    row = dict() 
    for f in get_primitive_fields(self.__class__):
      row[f] = vars(self)[f]
    
    if self.invocation_info != None:
      row.update(self.invocation_info.flatten())
      row.update(self.contexts.flatten())

    return row

@dataclass
class NullHandle:
  project : str
  source_location  : str
  line_no : int
  handle : str
  models : List[NullModel]

  @classmethod
  def from_result(cls, jsonfile):
    handles = read_json_from_file(jsonfile)
    ret = []
    for h in handles:
        h2 = from_dict(cls, {'project': os.path.basename(jsonfile).split('.')[0], **h})
        ret.append(h2)
    return ret


  @classmethod
  def get_columns(cls):
    return get_primitive_fields(cls) + NullModel.get_columns()

  def flatten(self):
    row = dict()
    for f in (get_primitive_fields(self.__class__)):
      row[f] = vars(self)[f]

    results = []
    for m in self.models:
      d = m.flatten()
      d.update(row)
      results.append(d)

    return results
