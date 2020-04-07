package versioning

import (
	"errors"
	"fmt"
	"os"
)

// VersionInfo ...
type VersionInfo int

const (
	StableBuildGitCommit VersionInfo = iota
	StableBuildScmStatus
	StableBuildScmRevision
	StableBuildMajorVersion
	StableBuildMinorVersion
	StableDockerTag
	gitCommit
	gitTreeState
	gitVersion
	gitMajor
	gitMinor
	buildDate
)

var mapping = map[VersionInfo]string{
	StableBuildGitCommit:    "STABLE_BUILD_GIT_COMMIT",
	StableBuildScmStatus:    "STABLE_BUILD_SCM_STATUS",
	StableBuildScmRevision:  "STABLE_BUILD_SCM_REVISION",
	StableBuildMajorVersion: "STABLE_BUILD_MAJOR_VERSION",
	StableBuildMinorVersion: "STABLE_BUILD_MINOR_VERSION",
	StableDockerTag:         "STABLE_DOCKER_TAG",
	gitCommit:               "gitCommit",
	gitTreeState:            "gitTreeState",
	gitVersion:              "gitVersion",
	gitMajor:                "gitMajor",
	gitMinor:                "gitMinor",
	buildDate:               "buildDate",
}

// Query returns git versioning info attached to the current bazel build
func Query(request VersionInfo) (string, error) {
	if envVar, ok := mapping[request]; ok {
		if value := os.Getenv(envVar); value != "" {
			return value, nil
		}
		return "", fmt.Errorf("%s not set", envVar)
	}
	return "", errors.New("No such version info")
}

// GetVersion returns the default formatted version string attached to the current bazel build
func GetVersion() string {
	var result string
	if version, err := Query(gitVersion); err == nil {
		result = version
		if commit, err := Query(gitCommit); err == nil {
			result += fmt.Sprintf(" (#%s)", commit)
		}
		if buildDate, err := Query(buildDate); err == nil {
			result += fmt.Sprintf(" (built %s)", buildDate)
		}
	}
	return result
}

// BinaryVersion returns the default formatted version string for a binary
func BinaryVersion(version string, buildTime string) string {
	if buildTime != "" {
		return fmt.Sprintf("%s (built on %s)", version, buildTime)
	}
	return version
}
