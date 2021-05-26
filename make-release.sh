#!/bin/bash
# Release process automation script. 
# Used to create branch/tag, update versions in pom.xml
# build and push maven artifacts and docker images to Quay.io

REGISTRY="quay.io"
ORGANIZATION="eclipse"

 # KEEP RIGHT ORDER!!!
DOCKER_FILES_LOCATIONS=(
    che/dockerfiles/endpoint-watcher
    che/dockerfiles/keycloak
    che/dockerfiles/postgres
    che/dockerfiles/dev
    che/dockerfiles/che
)

IMAGES_LIST=(
    quay.io/eclipse/che-endpoint-watcher
    quay.io/eclipse/che-keycloak
    quay.io/eclipse/che-postgres
    quay.io/eclipse/che-dev
    quay.io/eclipse/che-server
)

loadMvnSettingsGpgKey() {
    set +x
    mkdir $HOME/.m2
    #prepare settings.xml for maven and sonatype (central maven repository)
    echo $CHE_MAVEN_SETTINGS | base64 -d > $HOME/.m2/settings.xml 
    #load GPG key for sign artifacts
    echo $CHE_OSS_SONATYPE_GPG_KEY | base64 -d > $HOME/.m2/gpg.key
    #load SSH key for release process
    echo ${#CHE_OSS_SONATYPE_GPG_KEY}
    mkdir $HOME/.ssh/
    echo $CHE_GITHUB_SSH_KEY | base64 -d > $HOME/.ssh/id_rsa
    chmod 0400 $HOME/.ssh/id_rsa
    ssh-keyscan github.com >> ~/.ssh/known_hosts
    set -x
    export GPG_TTY=$(tty)
    gpg --import $HOME/.m2/gpg.key
    # gpg --import --batch $HOME/.m2/gpg.key
    gpg --version
}

installDebDeps(){
    set +x
    # TODO should this be node 12?
    curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
    sudo apt-get install -y nodejs
}

installMaven(){
    set -x
    mkdir -p /opt/apache-maven && curl -sSL https://downloads.apache.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz | tar -xz --strip=1 -C /opt/apache-maven
    export M2_HOME="/opt/apache-maven"
    export PATH="/opt/apache-maven/bin:${PATH}"
    mvn -version || die_with "mvn not found in path: ${PATH} !"
    set +x
}

evaluateCheVariables() {
    echo "Che version: ${CHE_VERSION}"
    # derive branch from version
    BRANCH=${CHE_VERSION%.*}.x
    echo "Branch: ${BRANCH}"

    if [[ ${CHE_VERSION} == *".0" ]]; then
        BASEBRANCH="main"
    else
        BASEBRANCH="${BRANCH}"
    fi
    echo "Basebranch: ${BASEBRANCH}" 
    echo "Release che-parent: ${RELEASE_CHE_PARENT}"
    echo "Version che-parent: ${VERSION_CHE_PARENT}"
}

checkoutProjects() {
    if [[ ${RELEASE_CHE_PARENT} = "true" ]]; then
        checkoutProject git@github.com:eclipse/che-parent
    fi
    checkoutProject git@github.com:eclipse-che/che-server
}

checkoutProject() {
    PROJECT="${1##*/}"
    echo "checking out project $PROJECT with ${BRANCH} branch"

    if [[ ! -d ${PROJECT} ]]; then
        echo "project not found in ${PROJECT} directory, performing 'git clone'"
        git clone $1
    fi

    cd $PROJECT
    git checkout ${BASEBRANCH}

    set -x
    set +e
    if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
        git branch "${BRANCH}" || git checkout "${BRANCH}" && git pull origin "${BRANCH}"
        git push origin "${BRANCH}"
        git fetch origin "${BRANCH}:${BRANCH}"
        git checkout "${BRANCH}"
    fi
    set -e
    set +x
    cd ..
}

checkoutTags() {
    if [[ ${RELEASE_CHE_PARENT} = "true" ]]; then
        cd che-parent
        git checkout ${CHE_VERSION}
        cd ..
    fi
    cd che
    git checkout ${CHE_VERSION}
    cd ..
}

# check for build errors, since we're using set +e above to NOT fail the build for Nexus problems
checkLogForErrors () {
    tmplog="$1"
    errors_in_log="$(grep -E "FAILURE \[|BUILD FAILURE|Failed to execute goal" $tmplog || true)"
    if [[ ${errors_in_log} ]]; then
        echo "${errors_in_log}"
        exit 1
    fi
}

# TODO change it to someone else?
setupGitconfig() {
  git config --global user.name "Mykhailo Kuznietsov"
  git config --global user.email mkuznets@redhat.com

  # hub CLI configuration
  git config --global push.default matching
  # replace default GITHUB_TOKEN, that is used by GitHub 
  export GITHUB_TOKEN="${CHE_BOT_GITHUB_TOKEN}"
}

commitChangeOrCreatePR() {
    set +e
    aVERSION="$1"
    aBRANCH="$2"
    PR_BRANCH="$3"

    COMMIT_MSG="[release] Bump to ${aVERSION} in ${aBRANCH}"

    # commit change into branch
    git commit -asm "${COMMIT_MSG}"
    git pull origin "${aBRANCH}"

    PUSH_TRY="$(git push origin "${aBRANCH}")"
    # shellcheck disable=SC2181
    if [[ $? -gt 0 ]] || [[ $PUSH_TRY == *"protected branch hook declined"* ]]; then
        # create pull request for main branch, as branch is restricted
        git branch "${PR_BRANCH}"
        git checkout "${PR_BRANCH}"
        git pull origin "${PR_BRANCH}"
        git push origin "${PR_BRANCH}"
        lastCommitComment="$(git log -1 --pretty=%B)"
        hub pull-request -f -m "${lastCommitComment}" -b "${aBRANCH}" -h "${PR_BRANCH}"
    fi
    set -e
}

createTags() {
    if [[ $RELEASE_CHE_PARENT = "true" ]]; then
        tagAndCommit che-parent
    fi
    tagAndCommit che
}

tagAndCommit() {
    cd $1
    # this branch isn't meant to be pushed
    git checkout -b release-${CHE_VERSION}
    git commit -asm "Release version ${CHE_VERSION}"
    if [ $(git tag -l "$CHE_VERSION") ]; then
        echo "tag ${CHE_VERSION} already exists! recreating ..."
        git tag -d ${CHE_VERSION}
        git push origin :${CHE_VERSION}
        git tag "${CHE_VERSION}"
    else
        echo "[INFO] creating new tag ${CHE_VERSION}"
        git tag "${CHE_VERSION}"
    fi
    git push --tags
    echo "[INFO] tag created and pushed for $1"
    cd ..
}

prepareRelease() {
    if [[ $RELEASE_CHE_PARENT = "true" ]]; then
        pushd che-parent >/dev/null
            # Install previous version, in case it is not available in central repo
            # which is needed for dependent projects
            mvn clean install
            mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${VERSION_CHE_PARENT}
            mvn clean install
        popd >/dev/null
        echo "[INFO] Che Parent version has been updated to ${VERSION_CHE_PARENT}"
    fi

    pushd che >/dev/null
        if [[ $RELEASE_CHE_PARENT = "true" ]]; then
            mvn versions:update-parent -DgenerateBackupPoms=false -DallowSnapshots=false -DparentVersion=[${VERSION_CHE_PARENT}]
        fi
        mvn versions:set -DgenerateBackupPoms=false -DallowSnapshots=false -DnewVersion=${CHE_VERSION}
        echo "[INFO] Che Server version has been updated to ${CHE_VERSION} (parentVersion = ${VERSION_CHE_PARENT})"

        # Replace dependencies in che-server parent
        sed -i -e "s#<che.version>.*<\/che.version>#<che.version>${CHE_VERSION}<\/che.version>#" pom.xml
        echo "[INFO] Dependencies updated in che-server parent"

        # TODO pull parent pom version from VERSION file, instead of being hardcoded
        pushd typescript-dto >/dev/null
            sed -i -e "s#<che.version>.*<\/che.version>#<che.version>${CHE_VERSION}<\/che.version>#" dto-pom.xml
            sed -i -e "/<groupId>org.eclipse.che.parent<\/groupId>/ { n; s#<version>.*<\/version>#<version>${VERSION_CHE_PARENT}<\/version>#}" dto-pom.xml
            echo "[INFO] Dependencies updated in che typescript DTO (parent = ${VERSION_CHE_PARENT}, che server = ${CHE_VERSION})"
        popd >/dev/null

        # TODO more elegant way to execute these scripts
        pushd .ci >/dev/null
            ./set_tag_version_images.sh ${CHE_VERSION}
            echo "[INFO] Tag versions of images have been set in che-server"
        popd >/dev/null
    popd >/dev/null
}

releaseCheServer() {
    set -x
    tmpmvnlog=/tmp/mvn.log.txt
    if [[ $RELEASE_CHE_PARENT = "true" ]]; then
        pushd che-parent >/dev/null
        rm -f $tmpmvnlog || true
        set +e
        mvn clean install -ntp -U -Pcodenvy-release -Dgpg.passphrase=$CHE_OSS_SONATYPE_PASSPHRASE | tee $tmpmvnlog
        EXIT_CODE=$?
        set -e
        # try maven build again if Nexus dies
        if grep -q -E "502 - Bad Gateway|Nexus connection problem" $tmpmvnlog; then
            rm -f $tmpmvnlog || true
            mvn clean install -ntp -U -Pcodenvy-release -Dgpg.passphrase=$CHE_OSS_SONATYPE_PASSPHRASE | tee $tmpmvnlog
            EXIT_CODE=$?
        fi
        # check log for errors if build successful; if failed, no need to check (already failed)
        if [ $EXIT_CODE -eq 0 ]; then
            checkLogForErrors $tmpmvnlog
            echo 'Build of che-parent: Success!'
        else
            echo '[ERROR] 2. Build of che-parent: Failed!'
            exit $EXIT_CODE
        fi
        popd >/dev/null
    fi

    pushd che >/dev/null
    rm -f $tmpmvnlog || true
    set +e
    mvn clean install -U -Pcodenvy-release -Dgpg.passphrase=$CHE_OSS_SONATYPE_PASSPHRASE | tee $tmpmvnlog
    EXIT_CODE=$?
    set -e
    # try maven build again if Nexus dies
    if grep -q -E "502 - Bad Gateway|Nexus connection problem" $tmpmvnlog; then
        rm -f $tmpmvnlog || true
        mvn clean install -U -Pcodenvy-release -Dgpg.passphrase=$CHE_OSS_SONATYPE_PASSPHRASE | tee $tmpmvnlog
        EXIT_CODE=$?
    fi

    # check log for errors if build successful; if failed, no need to check (already failed)
    if [ $EXIT_CODE -eq 0 ]; then
        checkLogForErrors $tmpmvnlog
        echo 'Build of che-server: Success!'
    else
        echo '[ERROR] 2. Build of che-server: Failed!'
        exit $EXIT_CODE
    fi
    set +x
    popd >/dev/null
}

releaseTypescriptDto() {
    pushd che/typescript-dto >/dev/null
    ./build.sh
    popd >/dev/null
}

buildImages() {
    echo "Going to build docker images"
    set -e
    set -o pipefail
    TAG=$1
  
    # stop / rm all containers
    if [[ $(docker ps -aq) != "" ]];then
        docker rm -f "$(docker ps -aq)"
    fi

    # BUILD IMAGES
    for image_dir in ${DOCKER_FILES_LOCATIONS[@]}
      do
        bash "$(pwd)/${image_dir}/build.sh" --tag:${TAG}
        if [[ $? -ne 0 ]]; then
           echo "ERROR:"
           echo "build of '${image_dir}' image is failed!"
           exit 1
        fi
      done
}

tagLatestImages() {
    for image in ${IMAGES_LIST[@]}
     do
         echo y | docker tag "${image}:$1" "${image}:latest"
         if [[ $? -ne 0 ]]; then
           die_with  "docker tag of '${image}' image is failed!"
         fi
     done
}

pushImagesOnQuay() {
    #PUSH IMAGES
    for image in ${IMAGES_LIST[@]}
        do
            echo y | docker push "${image}:$1"
            if [[ $2 == "pushLatest" ]]; then
                echo y | docker push "${image}:latest"
            fi
            if [[ $? -ne 0 ]]; then
            die_with  "docker push of '${image}' image is failed!"
            fi
        done
}

bumpVersions() {
    # infer project version + commit change into ${BASEBRANCH} branch
    echo "${BASEBRANCH} ${BRANCH}"
    if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
        # bump the y digit
        [[ ${BRANCH} =~ ^([0-9]+)\.([0-9]+)\.x ]] && BASE=${BASH_REMATCH[1]}; NEXT=${BASH_REMATCH[2]}; (( NEXT=NEXT+1 )) # for BRANCH=7.10.x, get BASE=7, NEXT=11
        NEXTVERSION_Y="${BASE}.${NEXT}.0-SNAPSHOT"
        bumpVersion ${NEXTVERSION_Y} ${BASEBRANCH}
    fi
    # bump the z digit
    [[ ${CHE_VERSION} =~ ^([0-9]+)\.([0-9]+)\.([0-9]+) ]] && BASE="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}"; NEXT="${BASH_REMATCH[3]}"; (( NEXT=NEXT+1 )) # for VERSION=7.7.1, get BASE=7.7, NEXT=2
    NEXTVERSION_Z="${BASE}.${NEXT}-SNAPSHOT"
    bumpVersion ${NEXTVERSION_Z} ${BRANCH}
}

bumpVersion() {
    set -x
    echo "[info]bumping to version $1 in branch $2"

    if [[ $RELEASE_CHE_PARENT = "true" ]]; then
        pushd che-parent >/dev/null
        git checkout $2
        #install previous version, in case it is not available in central repo
        #which is needed for dependent projects
        
        mvn clean install
        mvn versions:set -DgenerateBackupPoms=false -DnewVersion=${CHE_VERSION}
        mvn clean install
        commitChangeOrCreatePR ${CHE_VERSION} $2 "pr-${2}-to-${1}"
        popd >/dev/null
    fi

    pushd che >/dev/null
        git checkout $2
        if [[ $RELEASE_CHE_PARENT = "true" ]]; then
            mvn versions:update-parent -DgenerateBackupPoms=false -DallowSnapshots=true -DparentVersion=[${VERSION_CHE_PARENT}]
        fi
        mvn versions:set -DgenerateBackupPoms=false -DallowSnapshots=true -DnewVersion=$1
        sed -i -e "s#<che.version>.*<\/che.version>#<che.version>$1<\/che.version>#" pom.xml
        pushd typescript-dto >/dev/null
            sed -i -e "s#<che.version>.*<\/che.version>#<che.version>${1}<\/che.version>#" dto-pom.xml
            sed -i -e "/<groupId>org.eclipse.che.parent<\/groupId>/ { n; s#<version>.*<\/version>#<version>${VERSION_CHE_PARENT}<\/version>#}" dto-pom.xml
        popd >/dev/null

        commitChangeOrCreatePR $1 $2 "pr-${2}-to-${1}"
    popd >/dev/null
    set +x
}

updateImageTagsInCheServer() {
    cd che
    git checkout ${BRANCH}
    cd .ci
    ./set_tag_version_images.sh ${CHE_VERSION}
    cd ..
    git commit -asm "Set ${CHE_VERSION} release image tags"
    git push origin ${BRANCH}
}

installMaven
loadMvnSettingsGpgKey
installDebDeps
set -x
setupGitconfig

evaluateCheVariables

checkoutProjects

if [[ "${BUMP_NEXT_VERSION}" = "true" ]]; then
    bumpVersions
    updateImageTagsInCheServer
    # checkout back to branches to make release from
    checkoutProjects
fi

if [[ "${REBUILD_FROM_EXISTING_TAGS}" = "true" ]]; then
    echo "[INFO] Checking out from existing ${CHE_VERSION} tag"
    checkoutTags
else
    echo "[INFO] Creating a new ${CHE_VERSION} tag"
    prepareRelease
    createTags
fi
releaseCheServer
releaseTypescriptDto

if [[ "${BUILD_AND_PUSH_IMAGES}" = "true" ]]; then
    buildImages  ${CHE_VERSION}
    tagLatestImages ${CHE_VERSION}
    pushImagesOnQuay ${CHE_VERSION} pushLatest
fi
