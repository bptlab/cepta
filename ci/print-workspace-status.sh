#!/usr/bin/env bash
# See: https://github.com/kubernetes/kubernetes/blob/master/hack/print-workspace-status.sh

set -o errexit
set -o nounset
set -o pipefail

export CEPTA_ROOT=$(dirname "${BASH_SOURCE}")/..

source "${CEPTA_ROOT}/ci/versioning/query-git.sh"
cepta::version::get_version_vars

# Most important ones
export STABLE_BUILD_GIT_COMMIT=${CEPTA_GIT_COMMIT-}
export STABLE_DOCKER_TAG=${CEPTA_GIT_VERSION/+/_}
export STABLE_BUILD_SCM_REVISION=${CEPTA_GIT_VERSION-}
export STABLE_BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')

# Prefix with STABLE_ so that these values are saved to stable-status.txt
# instead of volatile-status.txt.
# Stamped rules will be retriggered by changes to stable-status.txt, but not by
# changes to volatile-status.txt.
# IMPORTANT: the camelCase vars should match the lists in hack/lib/version.sh
# and pkg/version/def.bzl.
cat <<EOF
STABLE_BUILD_GIT_COMMIT ${STABLE_BUILD_GIT_COMMIT}
STABLE_BUILD_SCM_STATUS ${CEPTA_GIT_TREE_STATE-}
STABLE_BUILD_SCM_REVISION ${STABLE_BUILD_SCM_REVISION}
STABLE_BUILD_MAJOR_VERSION ${CEPTA_GIT_MAJOR-}
STABLE_BUILD_MINOR_VERSION ${CEPTA_GIT_MINOR-}
STABLE_DOCKER_TAG ${STABLE_DOCKER_TAG}
STABLE_BUILD_DATE ${STABLE_BUILD_DATE}
STABLE_VERSION ${STABLE_DOCKER_TAG}
gitCommit ${STABLE_BUILD_GIT_COMMIT}
gitTreeState ${CEPTA_GIT_TREE_STATE-}
gitVersion ${STABLE_BUILD_SCM_REVISION}
gitMajor ${CEPTA_GIT_MAJOR-}
gitMinor ${CEPTA_GIT_MINOR-}
buildDate ${STABLE_BUILD_DATE}
EOF