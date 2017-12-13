#!/bin/bash -x

DATA_DIR=${1:-incoming}

echo "Processing data in ${DATA_DIR}"

ant convert -Ddata.dir=${DATA_DIR} -Dbots=true >| process-bot-data.log
src/main/python/aggregate.py ${DATA_DIR}

echo "Summary results should be in ${DATA_DIR}/*.csv"
