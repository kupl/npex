#!/usr/bin/python3.8

import csv
import logging
import subprocess
import os
import glob
import json
import argparse
from slacker import Slacker
from datetime import datetime
from  multiprocessing import Pool
from functools import partial

from null_handle import NullHandle
import classifier

ROOT_DIR=os.path.dirname(os.path.realpath(__file__))

JDK_15_PATH = '/usr/lib/jvm/jdk-15.0.1'
NPEX_DRIVER_JAR_PATH = '/home/june/project/npex/driver/target/driver-1.0-SNAPSHOT.jar'

logger = logging.getLogger()

def run_npex_extractor(project_root_dir, results_dir):
    logger.info(f'Running NPEX-extractor on {project_root_dir}')
    project_name = os.path.basename(project_root_dir)
    cmd = f'{JDK_15_PATH}/bin/java --enable-preview -cp {NPEX_DRIVER_JAR_PATH} npex.driver.Main'
    cmd = f'{cmd} handle-extractor {project_root_dir} --results=./{results_dir}/{project_name}.results.json -c'
    ret = subprocess.run(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    if ret.returncode != 0:
      logger.error(f'Fails to extract null handles for project {project_name}')
      with open(f'logs/{project_name}.err.log', 'w') as f:
        f.write(ret.stdout.decode('utf-8'))
    

def collect(benchmarks, results_dir, jobs):
  with Pool(processes=jobs) as pool:
    pool.map(partial(run_npex_extractor, results_dir=results_dir), benchmarks)
    pool.join()

def statistics(results, cached=False):
  dicts = []
  for r in results:
    js = NullHandle.from_result(r)
    dicts.extend(js)

  rows = sum([d.flatten() for d in dicts], [])

  with open('results.csv', 'w', newline='') as csvfile:
    writer = csv.DictWriter(csvfile, fieldnames=NullHandle.get_columns(), restval=None)
    writer.writeheader()
    for row in rows:
      writer.writerow(row)


def __initialize_logger(logpath):
  logger.setLevel(logging.NOTSET)

  consoleHandler = logging.StreamHandler()
  consoleHandler.setLevel(logging.INFO)
  consoleHandler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(message)s"))
  logger.addHandler(consoleHandler)

  fileHandler = logging.FileHandler(logpath,'w', encoding=None, delay=True)
  fileHandler.setLevel(logging.DEBUG)
  fileHandler.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(message)s"))
  logger.addHandler(fileHandler)

    
if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument('-log', help="a log file (default: stored by datetime at the 'logs' in the running directory")
  subparsers = parser.add_subparsers( dest='subcommand')

  collector = subparsers.add_parser('collect', help='collect null-handles')
  collector.add_argument('benchmarks', nargs='+', help='benchmarks directories to collect null-handles')
  collector.add_argument('results_dir', help='a directory to store collected null-handles in JSON format (default: %(default)s)', default=f'{os.getcwd()}/results')
  collector.add_argument('-j', type=int, dest='jobs',help='the number of jobs to run in parallel (default: %(default)s)', default=4)

  stat = subparsers.add_parser('stat', help='print statistics on collected null-handles')
  stat.add_argument('results_dir', help='a directory where collected null-handles')

  learn = subparsers.add_parser('learn', help='learn model classifier')
  learn.add_argument('data', help='csvfile for collected null-handles')
  learn.add_argument('-cls', help='path to store learned classifier (default: %(default)s)', default=classifier.CLASSIFIER_PATH)

  predict = subparsers.add_parser('predict', help='predict label with learned classifier')
  predict.add_argument('data', help='csvfile for collected null-handles')
  predict.add_argument('-cls', help='path to store learned classifier (default: %(default)s)', default=classifier.CLASSIFIER_PATH)

  args = parser.parse_args()
  logpath = f'{datetime.today().strftime("%m%d%_H%M%_S")}.log' if args.log == None else args.log
  __initialize_logger(logpath)

  if args.subcommand == 'collect':
    os.makedirs(args.results_dir, exist_ok=False)
    os.makedirs("logs")
    collect(args.benchmarks, args.results_dir, jobs=args.jobs)
    
  elif args.subcommand == 'stat':
    results = [r for r in glob.glob(f'{args.results_dir}/*') if r.endswith('.json')]
    statistics(results)

  elif args.subcommand == 'learn':
    classifier.train(args.data, args.cls)

  elif args.subcommand == 'predict':
    classifier.see_conflict(args.data, args.cls) 

