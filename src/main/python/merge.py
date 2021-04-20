#!/usr/bin/python3

import argparse
import logging
import pandas as pd
import pathlib

logging.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname) -8s %(message)s',
                    filename='merge.log', filemode='w')

logger = logging.getLogger(__name__)

# changed from "*-raw-aggr-bayesian-analysis.txt" after 611f704ed32f879fb6c556039e3b070414070072
DEFAULT_DATA_GLOB = '*-aggregated-chats.csv'
DEFAULT_MERGED_CSV_OUTPUT_FILENAME = "merged.csv"
DEFAULT_SORT_COLUMNS = ['Treatment ID', 'Date', 'Stage', 'Group ID', 'Participant UUID', 'Assigned Number']

__author__ = "Allen Lee"
__version__ = "0.1.0"
__license__ = "MIT"

"""
Merges aggregated treatment data files discovered with the same headers into a single CSV
"""

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--directory", help="Input data directory with subdirectories of round data files to aggregate.")
    parser.add_argument("-m", "--match", help="Glob to match relevant savefiles to aggregate", default=DEFAULT_DATA_GLOB)
    parser.add_argument("-o", "--output", help="Output file", default=DEFAULT_MERGED_CSV_OUTPUT_FILENAME)
    parser.add_argument("-s", "--sortcols", help="Sort output by column names", nargs='*')

    args = parser.parse_args()
    filematch_glob = args.match
    output_filename = args.output
    sort_columns = args.sortcols

    logger.debug("sort columns are %s", sort_columns)

    data_dir = pathlib.Path(args.directory)
    assert data_dir.is_dir()

    paths = []
    for path in data_dir.rglob(filematch_glob):
        logger.debug("path name: %s", path.absolute())
        paths.append(path)

    merged_csv = pd.concat([pd.read_csv(path, engine='python') for path in paths])
    merged_csv.columns = merged_csv.columns.str.strip()
    if not sort_columns:
        sort_columns = DEFAULT_SORT_COLUMNS
        # check if sort columns exists in the data frame
        merged_csv.sort_values(by=sort_columns, inplace=True)
    merged_csv.to_csv(output_filename, index=False, encoding='utf-8-sig')


if __name__ == "__main__":
    main()
