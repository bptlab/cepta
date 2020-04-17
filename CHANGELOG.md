## Changelog
All notable changes to this project will be documented in this file.

### [0.2.0] - 2020-04-17
##### Added
- #220
  - generic internal high level models to be used in ESP pipelines
- #219
  - Flink operator which removes a duplicate in a given time window
- #215
  - Uniform backoff on startup connection issues making the microservice startup more resilient and stable
- #213
  - Single install helm chart fir deploying to kubernetes clusters
  - GitHub Pages distribution landing page
- #212
  - Building of local docker images with bazel without running them
  - Manually trigger docker hib publishing as part of the release pipeline
- #207
  - More extensive theming support
  - New Map View
  - New Transport Detail View
  - New Live Feed View
  - Link to the grafana monitoring dashboard

##### Changed
- #217
  - consistent naming of bazel targets
- #215
  - Refactored interfaces for mongo, postgres and kafka wrappers
  - Conditional rebuilds of the dev docker deployemnt images
  - Fix of a bug where calling `--help` on some services would block indefinitely long
- #207
  - Fixes to the docker dev deployment
  - UI/UX enhancements and a lot of refactoring
    - Redesign of the user settings view
    - Redesign of the transport management view
- 3b11e14a05e31c29e62e4a8c779d023eadfed9b9
  - Mitigates CVE-2019-20149

##### Removed
- WIP

### [0.1.0] - 2020-04-10
##### Added
- Initial versioned release

[0.2.0]: https://github.com/bptlab/cepta/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/bptlab/cepta/releases/tag/v0.1.0
