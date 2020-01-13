# Eclipse Che release process

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
