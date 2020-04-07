#!/usr/bin/env bash
# See: https://github.com/kubernetes/kubernetes/blob/master/hack/print-workspace-status.sh

set -o errexit
set -o nounset
set -o pipefail

# export CEPTA_ROOT=$(dirname "${BASH_SOURCE}")/..
export CEPTA_ROOT=$(realpath $(dirname $0)"/../")
echo ${CEPTA_ROOT}

source "${CEPTA_ROOT}/ci/versioning/query-git.sh"
cepta::version::get_version_vars

# Prefix with STABLE_ so that these values are saved to stable-status.txt
# instead of volatile-status.txt.
# Stamped rules will be retriggered by changes to stable-status.txt, but not by
# changes to volatile-status.txt.
# IMPORTANT: the camelCase vars should match the lists in hack/lib/version.sh
# and pkg/version/def.bzl.
cat <<EOF
STABLE_BUILD_GIT_COMMIT ${CEPTA_GIT_COMMIT-}
STABLE_BUILD_SCM_STATUS ${CEPTA_GIT_TREE_STATE-}
STABLE_BUILD_SCM_REVISION ${CEPTA_GIT_VERSION-}
STABLE_BUILD_MAJOR_VERSION ${CEPTA_GIT_MAJOR-}
STABLE_BUILD_MINOR_VERSION ${CEPTA_GIT_MINOR-}
STABLE_DOCKER_TAG ${CEPTA_GIT_VERSION/+/_}
gitCommit ${CEPTA_GIT_COMMIT-}
gitTreeState ${CEPTA_GIT_TREE_STATE-}
gitVersion ${CEPTA_GIT_VERSION-}
gitMajor ${CEPTA_GIT_MAJOR-}
gitMinor ${CEPTA_GIT_MINOR-}
buildDate $(date -u +'%Y-%m-%dT%H:%M:%SZ')
EOF