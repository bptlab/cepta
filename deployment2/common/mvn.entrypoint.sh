#!/bin/sh
mvn exec:java -pl $MODULE_ENV -am -Dexec.args="$*"