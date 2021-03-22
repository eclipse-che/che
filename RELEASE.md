# Eclipse Che release process

##### 0. Create new issue for the release

Use this template, eg., for VERSION = 7.8.0 and BRANCH = 7.8.x:

<!--
RELEASE-TEMPLATE-BEGIN

### List of pending issues / PRs
* [ ] (add items)

### Release status

In series, the following will be released via [che-release](https://github.com/eclipse/che-release/blob/master/cico_release.sh):

* che-theia, che-machine-exec, che-devfile registry,
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
- [ ] community operator PRs _(depends on all of the above)_
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

##### 1. Create branch for release preparation and next bugfixes:

```
git branch {branchname} # e.g 7.7.x
git push --set-upstream origin {branchname}
```

##### 2. Create PR for switch master to the next development version:

```
  git branch set_next_version_in_master_{next_version} # e.g 7.8.0-SNAPSHOT
  # Update parent version
  mvn versions:update-parent  versions:commit -DallowSnapshots=true -DparentVersion={next_version}

  # Update dependencies
  sed -i -e "s#{version_old}#{next_version}#" pom.xml
  git commit
  git push --set-upstream origin set_next_version_in_master_{next_version}
  ```

* Create PR
##### 3. In release branch of Che need to set up released version of plugin registry and plugins:
    1 Update deploy_che.sh (should be deprecated soon - https://github.com/eclipse/che/issues/14069) default environment variables
    2 Update default version of plugins and editors in che.properties to released tag e.g `7.7.0`
    3 Update Helm charts with released tag e.g `7.7.0`

  To do this execute commands:
  ```
  cd .ci
  set_tag_version_images.sh 7.25.0
  git commit
  git push
  ```
##### 4. Start pre-release testing.
##### 5. If pre-release test passed need to merge branch to the `release` branch and push changes, release process will start by webhook:
* Set released parent version
* Update dependencies in pom.xml, then:
  ```
  git checkout release
  git merge -X theirs {branchname}
  git push -f
  ```

##### 6. Close/release repository on Nexus
 https://oss.sonatype.org/#stagingRepositories

 > **Note:** For bugfix release procedure will be similar except creating new branch on first step and update version in master branch.

# Script
`make-release.sh` is a script that performs these actions, use --prerelease-testing flag to prepare "RC" release for QE testing, and --trigger-release to perform a release after that
