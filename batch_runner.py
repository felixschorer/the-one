#!/usr/bin/env python3

"""
Script for running multiple instances of the simulator in parallel. Default settings are ignored.

Usage: {} ONE_JAR SETTINGS_DIR OUT_DIR [DATA_DIR]

ONE_JAR       path to compiled .jar of the simulator
SETTINGS_DIR  path to the directory which contains the settings files which should be run
OUT_DIR       path to the directory where the results should be saved
DATA_DIR      path to an optional data directory which should be copied into the working directory of the simulator
"""

import os
import shutil
import subprocess
import sys
from functools import partial
from multiprocessing import Pool, Lock, cpu_count
from os import path

print_lock = Lock()


def run_simulation(one_jar, out_dir, settings_file, data_dir=None):
    try:
        file_name, etx = path.splitext(path.basename(settings_file))
        cwd = path.join(out_dir, file_name)

        # remove working directory if it already exists
        if path.exists(cwd):
            shutil.rmtree(cwd)

        # create working directory and copy settings and data into it
        os.mkdir(cwd)
        shutil.copy2(settings_file, path.join(cwd, 'default_settings.txt'))
        if data_dir is not None:
            shutil.copytree(data_dir, path.join(cwd, path.basename(data_dir)))

        with print_lock:
            print('Start processing {}'.format(file_name))

        # run simulator in working directory
        with open(path.join(cwd, 'out.log'), 'w') as log_file:
            exit_code = subprocess.call('java -jar {} -b'.format(path.abspath(one_jar)),
                                        cwd=cwd, stdout=log_file, stderr=subprocess.STDOUT)

        with print_lock:
            if exit_code == 0:
                print('Finished processing {}'.format(file_name))
            else:
                print('Error processing {}'.format(file_name))
    except KeyboardInterrupt:
        return


def main(argv):
    if len(argv) < 4 or 5 < len(argv):
        binary = path.basename(argv[0])
        print("Usage: {} ONE_JAR SETTINGS_DIR OUT_DIR [DATA_DIR]".format(binary))
        sys.exit(0)

    one_jar, settings_dir, out_dir = argv[1:4]
    data_dir = argv[4] if len(argv) == 5 else None

    if not path.exists(out_dir):
        os.mkdir(out_dir)

    directory_entries = [path.join(settings_dir, entry) for entry in os.listdir(settings_dir)]
    settings_files = [entry for entry in directory_entries if path.isfile(entry)]

    with Pool(processes=cpu_count()) as p:
        try:
            p.map(partial(run_simulation, one_jar, out_dir, data_dir=data_dir), settings_files)
        except KeyboardInterrupt:
            p.terminate()


if __name__ == '__main__':
    main(sys.argv)
