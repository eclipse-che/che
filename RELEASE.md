# Eclipse Che release process

##### 0. Create new issue for the release

Use this template, eg., for VERSION = 7.8.0 and BRANCH = 7.8.x:

<!--
RELEASE-TEMPLATE-BEGIN

### List of pending issues / PRs
* [ ] (add items)

### Release status

In series, the following will be released via [che-release](https://github.com/eclipse/che-release/blob/master/cico_release.sh):

* che-theia, che-machine-exec, che-devfile registry
* che-plugin-registry (once che-theia and machine-exec are done)
* che-parent, che-dashboard
* che (server assembly, with maven artifacts + containers)

Then manually:

* release che server in Maven Central

Then by script:

* che-operator
* generation of OperatorHub PRs
* chectl

- [ ] che-theia, che-machine-exec, che-devfile-registry, che-plugin-registry
- [ ] che-parent, che-dashboard
- [ ] che
- [ ] che-operator _(depends on all of the above)_
- [ ] chectl _(depends on all of the above)_
- [ ] [community operator PRs](https://github.com/operator-framework/community-operators/pulls/che-incubator-bot) _(depends on all of the above)
  - [ ] https://github.com/operator-framework/community-operators/pull/ ???
  - [ ] https://github.com/operator-framework/community-operators/pull/ ???

If this is a .0 release:

- [ ] complete current milestone
  - [ ] move incomplete *deferred* issues to backlog
  - [ ] move incomplete *WIP* issues to next milestone
  - [ ] close completed issues
  - [ ] close milestone

| Owner | Process | Script | CI | Artifact(s) + Container(s) |
| --- | --- | --- | --- | --- |
| @mkuznyetsov  | [che-theia](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-theia-che-release/) | [`eclipse/che-theia`](https://quay.io/eclipse/che-theia) |
| @mkuznyetsov  | [che-machine-exec](https://github.com/eclipse-che/che-machine-exec/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-machine-exec-release/) | [`eclipse/che-machine-exec`](https://quay.io/eclipse/che-machine-exec)|
| @mkuznyetsov  | [che-plugin-registry](https://github.com/eclipse/che-plugin-registry/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-plugin-registry-release/) | [`eclipse/che-plugin-registry`](https://quay.io/eclipse/che-plugin-registry)|
| @mkuznyetsov  | [che-devfile-registry](https://github.com/eclipse/che-devfile-registry/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-devfile-registry-release/) | [`eclipse/che-devfile-registry`](https://quay.io/eclipse/che-devfile-registry)|
| @mkuznyetsov  | [che-parent](https://github.com/eclipse/che/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release) | [che-parent](https://search.maven.org/search?q=a:che-parent) |
| @mkuznyetsov  | [che-dashboard](https://github.com/eclipse/che-dashboard/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release/) | [`che-dashboard`](https://quay.io/repository/eclipse/che-dashboard?tag=next&tab=tags) |
| @mkuznyetsov  | [che](https://github.com/eclipse/che/blob/master/RELEASE.md) | [cico_release.sh](https://github.com/eclipse/che-release/blob/master/cico_release.sh) | [centos](https://ci.centos.org/job/devtools-che-release-che-release) | [che.core](https://search.maven.org/search?q=che.core), [che.server](https://mvnrepository.com/artifact/org.eclipse.che/che-server)<br/> [`eclipse/che-server`](https://quay.io/eclipse/che-server),<br/>[`eclipse/che-endpoint-watcher`](https://quay.io/eclipse/che-endpoint-watcher),<br/> [`eclipse/che-keycloak`](https://quay.io/eclipse/che-keycloak),<br/> [`eclipse/che-postgres`](https://quay.io/eclipse/che-postgres),<br/> [`eclipse/che-server`](https://quay.io/eclipse/che-server),<br/> [`eclipse/che-e2e`](https://quay.io/eclipse/che-e2e) |
| @tolusha| [che-operator](https://github.com/eclipse-che/che-operator/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse-che/che-operator/blob/master/make-release.sh) | ? | [`eclipse/che-operator`](https://quay.io/eclipse/che-operator)|
| @tolusha| [chectl](https://github.com/che-incubator/chectl/blob/master/RELEASE.md) | [make-release.sh](https://github.com/che-incubator/chectl/blob/master/make-release.sh) | [travis](https://travis-ci.org/che-incubator/chectl) | [chectl releases](https://github.com/che-incubator/chectl/releases)

RELEASE-TEMPLATE-END
-->

# Automated release workflow
Release is performed with GitHub Actions workflow [release.yml](https://github.com/eclipse/che/actions/workflows/release.yml).
It is also used to release [Che Parent](https://github.com/eclipse/che-parent), if appropriate input paramater `releaseParent` is set to true.
The release will perform the build and deployment of Maven Artifacts, build and push of all nesessary docker images, and bumping up development version.

[make-release.sh](https://github.com/eclipse/che/blob/master/make-release.sh) is the script that can be used for standalone release outside of GitHub Actions. However, ensure that all environment variables are set in place before invoking `./make-release.sh`, similarly to how it is outlined in [release.yml](https://github.com/eclipse/che/actions/workflows/release.yml):

RELEASE_CHE_PARENT - if `true`, will perform the release of Che Parent as well.
VERSION_CHE_PARENT - if RELEASE_CHE_PARENT is `true`, here the version of Che Parent must be provided.
DEPLOY_TO_NEXUS - if `true`, will deploy Maven artifacts to Nexus. Set `false`, if this step needs to be skipped.
AUTORELEASE_ON_NEXUS - if `true`, after deploying Maven artifacts to staging repository, they will be automatically closed and release. Set `false`, if you want to inspect the staging repository, before artifacts will be released.
REBUILD_FROM_EXISTING_TAGS - if `true`, release will not create new tag, but instead checkout to existing one. Use this to rerun failed attempts, without having to recreate the tag.
BUILD_AND_PUSH_IMAGES - if `true`, will build all asociated images in [dockerfiles](https://github.com/eclipse/che/tree/master/dockerfiles) directory. Set `false`, if this step needs to be skipped.
BUMP_NEXT_VERSION - if `true`, will increase the development versions in main and bugfix branches. Set false, if this step needs to be skipped


