#!/bin/bash

cd $(dirname $0)
MODULE="$1"
cmd=${@:2}
mvn -am -pl ${MODULE} clean compile exec:java -Dexec.args="$cmd"