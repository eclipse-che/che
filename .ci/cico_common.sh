#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Just a script to get and build eclipse-che locally
# please send PRs to github.com/kbsingh/build-run-che

# update machine, get required deps in place
# this script assumes its being run on CentOS Linux 7/x86_64


function die_with() {
	echo "$*" >&2
	exit 1
}

function getCurrentVersion() {
    echo $(scl enable rh-maven33 "mvn help:evaluate -Dexpression=project.version -q -DforceStdout")
}

function getReleaseVersion() {
    echo "$(echo $1 | cut -d'-' -f1)" #cut SNAPSHOT form the version name
}

function setReleaseVersionInMavenProject(){
    scl enable rh-maven33 "mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$1"
}

load_jenkins_vars() {
    set +x
    eval "$(./env-toolkit load -f jenkins-env.json \
                              CHE_BOT_GITHUB_TOKEN \
                              CHE_MAVEN_SETTINGS \
                              CHE_GITHUB_SSH_KEY \
                              CHE_OSS_SONATYPE_GPG_KEY \
                              CHE_OSS_SONATYPE_PASSPHRASE \
                              QUAY_ECLIPSE_CHE_USERNAME \
                              QUAY_ECLIPSE_CHE_PASSWORD)"
}

load_mvn_settings_gpg_key() {
    set +x
    mkdir $HOME/.m2
    #prepare settings.xml for maven and sonatype (central maven repository)
    echo $CHE_MAVEN_SETTINGS | base64 -d > $HOME/.m2/settings.xml
    #load GPG key for sign artifacts
    echo $CHE_OSS_SONATYPE_GPG_KEY | base64 -d > $HOME/.m2/gpg.key
    #load SSH key for release process
    echo ${#CHE_OSS_SONATYPE_GPG_KEY}
    echo $CHE_GITHUB_SSH_KEY | base64 -d > $HOME/.ssh/id_rsa
    chmod 0400 $HOME/.ssh/id_rsa
    ssh-keyscan github.com >> ~/.ssh/known_hosts
    set -x
    gpg --import $HOME/.m2/gpg.key
}

setup_gitconfig() {
  set +x
  git config --global github.user che-bot
  git config --global github.token $CHE_BOT_GITHUB_TOKEN

}

install_deps(){
    set +x
    yum -y update
    yum -y install centos-release-scl-rh java-1.8.0-openjdk-devel git 
    yum -y install rh-maven33
    yum install -y yum-utils device-mapper-persistent-data lvm2
    yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    curl -sL https://rpm.nodesource.com/setup_10.x | bash -
    yum-config-manager --add-repo https://dl.yarnpkg.com/rpm/yarn.repo
    yum install -y docker-ce nodejs yarn gcc-c++ make
    service docker start
}

build_and_deploy_artifacts() {
    set -x
    scl enable rh-maven33 'mvn clean install -U -Pintegration'
    if [ $? -eq 0 ]; then
        echo 'Build Success!'
        echo 'Going to deploy artifacts'
        scl enable rh-maven33 "mvn clean deploy -DcreateChecksum=true  -Dgpg.passphrase=$CHE_OSS_SONATYPE_PASSPHRASE"
    else
        echo 'Build Failed!'
        exit 1
    fi
}

gitHttps2ssh(){
    #git remote set-url origin git@github.com:$(git remote get-url origin | sed 's/https:\/\/github.com\///' | sed 's/git@github.com://')
    #git version 1.8.3 not support get-url sub-command so hardcode url
    git remote set-url origin git@github.com:eclipse/che-parent
}


setup_gitconfig() {
  #git config --global github.user che-bot
  #git config --global github.token $CHE_BOT_GITHUB_TOKEN
  git config --global user.name "Vitalii Parfonov"
  git config --global user.email vparfono@redhat.com
}

releaseProject() {
    set -x
    git checkout -f release
    curVer=$(getCurrentVersion)
    echo ">>>>>>>> $curVer"
    tag=$(getReleaseVersion $curVer)
    echo ">>>>>>>>>> $tag"
    setReleaseVersionInMavenProject $tag
    git commit -asm "Release version ${tag}"
    build_and_deploy_artifacts
    git tag "${tag}" || die_with "Failed to create tag ${tag}! Release has been deployed, however"
    git push --tags ||  die_with "Failed to push tags. Please do this manually"
    publishImagesOnQuayLatest
    exit 0
}


publishImagesOnQuay() {

    echo "Going to build and push docker images"
    set -e
    set -o pipefail

    REGISTRY="quay.io"
    ORGANIZATION="eclipse"
    # For pushing to quay.io 'eclipse' organization we need to use different credentials
    QUAY_USERNAME=${QUAY_ECLIPSE_CHE_USERNAME}
    QUAY_PASSWORD=${QUAY_ECLIPSE_CHE_PASSWORD}
    if [ -n "${QUAY_USERNAME}" ] && [ -n "${QUAY_PASSWORD}" ]; then
        docker login -u "${QUAY_USERNAME}" -p "${QUAY_PASSWORD}" "${REGISTRY}"
    else
      echo "Could not login, missing credentials for pushing to the '${ORGANIZATION}' organization"
      return
    fi

    # stop / rm all containers
    if [[ $(docker ps -aq) != "" ]];then
        docker rm -f $(docker ps -aq)
    fi

    # KEEP RIGHT ORDER!!!
    DOCKER_FILES_LOCATIONS=(
    dockerfiles/endpoint-watcher
    dockerfiles/keycloak
    dockerfiles/postgres
    dockerfiles/dev
    dockerfiles/che
    dockerfiles/dashboard-dev
    dockerfiles/e2e
    )

    IMAGES_LIST=(
    eclipse/che-endpoint-watcher
    eclipse/che-keycloak
    eclipse/che-postgres
    eclipse/che-dev
    eclipse/che-server
    eclipse/che-dashboard-dev
    eclipse/che-e2e
    )


    # BUILD IMAGES
    for image_dir in ${DOCKER_FILES_LOCATIONS[@]}
     do
         bash $(pwd)/$image_dir/build.sh
         if [ $image_dir == "dockerfiles/che" ]; then
           #CENTOS SINGLE USER
           BUILD_ASSEMBLY_DIR=$(echo assembly/assembly-main/target/eclipse-che-*/eclipse-che-*/)
           LOCAL_ASSEMBLY_DIR="$image_dir/eclipse-che"
           if [ -d "${LOCAL_ASSEMBLY_DIR}" ]; then
               rm -r "${LOCAL_ASSEMBLY_DIR}"
           fi
           cp -r "${BUILD_ASSEMBLY_DIR}" "${LOCAL_ASSEMBLY_DIR}"
           docker build -t ${ORGANIZATION}/che-server:nightly-centos -f $(pwd)/$image_dir/Dockerfile.centos $(pwd)/$image_dir/
         fi
         if [ $? -ne 0 ]; then
           echo "ERROR:"
           echo "build of '$image_dir' image is failed!"
           exit 1
         fi
     done

    #PUSH IMAGES
    for image in ${IMAGES_LIST[@]}
     do
         docker tag "${image}:nightly" "${REGISTRY}/${image}:nightly"
         echo y | docker push "${REGISTRY}/${image}:nightly"
         if [ $image == "${ORGANIZATION}/che-server" ]; then
           docker tag "${image}:nightly" "${REGISTRY}/${image}:nightly-centos"
           echo y | docker push "${REGISTRY}/${ORGANIZATION}/che-server:nightly-centos"
         fi
         if [ $? -ne 0 ]; then
           echo "ERROR:"
           echo "docker push of '$image' image is failed!"
           exit 1
         fi
     done

}

publishImagesOnQuayLatest() {

    echo "Going to build and push docker images"
    set -e
    set -o pipefail

    TAG = $1
    git checkout ${TAG}
    REGISTRY="quay.io"
    ORGANIZATION="eclipse"
    # For pushing to quay.io 'eclipse' organization we need to use different credentials
    QUAY_USERNAME=${QUAY_ECLIPSE_CHE_USERNAME}
    QUAY_PASSWORD=${QUAY_ECLIPSE_CHE_PASSWORD}
    if [ -n "${QUAY_USERNAME}" ] && [ -n "${QUAY_PASSWORD}" ]; then
        docker login -u "${QUAY_USERNAME}" -p "${QUAY_PASSWORD}" "${REGISTRY}"
    else
      echo "Could not login, missing credentials for pushing to the '${ORGANIZATION}' organization"
      return
    fi

    # stop / rm all containers
    if [[ $(docker ps -aq) != "" ]];then
        docker rm -f $(docker ps -aq)
    fi

    # KEEP RIGHT ORDER!!!
    DOCKER_FILES_LOCATIONS=(
    dockerfiles/endpoint-watcher
    dockerfiles/keycloak
    dockerfiles/postgres
    dockerfiles/dev
    dockerfiles/che
    dockerfiles/dashboard-dev
    dockerfiles/e2e
    )

    IMAGES_LIST=(
    eclipse/che-endpoint-watcher
    eclipse/che-keycloak
    eclipse/che-postgres
    eclipse/che-dev
    eclipse/che-server
    eclipse/che-dashboard-dev
    eclipse/che-e2e
    )


    # BUILD IMAGES
    for image_dir in ${DOCKER_FILES_LOCATIONS[@]}
     do
         bash $(pwd)/$image_dir/build.sh
         if [ $image_dir == "dockerfiles/che" ]; then
           #CENTOS SINGLE USER
           BUILD_ASSEMBLY_DIR=$(echo assembly/assembly-main/target/eclipse-che-*/eclipse-che-*/)
           LOCAL_ASSEMBLY_DIR="$image_dir/eclipse-che"
           if [ -d "${LOCAL_ASSEMBLY_DIR}" ]; then
               rm -r "${LOCAL_ASSEMBLY_DIR}"
           fi
           cp -r "${BUILD_ASSEMBLY_DIR}" "${LOCAL_ASSEMBLY_DIR}"
           docker build -t ${ORGANIZATION}/che-server:${TAG}-centos -f $(pwd)/$image_dir/Dockerfile.centos $(pwd)/$image_dir/
         fi
         if [ $? -ne 0 ]; then
           echo "ERROR:"
           echo "build of '$image_dir' image is failed!"
           exit 1
         fi
     done

    #PUSH IMAGES
    for image in ${IMAGES_LIST[@]}
     do
         docker tag "${image}:${TAG}" "${REGISTRY}/${image}:${TAG}"
         docker tag "${image}:${TAG}" "${REGISTRY}/${image}:latest"
         echo y | docker push "${REGISTRY}/${image}:${TAG}"
         echo y | docker push "${REGISTRY}/${image}:latest"
         if [ $image == "${ORGANIZATION}/che-server" ]; then
           docker tag "${image}:${TAG}" "${REGISTRY}/${image}:${TAG}-centos"
           docker tag "${image}:${TAG}" "${REGISTRY}/${image}:latest-centos"
           echo y | docker push "${REGISTRY}/${ORGANIZATION}/che-server:${TAG}-centos"
           echo y | docker push "${REGISTRY}/${ORGANIZATION}/che-server:latest-centos"
         fi
         if [ $? -ne 0 ]; then
           echo "ERROR:"
           echo "docker push of '$image' image is failed!"
           exit 1
         fi
     done

}
