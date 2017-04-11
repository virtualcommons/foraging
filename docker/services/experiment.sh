#!/bin/sh

cd /code
ant deploy
nohup java -jar -server server.jar
