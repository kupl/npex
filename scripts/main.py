#!/usr/bin/python3.8
import subprocess
from multiprocessing import Pool
from functools import partial
from datetime import datetime
import os
import json
import logging
import argparse
import learn
import pprint
from data import DB

JDK_15_PATH = '/usr/lib/jvm/jdk-15.0.1'
NPEX_DRIVER_JAR_PATH = '/home/june/project/npex/driver/target/driver-1.0-SNAPSHOT.jar'
logger = open('log.log', 'w')

def run_npex(project_dir, mode, args=''):
  print(f'Running NPEX-extractor on {project_dir}')
  project_name = os.path.basename(project_dir)
  cmd = f'{JDK_15_PATH}/bin/java --enable-preview -cp {NPEX_DRIVER_JAR_PATH} npex.driver.Main'
  cmd = f'{cmd} {mode} {project_dir} {args}'
  ret = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

  if ret.returncode != 0:
    logger.write(f'{project_name}: Fails to extract null handles\n')
  else:
    logger.write(f'{project_name}: Succeed to extract null handles\n')
  logger.flush()


if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument('-log', help="a log file (default: stored by datetime at the 'logs' in the running directory")
  subparsers = parser.add_subparsers(dest='subcommand')

  collect = subparsers.add_parser('collect', help='collect null-handles')
  collect.add_argument('benchmarks', nargs='+', help='benchmarks directories to collect null-handles')
  collect.add_argument('-j',
                          type=int,
                          dest='jobs',
                          help='the number of jobs to run in parallel (default: %(default)s)',
                          default=4)

  db = subparsers.add_parser('db', help='construct learning DB from collected null handles')
  db.add_argument('benchmarks', nargs='+', help='target projects')
  db.add_argument('db_output', help='output path for constructed DB')

  train = subparsers.add_parser('train', help='train models for each invocation key')
  train.add_argument('db', help='pickled learning DB file')
  train.add_argument('models_dir', help='directory where to store learned classifiers')
  train.add_argument('classifier_output', help='output path for learned classifier')

  predict = subparsers.add_parser('predict', help='predict models for a project with learned classifier')
  predict.add_argument('benchmark_dir', help='target project direcotry')
  predict.add_argument('classifier', help='learned classifier file')
  predict.add_argument('prediction_output', help='learned classifier file')

  args = parser.parse_args()
  if args.subcommand == 'collect':
    with Pool(processes=args.jobs) as pool:
      dbs = pool.map(partial(run_npex, mode='handle-extractor', args='--cached'), args.benchmarks)
    
  elif args.subcommand == 'db':
    handle_json_files = [js for dir in args.benchmarks if os.path.exists(js := f'{dir}/handles.npex.json')]
    db = sum([DB.create_from_handles(js) for js in handle_json_files], DB(handles=[]))
    db.serialize(args.db_output)
    with open(f'{args.db_output}.json', 'w') as f:
      f.write(db.to_json())

  elif args.subcommand == 'train':
    db = DB.deserialize(args.db)
    td = learn.train_classifiers(db, args.models_dir, args.classifier_output)

  elif args.subcommand == 'predict':
    learn.generate_answer_sheet(args.benchmark_dir, args.classifier, args.prediction_output)
