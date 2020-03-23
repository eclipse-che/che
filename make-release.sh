#!/bin/bash
# Release process automation script. 
# Used to create branch/tag, update versions in pom.xml
# and and trigger release by force pushing changes to the release branch 

# set to 1 to actually trigger changes in the release branch
TRIGGER_RELEASE=0 
PRERELEASE_TESTING=0

bump_version() {
  CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

  NEXTVERSION=$1
  BUMP_BRANCH=$2

  git checkout ${BUMP_BRANCH}

  echo "Updating project version to ${NEXTVERSION}"
  mvn versions:set -DnewVersion=${NEXTVERSION}
  mvn versions:update-parent -DallowSnapshots=true -DparentVersion=${NEXTVERSION}
  mvn versions:commit

  echo "removing files"
  #TODO investigate why these files sometimes need to be cleaned up manually
  rm	wsmaster/integration-tests/mysql-tck/pom.xml.versionsBackup
  rm	wsmaster/integration-tests/postgresql-tck/pom.xml.versionsBackup

  echo "sed"
  # set new dependencies versions 
  sed -i -e "s#${VERSION}-SNAPSHOT#${NEXTVERSION}#" pom.xml

  COMMIT_MSG="[release] Bump to ${NEXTVERSION} in ${BUMP_BRANCH}"
  git commit -a -s -m "${COMMIT_MSG}"

  PR_BRANCH=pr-master-to-${NEXTVERSION}
  # create pull request for master branch, as branch is restricted
  git branch "${PR_BRANCH}"
  git checkout "${PR_BRANCH}"
  git pull origin "${PR_BRANCH}"
  git push origin "${PR_BRANCH}"
  lastCommitComment="$(git log -1 --pretty=%B)"
  hub pull-request -o -f -m "${lastCommitComment}
  ${lastCommitComment}" -b "${BRANCH}" -h "${PR_BRANCH}"

  git checkout ${CURRENT_BRANCH}
}

ask() {
  while true; do
    echo -e -n " (Y)es or (N)o "
    read -r yn
    case $yn in
      [Yy]* ) return 0;;
      [Nn]* ) return 1;;
      * ) echo "Please answer (Y)es or (N)o. ";;
    esac
  done
}


while [[ "$#" -gt 0 ]]; do
  case $1 in
    '-t'|'--trigger-release') TRIGGER_RELEASE=1; PRERELEASE_TESTING=0; shift 0;;
    '-r'|'--repo') REPO="$2"; shift 1;;
    '-v'|'--version') VERSION="$2"; shift 1;;
    '-p'|'--prerelease-testing') PRERELEASE_TESTING=1; TRIGGER_RELEASE=0; shift 0;;
  esac
  shift 1
done

usage ()
{
  echo "Provide the necessary parameters and make sure to choose either prerelease testing or trigger release option"
  echo "Usage: $0 --repo [GIT REPO TO EDIT] --version [VERSION TO RELEASE] [--trigger-release] [--prerelease-testing]"
  echo "Example: $0 --repo git@github.com:eclipse/che-subproject --version 7.7.0 --trigger-release"; echo
}

if [[ ! ${VERSION} ]] || [[ ! ${REPO} ]]; then
  usage
  exit 1
fi


set +e
  ask "Remove the tag if it already exists?"
  result=$?
set -e

if [[ $result == 0 ]]; then
  git add -A
  git push origin :${VERSION}
fi

# derive branch from version
BRANCH=${VERSION%.*}.x

# if doing a .0 release, use master; if doing a .z release, use $BRANCH
if [[ ${VERSION} == *".0" ]]; then
  BASEBRANCH="master"
else 
  BASEBRANCH="${BRANCH}"
fi

# work in tmp dir
TMP=$(mktemp -d); pushd "$TMP" > /dev/null || exit 1

# get sources from ${BASEBRANCH} branch
echo "Check out ${REPO} to ${TMP}/${REPO##*/}"
git clone "${REPO}" -q
cd "${REPO##*/}" || exit 1
git fetch origin "${BASEBRANCH}":"${BASEBRANCH}"
git checkout "${BASEBRANCH}"

# create new branch off ${BASEBRANCH} (or check out latest commits if branch already exists), then push to origin
if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
  git branch "${BRANCH}" || git checkout "${BRANCH}" && git pull origin "${BRANCH}"
  git push origin "${BRANCH}"
  git fetch origin "${BRANCH}:${BRANCH}"
  git checkout "${BRANCH}"

  cd .ci 
  ./set_tag_version_images_linux.sh ${VERSION}
  cd ..
  git commit -a -s -m "Update image tags to ${VERSION} version"
fi

if [[ $PRERELEASE_TESTING -eq 1 ]]; then
  # create pre-release branch and update image tags
  git checkout release-candidate
  cd .ci 
  ./set_tag_version_images_linux.sh ${VERSION}
  cd ..
  git commit -a -s -m "Update image tags to ${VERSION} version"
  git push origin release-candidate -f
fi

if [[ $TRIGGER_RELEASE -eq 1 ]]; then
  # push new branch to release branch to trigger CI build
  git fetch origin "release-candidate:release-candidate"
  git checkout "release-candidate"
  git branch release -f
  git push origin release -f
fi

# now update ${BASEBRANCH} to the new snapshot version
git fetch origin "${BASEBRANCH}":"${BASEBRANCH}"
git checkout "${BASEBRANCH}"

# infer project version + commit change into ${BASEBRANCH} branch
if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
  # bump the y digit
  [[ $BRANCH =~ ^([0-9]+)\.([0-9]+)\.x ]] && BASE=${BASH_REMATCH[1]}; NEXT=${BASH_REMATCH[2]}; (( NEXT=NEXT+1 )) # for BRANCH=7.10.x, get BASE=7, NEXT=11
  NEXTVERSION_Y="${BASE}.${NEXT}.0-SNAPSHOT"
  bump_version ${NEXTVERSION_Y} ${BASEBRANCH}
fi
# bump the z digit
[[ $VERSION =~ ^([0-9]+)\.([0-9]+)\.([0-9]+) ]] && BASE="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}"; NEXT="${BASH_REMATCH[3]}"; (( NEXT=NEXT+1 )) # for VERSION=7.7.1, get BASE=7.7, NEXT=2
NEXTVERSION_Z="${BASE}.${NEXT}-SNAPSHOT"
bump_version ${NEXTVERSION_Z} ${BUGFIX}

popd > /dev/null || exit

# cleanup tmp dir
# cd /tmp && rm -fr "$TMP"
