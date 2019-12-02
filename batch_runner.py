#!/usr/bin/env python3

import sys
import subprocess
from multiprocessing import Pool, Lock, cpu_count
import os
from os import path
import shutil
from functools import partial

print_lock = Lock()


def run_simulation(one_jar, out_dir, settings_file, data_dir=None):
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
    exit_code = subprocess.call('java -jar {} -b'.format(path.abspath(one_jar)),
                                cwd=cwd, stdout=subprocess.DEVNULL)

    with print_lock:
        if exit_code == 0:
            print('Finished processing {}'.format(file_name))
        else:
            print('Error processing {}'.format(file_name))


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

    # wrapper function for partially applying arguments to run_simulation and handling keyboard interrupts
    def run_fn(settings_file):
        try:
            run_simulation(one_jar, out_dir, settings_file, data_dir=data_dir)
        except KeyboardInterrupt:
            return

    with Pool(processes=cpu_count()) as p:
        try:
            p.map(run_fn, settings_files)
        except KeyboardInterrupt:
            p.terminate()


if __name__ == '__main__':
    main(sys.argv)
