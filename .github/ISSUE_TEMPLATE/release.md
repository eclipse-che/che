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
* che-parent, che-dashboard, che-workspace-loader, and che (server assembly), including release to [Maven Central](https://github.com/eclipse/che/issues/16043)
* che-operator

Then, these steps will be done, which still [require some manual intervention](https://github.com/eclipse/che-release/blob/master/README.md#phase-2---manual-steps) (PR verification is not yet automated):

* generation of OperatorHub PRs
* chectl

- [ ] che-theia, che-machine-exec, che-devfile-registry, che-plugin-registry
- [ ] che-parent, che-dashboard, che-workspace-loader, che
- [ ] che-operator _(depends on all of the above)_
- [ ] [Che community operator PRs](https://github.com/operator-framework/community-operators/pulls?q=%22Update+eclipse-che+operator%22+is%3Aopen) _(depends on all of the above)_
- [ ] chectl _(depends on all of the above)_

If this is a .0 release:

- [ ] [send reminder to TLs to triage unresolved issues](https://github.com/eclipse/che/issues/17579)
- [ ] complete current milestone
  - [ ] move incomplete *deferred* issues to backlog
  - [ ] move incomplete *WIP* issues to next milestone
  - [ ] close completed issues
  - [ ] close milestone

| Owner | Process | Script | CI | Artifact(s) + Container(s) |
| --- | --- | --- | --- | --- |
| @mkuznyetsov  | [che-theia](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-theia-che-release/) | [`eclipse/che-theia`](https://quay.io/eclipse/che-theia) |
| @mkuznyetsov  | [che-machine-exec](https://github.com/eclipse/che-machine-exec/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-machine-exec-release/) | [`eclipse/che-machine-exec`](https://quay.io/eclipse/che-machine-exec)| 
| @mkuznyetsov  | [che-plugin-registry](https://github.com/eclipse/che-plugin-registry/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-plugin-registry-release/) | [`eclipse/che-plugin-registry`](https://quay.io/eclipse/che-plugin-registry)| 
| @mkuznyetsov  | [che-devfile-registry](https://github.com/eclipse/che-devfile-registry/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-devfile-registry-release/) | [`eclipse/che-devfile-registry`](https://quay.io/eclipse/che-devfile-registry)| 
| @mkuznyetsov  | [che-parent](https://github.com/eclipse/che/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release) | [che-parent](https://search.maven.org/search?q=a:che-parent) |
| @mkuznyetsov  | [che-dashboard](https://github.com/eclipse/che-dashboard/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release/) | [`che-dashboard`](https://quay.io/repository/eclipse/che-dashboard?tag=next&tab=tags) |
| @mkuznyetsov  | [che-workspace-loader](https://github.com/eclipse/che-workspace-loader/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release) | [`che-workspace-loader`](https://quay.io/repository/eclipse/che-workspace-loader?tag=next&tab=tags) |
| @mkuznyetsov  | [che](https://github.com/eclipse/che/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release) | [che.core](https://search.maven.org/search?q=che.core),<br/> [`eclipse/che-server`](https://quay.io/eclipse/che-server),<br/>[`eclipse/che-endpoint-watcher`](https://quay.io/eclipse/che-endpoint-watcher),<br/> [`eclipse/che-keycloak`](https://quay.io/eclipse/che-keycloak),<br/> [`eclipse/che-postgres`](https://quay.io/eclipse/che-postgres),<br/> [`eclipse/che-server`](https://quay.io/eclipse/che-server),<br/> [`eclipse/che-e2e`](https://quay.io/eclipse/che-e2e) |
| @tolusha| [che-operator](https://github.com/eclipse/che-operator/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-operator/blob/master/make-release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release) | [`eclipse/che-operator`](https://quay.io/eclipse/che-operator)| 
| @tolusha| [chectl](https://github.com/che-incubator/chectl/blob/master/RELEASE.md) | [make-release.sh](https://github.com/che-incubator/chectl/blob/master/make-release.sh) | [travis](https://travis-ci.org/che-incubator/chectl) | [chectl releases](https://github.com/che-incubator/chectl/releases)
