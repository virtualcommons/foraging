#!/usr/bin/python3

import argparse
import csv
import logging
import os
import pathlib
import re

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname) -8s %(message)s',
                    filename='foraging-savefile-aggregator.log', filemode='w')

logger = logging.getLogger(__name__)

# changed from "*-raw-aggr-bayesian-analysis.txt" after 611f704ed32f879fb6c556039e3b070414070072
DEFAULT_DATA_GLOB = '*-client-summary-interval.txt'

DEFAULT_TREATMENT_IDENTIFIER = None
DEFAULT_AGGREGATED_CSV_OUTPUT_FILENAME = "aggregated.csv"
AGGREGATED_CSV_HEADER = ['Treatment ID', 'Date', 'Stage', 'Round']

__author__ = "Allen Lee"
__version__ = "0.1.0"
__license__ = "MIT"

"""
Aggregates multiple round data files discovered in the parameterized treatment directory into a single CSV.
"""

ROUND_SAVEFILE_REGEX = re.compile(r'^round-(?P<round_number>\d+)\.?(?P<repeated_round_index>\d+)?.save-*')

def extract_round_number(savefile_name):
    match = ROUND_SAVEFILE_REGEX.match(savefile_name)
    repeated_round_index = match.group('repeated_round_index') or 0
    return match.group('round_number'), repeated_round_index

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--directory", help="Input data directory with subdirectories of round data files to aggregate.")
    parser.add_argument("-m", "--match", help="Glob to match relevant savefiles to aggregate", default=DEFAULT_DATA_GLOB)
    parser.add_argument("-o", "--output", help="Output file", default=DEFAULT_AGGREGATED_CSV_OUTPUT_FILENAME)
    parser.add_argument("-t", "--treatment", help="Treatment identifier", default=DEFAULT_TREATMENT_IDENTIFIER)
# data directory should have a filesystem format like
# <root.data_dir>/<mm-dd-yyyy>/<hh.mm.ss>/<round-<dotted round number>.save
# FIXME: pull treatment id from directory name e.g., <root.data_dir>/<treatment.id>/<<mm-dd-yyyy>/...
    args = parser.parse_args()
    filematch_glob = args.match
    output_filename = args.output
    treatment_id = args.treatment
    if treatment_id:
        logger.warning("manually adding treatment id to all datarows: %s", treatment_id)
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
                combined_date_time = f'{date_dir.name} {hour_dir.name}'
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
                            datarow = [combined_date_time, round_number, repeated_round_index] + row
                            if treatment_id:
                                datarow.insert(0, treatment_id)
                            logger.debug("datarow: %s", datarow)
                            aggregated_csv.writerow(datarow)
    """
    disabled sorting routine
    sorted_aggregated_csv = pathlib.Path(data_dir, f'sorted.{output_filename}')
    with sorted_aggregated_csv.open('w') as sorted_output:
        subprocess.run(['sort', '-b', '-k2,2', '-k5,5', '-t,', aggregated_csv_path.name],
                       cwd=aggregated_csv_path.parent,
                       stdout=sorted_output)
    """


if __name__ == "__main__":
    main()
