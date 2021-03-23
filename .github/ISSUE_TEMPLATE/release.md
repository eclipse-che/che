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

In series, the following will be released via [che-release](https://github.com/eclipse/che-release/blob/master/cico_release.sh):

* che-theia, che-machine-exec, che-devfile registry,
* che-plugin-registry (once che-theia and machine-exec are done)
* che-parent, che-dashboard, and che (server assembly)
  * Note that [release to Maven Central is deprecated](https://github.com/eclipse/che/issues/18941)
* che-operator

Then, these steps will be done, which still [require some manual intervention](https://github.com/eclipse/che-release/blob/master/README.md#phase-2---manual-steps) (PR verification is not yet automated):

* generation of OperatorHub PRs
* chectl

- [ ] che-theia, che-machine-exec, che-devfile-registry, che-plugin-registry
- [ ] che-parent, che-dashboard, che
- [ ] che-operator _(depends on all of the above)_
- [ ] chectl _(depends on che-operator)_
- [ ] [Che community operator PRs](https://github.com/operator-framework/community-operators/pulls?q=%22Update+eclipse-che+operator%22+is%3Aopen) _(depends on che-operator)_
- [ ] [che-docs PR](https://github.com/eclipse/che-docs/pulls/che-bot) _(depends on che-operator)_
If this is a .0 release:

- [ ] [send reminder to TLs to triage unresolved issues](https://github.com/eclipse/che/issues/17579)
- [ ] complete current milestone
  - [ ] move incomplete *deferred* issues to backlog
  - [ ] move incomplete *WIP* issues to next milestone
  - [ ] close completed issues
  - [ ] close milestone

| Process <sup>[1]</sup> | Script | Action | Container(s) + Artifact(s) |
| --- | --- | --- | --- |
| [che-release](https://github.com/eclipse/che-release/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [Action](https://github.com/eclipse/che-release/actions?query=workflow%3A%22Release+-+Orchestrate+Overall+Release+Phases%22) | n/a |
| [che-theia](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-theia/blob/master/make-release.sh) | [Action](https://github.com/eclipse/che-theia/actions?query=workflow%3A%22Release+Che+Theia%22) | [`eclipse/che-theia`](https://quay.io/eclipse/che-theia) |
| [che-machine-exec](https://github.com/eclipse-che/che-machine-exec/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse-che/che-machine-exec/blob/master/make-release.sh) | [Action](https://github.com/eclipse-che/che-machine-exec/actions?query=workflow%3A%22Release+Che+Machine+Exec%22) | [`eclipse/che-machine-exec`](https://quay.io/eclipse/che-machine-exec)|
| [che-devfile-registry](https://github.com/eclipse/che-devfile-registry/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-devfile-registry/blob/master/make-release.sh) | [Action](https://github.com/eclipse/che-devfile-registry/actions?query=workflow%3A%22Release+Che+Devfile+Registry%22) | [`eclipse/che-devfile-registry`](https://quay.io/eclipse/che-devfile-registry)|
| [che-plugin-registry](https://github.com/eclipse/che-plugin-registry/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-plugin-registry/blob/master/make-release.sh) | [Action](https://github.com/eclipse/che-plugin-registry/actions?query=workflow%3A%22Release+Che+Plugin+Registry%22) | [`eclipse/che-plugin-registry`](https://quay.io/eclipse/che-plugin-registry)|
| [che-parent](https://github.com/eclipse/che-parent/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-parent/blob/master/make-release.sh) | [Action](https://github.com/eclipse/che/actions?query=workflow%3A%22Release+Che+Server%22) | [che-server](https://search.maven.org/search?q=a:che-server) <sup>[2]</sup> |
| [che-dashboard](https://github.com/eclipse/che-dashboard/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-dashboard/blob/master/make-release.sh) | [Action](https://github.com/eclipse/che-dashboard/actions?query=workflow%3A%22Release+Che+Dashboard%22) | [`che-dashboard`](https://quay.io/repository/eclipse/che-dashboard?tag=next&tab=tags) |
| [che](https://github.com/eclipse/che/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che/blob/master/make-release.sh) | [Action](https://github.com/eclipse/che/actions?query=workflow%3A%22Release+Che+Server%22) | [che.core](https://search.maven.org/search?q=che.core) <sup>[2]</sup>,<br/> [`eclipse/che-server`](https://quay.io/eclipse/che-server),<br/>[`eclipse/che-endpoint-watcher`](https://quay.io/eclipse/che-endpoint-watcher),<br/> [`eclipse/che-keycloak`](https://quay.io/eclipse/che-keycloak),<br/> [`eclipse/che-postgres`](https://quay.io/eclipse/che-postgres),<br/> [`eclipse/che-server`](https://quay.io/eclipse/che-server),<br/> [`eclipse/che-e2e`](https://quay.io/eclipse/che-e2e) |
| [che-operator](https://github.com/eclipse-che/che-operator/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse-che/che-operator/blob/master/make-release.sh) | [Action](https://github.com/eclipse-che/che-operator/actions?query=workflow%3A%22Release+Che+Operator%22) | [`eclipse/che-operator`](https://quay.io/eclipse/che-operator)|
| [chectl](https://github.com/che-incubator/chectl/blob/master/RELEASE.md) | [make-release.sh](https://github.com/che-incubator/chectl/blob/master/make-release.sh) | [Action](https://github.com/che-incubator/chectl/actions) | [chectl releases](https://github.com/che-incubator/chectl/releases)


<sup>[1]</sup> Overall process owner: @mkuznyetsov

<sup>[2]</sup> Note that [release of artifacts to Maven Central is deprecated](https://github.com/eclipse/che/issues/18941) and will be removed in the future.
