import argparse
import time
import json
import glob
import os
import subprocess
import shutil
import csv
import sys
import random
from pprint import pprint
from typing import List, Dict
from dataclasses import asdict, dataclass, field, fields, is_dataclass
NPEX_DIR = f'{os.path.dirname(os.path.realpath(__file__))}/..'
INFER_DIR = f"{NPEX_DIR}/npex-analyzer"
sys.path.append(f"{INFER_DIR}/scripts")
from verify import *

ROOT_DIR = os.getcwd()
INFER_PATH = f"{INFER_DIR}/infer/bin/infer"
NPEX_JAR = f"{NPEX_DIR}/npex-driver/target/npex-driver-1.0-SNAPSHOT.jar"
if os.path.isfile(f"{NPEX_JAR}") is False:
  print(f"no jar in {NPEX_JAR}")
  exit(1)
NPEX_SCRIPT = f"{NPEX_DIR}/scripts/main.py"
CLASSIFIER = f"{NPEX_DIR}/examples/classifier-example"

JDK_15 = "/usr/lib/jvm/jdk-15.0.1"
JAVA_15 = f"{JDK_15}/bin/java"
NPEX_CMD = f"{JAVA_15} --enable-preview -cp {NPEX_JAR} npex.driver.Main"

MVN_OPT = "-V -B -Denforcer.skip=true -Dcheckstyle.skip=true -Dcobertura.skip=true -Drat.skip=true -Dlicense.skip=true -Dfindbugs.skip=true -Dgpg.skip=true -Dskip.npm=true -Dskip.gulp=true -Dskip.bower=true -DskipTests=true -DskipITs=true -Dtest=None -DfailIfNoTests=false"

def parse_trace(bug, st_file):
    with open(st_file, 'r') as f:
      lines = f.readlines()
    java_files = glob.glob(f"{bug.project_root_dir}/**/*.java", recursive=True)
    traces = []
    for splitted in lines:
        if "at " not in splitted:
            continue
        content = splitted.split("at ")[1]

        if len(content.split("(")) != 2:
            continue
        [pkg_mthd_path, file_line_right] = content.split("(")

        if "." not in pkg_mthd_path:
            continue
        pkg_path, mthd = "/".join(
            pkg_mthd_path.split(".")[:-1]), pkg_mthd_path.split(".")[-1]
        file_line = file_line_right.rstrip(")").rstrip(")\n")

        if len(file_line.split(":")) != 2:
            continue

        [file, line] = file_line.split(":")
        filepath = None
        for java_file in java_files:
            if os.path.basename(java_file) != file:
                continue

            if filepath is None:
                filepath = java_file

            if pkg_path in java_file:
                filepath = java_file

        if filepath:
            traces.append({
                "filepath": filepath,
                "line": int(line),
                "method_name": mthd
            })


    if traces == []:
      print(f"[ERROR]: failed to parse trace from {st_file}")
      exit(1)

    with open(f"{bug.project_root_dir}/traces.json", 'w') as json_file:
      json_file.write(json.dumps(traces, indent=4))

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest='subcommand')

    prepare = subparsers.add_parser('prepare', help="prepare")
    prepare.add_argument('--parse_trace', help="parse trace")
    prepare.add_argument('--build', action="store_true", help="build project with infer and spoon")

    run_cmd = subparsers.add_parser('run', help="run npex")
    run_cmd.add_argument("--localize", action="store_true", help="#1: run fault-localization")
    run_cmd.add_argument("--enumerate", action="store_true", help="#2: enumerate patch candidates")
    run_cmd.add_argument("--predict", action='store_true', help="#3: predict null handles with the specified classifier")
    run_cmd.add_argument("--classifier", default=CLASSIFIER, help="#3: specifiy classifier, default is example/classifier-example")
    run_cmd.add_argument("--validate", action="store_true", help="#4: infer specs & validate patches")
    run_cmd.add_argument("--all", action="store_true", help="do all")


    args = parser.parse_args()
    error_reports = glob.glob("npe*.json") 
    bug = Bug(ROOT_DIR, error_reports)

    if os.path.isfile(f"{ROOT_DIR}/pom.xml") is False and os.path.isfile(f"{ROOT_DIR}/Main.java") is False:
        print(f"[ERROR]: invalid project; no pom.xml or no Main.java")
        exit(1)

    if args.subcommand == "prepare":
      if args.build:
        pprint(f"Build projects by infer & spoon")
        bug.capture_all(True)
        subprocess.run(f"{NPEX_CMD} build {bug.project_root_dir}", shell=True, cwd=bug.project_root_dir)
      
      if args.parse_trace:
        pprint(f"Parse NPE stack trace")
        parse_trace(bug, args.parse_trace)  

    if args.subcommand == "run":
      if args.all or args.localize:
        pprint(f"localize faulty null expressions by npe.json")
        bug.capture_all(False)
        bug.localize(False)

      if args.all or args.enumerate:
        pprint(f"Enumerate patch candidates for {error_reports}")
        for report in error_reports:
          subprocess.run(f"{NPEX_CMD} patch {bug.project_root_dir} --cached --report={report}", shell=True, cwd=bug.project_root_dir)

      if args.all or args.predict:
        pprint(f"Predict null handlings by {args.classifier}")
        bug.generate_model(args.classifier)

      # if args.all or args.infer:
      if args.all or args.validate:
        pprint(f"Start inference by {error_reports}")
        bug.capture_all(False)
        bug.inference(False, False, -1)
        pprint(f"Start verification by {error_reports}")
        bug.verify_all(-1, True)

#    subparsers = parser.add_subparsers(dest='prepare')
#    subparsers = parser.add_subparsers(dest='localize')
#    subparsers = parser.add_subparsers(dest='enumerate')
#    subparsers = parser.add_subparsers(dest='predict')
#    subparsers = parser.add_subparsers(dest='infer')
#    subparsers = parser.add_subparsers(dest='validate')
#    enumerate = subparsers.add_parser('enumerate', help="enumerate") 
#    predict = subparsers.add_parser('predict', help="predict")
#    predict.add_argument("--classifiers", help="classifiers to extract null model")
#    infer = subparsers.add_parser('infer', help="infer")
#    validate = subparsers.add_parser('validate', help="validate")
