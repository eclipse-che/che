#!/bin/bash
# Release process automation script. 
# Used to create branch/tag, update versions in pom.xml
# and and trigger release by force pushing changes to the release branch 

# set to 1 to actually trigger changes in the release branch
TRIGGER_RELEASE=0 
NOCOMMIT=0

while [[ "$#" -gt 0 ]]; do
  case $1 in
    '-t'|'--trigger-release') TRIGGER_RELEASE=1; NOCOMMIT=0; shift 0;;
    '-r'|'--repo') REPO="$2"; shift 1;;
    '-v'|'--version') VERSION="$2"; shift 1;;
    '-n'|'--no-commit') NOCOMMIT=1; TRIGGER_RELEASE=0; shift 0;;
  esac
  shift 1
done

usage ()
{
  echo "Usage: $0 --repo [GIT REPO TO EDIT] --version [VERSION TO RELEASE] [--trigger-release]"
  echo "Example: $0 --repo git@github.com:eclipse/che-subproject --version 7.7.0 --trigger-release"; echo
}

if [[ ! ${VERSION} ]] || [[ ! ${REPO} ]]; then
  usage
  exit 1
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
fi

if [[ $TRIGGER_RELEASE -eq 1 ]]; then
  # push new branch to release branch to trigger CI build
  git fetch origin "${BRANCH}:${BRANCH}"
  git checkout "${BRANCH}"
  git branch release -f
  echo "updating tags"
  .ci/.set_tag_version_images_linux.sh ${VERSION}
  git push origin release -f
fi

# now update ${BASEBRANCH} to the new snapshot version
git fetch origin "${BASEBRANCH}":"${BASEBRANCH}"
git checkout "${BASEBRANCH}"

# infer project version + commit change into ${BASEBRANCH} branch
if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
  # bump the y digit
  [[ $BRANCH =~ ^([0-9]+)\.([0-9]+)\.x ]] && BASE=${BASH_REMATCH[1]}; NEXT=${BASH_REMATCH[2]}; (( NEXT=NEXT+1 )) # for BRANCH=7.10.x, get BASE=7, NEXT=11
  NEXTVERSION="${BASE}.${NEXT}.0-SNAPSHOT"
else
  # bump the z digit
  [[ $VERSION =~ ^([0-9]+)\.([0-9]+)\.([0-9]+) ]] && BASE="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}"; NEXT="${BASH_REMATCH[3]}"; (( NEXT=NEXT+1 )) # for VERSION=7.7.1, get BASE=7.7, NEXT=2
  NEXTVERSION="${BASE}.${NEXT}-SNAPSHOT"
fi

# change project version
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

if [[ ${NOCOMMIT} -eq 0 ]]; then
  BRANCH=${BASEBRANCH}
  # commit change into branch
  COMMIT_MSG="[release] Bump to ${NEXTVERSION} in ${BRANCH}"
  git commit -a -s -m "${COMMIT_MSG}"
  git pull origin "${BRANCH}"

  PUSH_TRY="$(git push origin "${BRANCH}")"
  # shellcheck disable=SC2181
  if [[ $? -gt 0 ]] || [[ $PUSH_TRY == *"protected branch hook declined"* ]]; then
  PR_BRANCH=pr-master-to-${NEXTVERSION}
    # create pull request for master branch, as branch is restricted
    git branch "${PR_BRANCH}"
    git checkout "${PR_BRANCH}"
    git pull origin "${PR_BRANCH}"
    git push origin "${PR_BRANCH}"
    lastCommitComment="$(git log -1 --pretty=%B)"
    hub pull-request -o -f -m "${lastCommitComment}
${lastCommitComment}" -b "${BRANCH}" -h "${PR_BRANCH}"
  fi 
fi

popd > /dev/null || exit

# cleanup tmp dir
# cd /tmp && rm -fr "$TMP"
