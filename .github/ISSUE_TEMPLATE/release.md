---
name: Release📦
about: Create new release issue for Eclipse Che
title: ''
labels: 'kind/release'
assignees: ''

---

### List of pending issues / PRs
* [ ] description #xxx https://github.com/eclipse/che/issues/xxx

### Release status

The following will be released via [che-release](https://github.com/eclipse/che-release/blob/master/cico_release.sh) according to a series of phases:

Phase 1:
- [ ] che-code, configbump, che-machine-exec, che-server, devworkspace-generator, kubernetes-image-puller.

Phase 2:
- [ ] che-e2e, che-plugin-registry, che-dashboard.

Phase 3:
- [ ] [chectl](https://github.com/che-incubator/chectl/pulls/che-bot). _(Che operator PRs must be approved after reviewing and verifying test runs in PR checks)_

Phase 4:
- [ ] [chectl](https://github.com/che-incubator/chectl/pulls/tolusha) _(depends on che-operator)_
- [ ] [Che community operator PRs for Openshift](https://github.com/redhat-openshift-ecosystem/community-operators-prod/pulls/che-incubator-bot) _(depends on che-operator)_
- [ ] [che-docs PR](https://github.com/eclipse/che-docs/pulls/che-bot) _(depends on che-operator)_

Phase 5
- [ ] released Che docs are published on the [Eclipse Che website](https://eclipse.dev/che/docs/stable/administration-guide/enabling-fuse-for-all-workspaces/)
Phase 6
Release can be marked as complete, and this issue can be closed after these steps:
- [ ] check for [remaining version update PRs](https://github.com/eclipse-che/che-release/actions/workflows/release-check-unmerged-PRs.yml) and merge/close them.
- [ ] finalize [release notes](https://github.com/eclipse/che/releases) for this release.

<sup>[1]</sup> Overall process owner: @mkuznyetsov
More information about the process [here](https://github.com/eclipse-che/che-release/blob/main/README.md)
