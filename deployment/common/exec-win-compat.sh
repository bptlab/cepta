#!/bin/sh
entry=$(mktemp) && cp "$1" ${entry} && dos2unix ${entry} && /bin/sh ${entry}
