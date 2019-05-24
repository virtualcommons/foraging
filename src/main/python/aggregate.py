#!/usr/bin/python3

import argparse
import csv
import logging
import os
import pathlib
import re
import subprocess

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname) -8s %(message)s',
                    filename='foraging-savefile-aggregator.log', filemode='w')

logger = logging.getLogger(__name__)

DEFAULT_DATA_GLOB = '*-raw-aggr-bayesian-analysis.txt'

DEFAULT_TREATMENT_IDENTIFIER = 't1'
DEFAULT_AGGREGATED_CSV_OUTPUT_FILENAME = "aggregated.csv"
AGGREGATED_CSV_HEADER = ['Treatment ID', 'Date', 'Stage', 'Round']

__author__ = "Allen Lee"
__version__ = "0.0.1"
__license__ = "MIT"

"""
Aggregates all data files discovered in the parameterized directory into a single CSV.

"""

ROUND_SAVEFILE_REGEX = re.compile(r'^round-(?P<round_number>\d+)\.?(?P<repeated_round_index>\d+)?.save-*')

def extract_round_number(savefile_name):
    match = ROUND_SAVEFILE_REGEX.match(savefile_name)
    repeated_round_index = match.group('repeated_round_index') or 0
    return match.group('round_number'), repeated_round_index

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--directory", help="Input data directory with directories of round data files to aggregate.")
    parser.add_argument("-m", "--match", help="Glob to match relevant savefiles to aggregate", default=DEFAULT_DATA_GLOB)
    parser.add_argument("-o", "--output", help="Output file", default=DEFAULT_AGGREGATED_CSV_OUTPUT_FILENAME)
    parser.add_argument("-t", "--treatment", help="Treatment identifier", default=DEFAULT_TREATMENT_IDENTIFIER)
# data directory should have a filesystem format like
# <root.data_dir>/<mm-dd-yyyy>/<hh.mm.ss>/<round-<dotted round number>.save
    args = parser.parse_args()
    filematch_glob = args.match
    output_filename = args.output
    treatment_id = args.treatment
    data_dir = pathlib.Path(args.directory)

    assert data_dir.is_dir()
    # extract date and treatment id for each direct subdir in the path
    aggregated_csv_path = pathlib.Path(data_dir, output_filename)
    aggregated_csv_header = list(AGGREGATED_CSV_HEADER)
    with aggregated_csv_path.open('w') as output:
        aggregated_csv = csv.writer(output)
        for date_dir in data_dir.iterdir():
            if not date_dir.is_dir():
                logger.debug("Invalid date dir entry: %s", date_dir)
                continue
            for hour_dir in date_dir.iterdir():
                combined_date_time = '{0} {1}'.format(date_dir.name, hour_dir.name)
                savefiles = hour_dir.glob(filematch_glob)
                for savefile in savefiles:
                    round_number, repeated_round_index = extract_round_number(savefile.name)
                    if os.path.getsize(savefile) == 0:
                        continue
                    with savefile.open('r') as savefile_input:
                        logger.debug("processing savefile: %s", savefile)
                        savefile_csv = csv.reader(savefile_input)
                        savefile_csv_header = next(savefile_csv)
                        logger.debug("header: %s", savefile_csv_header)
                        if len(aggregated_csv_header) == len(AGGREGATED_CSV_HEADER):
                            # append the first savefile header
                            aggregated_csv_header.extend(savefile_csv_header)
                            aggregated_csv.writerow(aggregated_csv_header)
                        for row in savefile_csv:
                            datarow = [treatment_id, combined_date_time, round_number, repeated_round_index] + row
                            logger.debug("datarow: %s", datarow)
                            aggregated_csv.writerow(datarow)
    # at the end sort by the timestamp
    sorted_aggregated_csv = pathlib.Path(data_dir, 'sorted.{0}'.format(output_filename))
    subprocess.run(['sort', '-n', '-k1', '-k4', '-t,', aggregated_csv_path.name],
                   cwd=aggregated_csv_path.parent,
                   stdout=sorted_aggregated_csv.open('w'))


if __name__ == "__main__":
    main()
