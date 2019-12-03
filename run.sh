#!/bin/bash

#if [[ "$1" == "replayer" ]];
#then
#    MAIN_CLASS="org.bptlab.cepta.producers.ProducerRunner"
#else
#    MAIN_CLASS="org.bptlab.cepta.Main"
#fi
MODULE="$1"
cmd=${@:2}
# mvn clean compile exec:java -Dexec.mainClass=${MAIN_CLASS} -Dexec.args="$cmd"
mvn clean compile exec:java -am -pl ${MODULE} -Dexec.args="$cmd"