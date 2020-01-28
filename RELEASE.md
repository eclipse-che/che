# Eclipse Che release process

##### 0. Create new issue for the release

Use this template, eg., for VERSION = 7.8.0 and BRANCH = 7.8.x:

<!-- 
RELEASE-TEMPLATE-BEGIN

### Release status
| Owner | Process | Script | Artifact(s) |
| --- | --- | --- | --- |
| <ul><li>[ ] @azatsarynnyy</li></ul>| [che-theia](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che-theia/blob/master/RELEASE.md) | `quay.io/eclipse/che-theia` |
| <ul><li>[ ] @nickboldt</li></ul>| [che-machine-exec](https://github.com/eclipse/che-machine-exec/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-machine-exec/blob/master/make-release.sh) | `quay.io/eclipse/che-machine-exec` | 
| <ul><li>[ ] @ibuziuk / @nickboldt</li></ul>| che-plugin-registry | ***MANUAL*** | `quay.io/eclipse/che-plugin-registry` | 
| | | | <ul><li>[ ] Copy nightly/next versions of che-theia & machine-exec as `$VERSION` to master and `$BRANCH` branches</li></ul>| 
| <ul><li>[ ] @nickboldt</li></ul>| [che-devfile-registry](https://github.com/eclipse/che-devfile-registry/blob/master/RELEASE.md) | [RELEASE.sh](https://github.com/eclipse/che-devfile-registry/blob/master/RELEASE.sh) | `quay.io/eclipse/che-devfile-registry` | 
| <ul><li>[ ] @vparfonov / @mkuznets</li></ul>| [che-parent](https://github.com/eclipse/che/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che/blob/master/RELEASE.md) | 
| <ul><li>[ ] @vparfonov / @mkuznets</li></ul>| [che-docs](https://github.com/eclipse/che/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che/blob/master/RELEASE.md) | 
| <ul><li>[ ] @vparfonov / @mkuznets</li></ul>| [che](https://github.com/eclipse/che/blob/master/RELEASE.md) | [***MANUAL***](https://github.com/eclipse/che/blob/master/RELEASE.md) | 
| <ul><li>[ ] @davidfestal</li></ul>| [che-operator](https://github.com/eclipse/che-operator/blob/master/RELEASE.md) | [make-release.sh](https://github.com/eclipse/che-operator/blob/master/make-release.sh) | `quay.io/eclipse/che-operator` | 
| | | | Community operator PRs: <ul><li>[ ] Kubernetes</li><li>[ ] OpenShift</li></ul>| 
| <ul><li>[ ] @tolusha</li></ul>| [chectl](https://github.com/che-incubator/chectl/blob/master/RELEASE.md) | [make-release.sh](https://github.com/che-incubator/chectl/blob/master/make-release.sh) |

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
* `git checkout release`
* `git merge -X theirs {branchname}`
* `git push -f`
##### 6. Close/release repository on Nexus 
 https://oss.sonatype.org/#stagingRepositories

 > **Note:** For bugfix release procedure will be similar except creating new branch on first step and update version in master branch. 
