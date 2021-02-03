#!/usr/bin/python3.8

import csv
import logging
import subprocess
import os
import glob
import json
import argparse
from datetime import datetime
from  multiprocessing import Pool
from functools import partial

from null_handle import NullHandle


'''
이슈를 정라하란 말이야 라고 해서 정리하는 커멘트:
- 이 스크립트는 모마 (모델 마이닝이라는 뜻)를 위한 스크립트입니다.
- 이 스크립트는 각각의 프로젝트를 돌면서, 각 프로젝트에 대해서 다음과 같은 두 를 합니다
-- do(project):
  1. NPEX 합성기를 돌려서 JSON results를 만들어낸다.
  2. 이 때 에러가 발생한 경우, 잘 처리를 합니다.
  3. 각 JSON results를 합쳐서 전체 데이터를 만들어낸다.
  4. 준희형이 요청하는게 생길때마다 필터 함수를 하나씩 구현한다
'''

ROOT_DIR=os.path.dirname(os.path.realpath(__file__))

JDK_15_PATH = '/usr/lib/jvm/jdk-15.0.1'
NPEX_DRIVER_JAR_PATH = '/home/june/project/npex/driver/target/driver-1.0-SNAPSHOT.jar'


logger = logging.getLogger()

def run_npex_extractor(project_root_dir, results_dir):
    logger.info(f'Running NPEX-extractor on {project_root_dir}')
    project_name = os.path.basename(project_root_dir)
    cmd = f'{JDK_15_PATH}/bin/java --enable-preview -cp {NPEX_DRIVER_JAR_PATH} npex.driver.Main'
    cmd = f'{cmd} -g handle-extractor {project_root_dir} --results=./results/{project_name}.results.json'
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

  keys = set()
  rows = sum([d.flatten() for d in dicts], [])
  for row in rows:
    keys = keys.union(row.keys())
  print(keys)

  with open('results.csv', 'w', newline='') as csvfile:
    writer = csv.DictWriter(csvfile, fieldnames=keys, restval=None)
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
  parser.add_argument('-log', help="a log file (default: stored by datetime at the 'logs' in script's root directory")
  subparsers = parser.add_subparsers( dest='subcommand')

  collector = subparsers.add_parser('collect', help='collect null-handles')
  collector.add_argument('benchmarks', nargs='+', help='benchmarks directories to collect null-handles')
  collector.add_argument('results_dir', help='a directory to store collected null-handles in JSON format (default: %(default)s)', default=f'{ROOT_DIR}/results')
  collector.add_argument('-j', type=int, dest='jobs',help='the number of jobs to run in parallel (default: %(default)s)', default=4)

  stat = subparsers.add_parser('stat', help='print statistics on collected null-handles')
  stat.add_argument('results_dir', help='a directory where collected null-handles')

  args = parser.parse_args()
  logpath = f'{ROOT_DIR}/{datetime.today().strftime("%m%d%_H%M%_S")}.log' if args.log == None else args.log
  __initialize_logger(logpath)

  if args.subcommand == 'collect':
    os.makedirs(args.results_dir, exist_ok=False)
    collect(args.benchmarks, args.results_dir, jobs=args.jobs)
    
  elif args.subcommand == 'stat':
    results = [r for r in glob.glob(f'{args.results_dir}/*') if r.endswith('.json')]
    statistics(results)

    
  
