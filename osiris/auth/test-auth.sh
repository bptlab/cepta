#!/bin/sh

# Usage: $ TOKEN=my-token ./test-auth.sh

curl -H 'Accept: application/json' -H "Authorization: Bearer ${TOKEN}" http://localhost:80