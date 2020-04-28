## Changelog
All notable changes to this project will be documented in this file.

### [0.3.0] - 2020-04-24
##### Added
- WIP
- [#248](https://github.com/bptlab/cepta/pull/248)
  - Refactors Delay detection based on Live and Planned Station arrival Data including tests
  - changes TrainDelayNotification location_id to station_id to match conventions of other events
- [#242](https://github.com/bptlab/cepta/pull/242)
  - Abstractions for starting mongo containers in golang tests
  - Implement e2e authentication via envoy `http.jwt_authn.v2alpha.JwtAuthentication`
  - Utility script for generating private and public RSA keys and a jwk set
  - Integration tests for authentication
  - Integration of authentication in the frontend
  - Add `auth` and `usermgmt` microservices in dev and prod docker env
- [#231](https://github.com/bptlab/cepta/pull/231)
  - Track the average speed of transports
  - t.b.a.
- [#220](https://github.com/bptlab/cepta/pull/220)
  - generic internal high level models to be used in ESP pipelines
- [#213](https://github.com/bptlab/cepta/pull/213)
  - Single install helm chart for deploying to kubernetes clusters
  - Automatic packaging and distribution via GitHub Pages
- [#241](https://github.com/bptlab/cepta/pull/241)
  - added a pattern which detects a train staying in a station
- [#204](https://github.com/bptlab/cepta/pull/219)
  - Remove duplicate function which removes all duplicate events in a DataStream

##### Changed
- WIP
- [#242](https://github.com/bptlab/cepta/pull/242)
  - Complete rewrite of auth and user management microservices
- [#217](https://github.com/bptlab/cepta/pull/217)
  - consistent naming of bazel targets

##### Removed
- WIP

### [0.2.0] - 2020-04-17
##### Added
- [#234](https://github.com/bptlab/cepta/pull/234)
  - Simple landing page with basic information about then CEPTA project
  - `.travis.yml` stage for automatically deploying the website
- [#219](https://github.com/bptlab/cepta/pull/219)
  - New operator which removes a duplicate with a tumbling event number window with a given size
- [#215](https://github.com/bptlab/cepta/pull/215)
  - Uniform backoff on startup connection issues making the microservice startup more resilient and stable
- [#212](https://github.com/bptlab/cepta/pull/212)
  - Building of local docker images with bazel without running them
  - Manually trigger docker hub publishing of the `anubis` frontend as part of the release pipeline
- [#207](https://github.com/bptlab/cepta/pull/207)
  - More extensive theming support
  - New Map View
  - New Transport Detail View
  - New Live Feed View
  - Link to the grafana monitoring dashboard

##### Changed
- [#215](https://github.com/bptlab/cepta/pull/215)
  - Refactored interfaces for mongo, postgres and kafka wrappers
  - Conditional rebuilds of the dev docker deployemnt images
  - Fix of a bug where calling `--help` on some services would block indefinitely long
- [#207](https://github.com/bptlab/cepta/pull/207)
  - Fixes to the docker dev deployment
  - UI/UX enhancements and a lot of refactoring
    - Redesign of the user settings view
    - Redesign of the transport management view
- [3b11e14...](https://github.com/bptlab/cepta/commit/3b11e14a05e31c29e62e4a8c779d023eadfed9b9)
  - Mitigates CVE-2019-20149

##### Removed

### [0.1.0] - 2020-04-10
##### Added
- Initial versioned release

[0.3.0]: https://github.com/bptlab/cepta/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/bptlab/cepta/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/bptlab/cepta/releases/tag/v0.1.0
