# Eclipse Che release process

##### 0. Create new issue for the release

Use this template, eg., for VERSION = 7.8.0 and BRANCH = 7.8.x:

<!--
RELEASE-TEMPLATE-BEGIN

### List of pending issues / PRs

- [ ] *(add blockers here)*

### Release status

In parallel, the following releases can be done:

* che-theia,
* che-machine-exec,
* che-devfile registry, then che-plugin-registry (once che-theia and machine-exec are done)
* che-parent, then che-docs, then che as Release Candidate for QE

Then in series:

* che-parent, then che-docs, then che as Release
* che-operator, then chectl

- [ ] che-theia
  - [ ] _specific version of theia used: *(tag or sha)*_
- [ ] che-machine-exec
- [ ] che-plugin-registry _(depends on che-theia, che-machine-exec)_
- [ ] che-devfile-registry
- [ ] che-parent
- [ ] che-docs _(depends on parent)_
- [ ] che _(depends on docs and parent)_
- [ ] che-operator _(depends on all of the above)_
  - [ ] Kubernetes community operator PR
  - [ ] OpenShift community operator PR
- [ ] chectl _(depends on all of the above)_
- [ ] complete current milestone
  - [ ] move incomplete *deferred* issues to backlog
  - [ ] move incomplete *WIP* issues to next milestone
  - [ ] close completed issues
  - [ ] close milestone

| Owner | Process | Script | CI | Artifact(s) |
| --- | --- | --- | --- | --- |
| @azatsarynnyy| [che-theia](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | [centos](https://ci.centos.org/job/devtools-che-theia-che-release/) | [`quay.io/eclipse/che-theia`](https://quay.io/eclipse/che-theia) |
| @nickboldt| [che-machine-exec](https://github.com/eclipse/che-machine-exec/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-machine-exec/blob/master/make-release.sh) | [centos](https://ci.centos.org/job/devtools-che-machine-exec-release/) | [`quay.io/eclipse/che-machine-exec`](https://quay.io/eclipse/che-machine-exec)| 
| @nickboldt| [che-devfile-registry](https://github.com/eclipse/che-devfile-registry/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-devfile-registry/blob/master/make-release.sh) | [centos](https://ci.centos.org/job/devtools-che-devfile-registry-release/) | [`quay.io/eclipse/che-devfile-registry`](https://quay.io/eclipse/che-devfile-registry)| 
| @nickboldt / @ericwill| [che-plugin-registry](https://github.com/eclipse/che-plugin-registry/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-plugin-registry/blob/master/make-release.sh) | [centos](https://ci.centos.org/job/devtools-che-plugin-registry-release/) | [`quay.io/eclipse/che-plugin-registry`](https://quay.io/eclipse/che-plugin-registry)| 
| @vparfonov / @mkuznyetsov| [che-parent](https://github.com/eclipse/che/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che/blob/master/RELEASE.md) | [centos](https://ci.centos.org/job/devtools-che-parent-che-release/) | [che-parent](https://search.maven.org/search?q=a:che-parent), [che.depmgt](https://search.maven.org/artifact/org.eclipse.che.depmgt/maven-depmgt-pom) |
| @vparfonov / @mkuznyetsov| [che-docs](https://github.com/eclipse/che/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che/blob/master/RELEASE.md) | [centos](https://ci.centos.org/job/devtools-che-docs-che-release/) | [che-docs](https://search.maven.org/search?q=a:che-docs)
| @vparfonov / @mkuznyetsov| [che](https://github.com/eclipse/che/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che/blob/master/RELEASE.md) | [centos](https://ci.centos.org/job/devtools-che-che-release/) | [che.core](https://search.maven.org/search?q=che.core), [`quay.io/eclipse/che-server`](https://quay.io/eclipse/che-server) |
| @tolusha| [che-operator](https://github.com/eclipse/che-operator/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-operator/blob/master/make-release.sh) | ? | [`quay.io/eclipse/che-operator`](https://quay.io/eclipse/che-operator)| 
| @tolusha| [chectl](https://github.com/che-incubator/chectl/blob/master/RELEASE.md) | [make-release.sh](https://github.com/che-incubator/chectl/blob/master/make-release.sh) | [travis](https://travis-ci.org/che-incubator/chectl) | [chectl releases](https://github.com/che-incubator/chectl/releases)

RELEASE-TEMPLATE-END
-->

##### 1. Create branch for release preparation and next bugfixes:
* `git branch {branchname} #e.g 7.7.x`
* `git push --set-upstream origin {branchname}`
##### 2. Create PR for switch master to the next development version :
* `git branch set_next_version_in_master_{next_version} #e.g 7.8.0-SNAPSHOT`
* Update parent version : `mvn versions:update-parent  versions:commit -DallowSnapshots=true -DparentVersion={next_version}`
* Update dependencies: `sed -i -e "s#{version_old}#{next_version}#" pom.xml`
* `git commit`
* `git push --set-upstream origin set_next_version_in_master_{next_version}`
* Create PR
##### 3. In release branch of Che need to set up released version of plugin registry and plugins:
    1 Update deploy_che.sh (should be deprecated soon - https://github.com/eclipse/che/issues/14069) default environment variables
    2 Update default version of plugins and editors in che.properties to released tag e.g `7.7.0`
    3 Update Helm charts with released tag e.g `7.7.0`
    
  To do this execute commands:
  * `cd .ci`
  * on Linux machine: `set_tag_version_images_linux.sh {tag}` 
  * on MacOS: `set_tag_version_images_macos.sh {tag}`
  * `git commit` 
  * `git push`
##### 4. Start pre-release testing.
##### 5. If pre-release test passed need to merge branch to the `release` branch and push changes, release process will start by webhook:
* Set released parent version
* Update dependencies in pom.xml 
* `git checkout release`
* `git merge -X theirs {branchname}`
* `git push -f`
##### 6. Close/release repository on Nexus 
 https://oss.sonatype.org/#stagingRepositories

 > **Note:** For bugfix release procedure will be similar except creating new branch on first step and update version in master branch. 

# Script
`make-release.sh` is a script that performs these actions, use --prerelease-testing flag to prepare "RC" release for QE testing, and --trigger-release to perform a release after that