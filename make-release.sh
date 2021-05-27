#!/bin/bash
# Copyright (c) 2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

set -e
REPO=git@github.com:eclipse-che/che-server
MAIN_BRANCH="main"
TMP=""

while [[ "$#" -gt 0 ]]; do
  case $1 in
    '-v'|'--version') VERSION="$2"; shift 1;;
    '-tmp'|'--use-tmp-dir') TMP=$(mktemp -d); shift 0;;
  esac
  shift 1
done

bump_version () {
  CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

  NEXT_VERSION=$1
  BUMP_BRANCH=$2

  git checkout "${BUMP_BRANCH}"

  echo "Updating project version to ${NEXT_VERSION}"
  echo "${NEXT_VERSION}" > VERSION
  git add VERSION
  COMMIT_MSG="[release] Bump to ${NEXT_VERSION} in ${BUMP_BRANCH}"
  git commit -asm "${COMMIT_MSG}"
  git pull origin "${BUMP_BRANCH}"

  set +e
  PUSH_TRY="$(git push origin "${BUMP_BRANCH}")"
  # shellcheck disable=SC2181
  if [[ $? -gt 0 ]] || [[ $PUSH_TRY == *"protected branch hook declined"* ]]; then
    PR_BRANCH=pr-${BUMP_BRANCH}-to-${NEXT_VERSION}
    # create pull request for the main branch branch, as branch is restricted
    git branch "${PR_BRANCH}"
    git checkout "${PR_BRANCH}"
    git pull origin "${PR_BRANCH}"
    git push origin "${PR_BRANCH}"
    lastCommitComment="$(git log -1 --pretty=%B)"
    hub pull-request -f -m "${lastCommitComment}" -b "${BUMP_BRANCH}" -h "${PR_BRANCH}"
  fi
  set -e
  git checkout "${CURRENT_BRANCH}"
}

usage ()
{
  echo "Usage: $0 --version [VERSION TO RELEASE]"
  echo -e "Example: $0 --version v0.1.0\n";
}

if [[ ! ${VERSION} ]]; then
  usage
  exit 1
fi


# derive bugfix branch from version
BRANCH=${VERSION#v}
BRANCH=${BRANCH%.*}.x

# if doing a .0 release, use main branch; if doing a .z release, use $BRANCH
if [[ ${VERSION} == *".0" ]]; then
  BASEBRANCH="${MAIN_BRANCH}"
else
  BASEBRANCH="${BRANCH}"
fi

# work in tmp dir
if [[ $TMP ]] && [[ -d $TMP ]]; then
  pushd "$TMP" > /dev/null || exit 1
  # get sources from ${BASEBRANCH} branch
  echo "Check out ${REPO} to ${TMP}/${REPO##*/}"
  git clone "${REPO}" -q
  cd "${REPO##*/}" || exit 1
fi

git remote show origin

# get sources from ${BASEBRANCH} branch
git fetch origin "${BASEBRANCH}":"${BASEBRANCH}" || true
git checkout "${BASEBRANCH}"

# create new branch off ${BASEBRANCH} (or check out latest commits if branch already exists), then push to origin
if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
  git branch "${BRANCH}" || git checkout "${BRANCH}"
  git push origin "${BRANCH}"
  git fetch origin "${BRANCH}:${BRANCH}" || true
  git checkout "${BRANCH}"
else
  git fetch origin "${BRANCH}:${BRANCH}" || true
  git checkout "${BRANCH}"
fi
set -e

# change VERSION file
echo "${VERSION}" > VERSION
git add VERSION

./dockerfiles/e2e/build.sh --tag:${VERSION}
docker tag quay.io/eclipse/che-e2e:${VERSION} quay.io/eclipse/che-e2e:latest
docker push quay.io/eclipse/che-e2e:${VERSION}
docker push quay.io/eclipse/che-e2e:latest

# tag the release
git tag "${VERSION}"
git push origin "${VERSION}"
COMMIT_MSG="[release] Release ${VERSION}"
git commit -asm "${COMMIT_MSG}"

# now update ${BASEBRANCH} to the new snapshot version
git checkout "${BASEBRANCH}"

# change VERSION file + commit change into ${BASEBRANCH} branch
if [[ "${BASEBRANCH}" != "${BRANCH}" ]]; then
  # bump the y digit, if it is a major release
  [[ $BRANCH =~ ^([0-9]+)\.([0-9]+)\.x ]] && BASE=${BASH_REMATCH[1]}; NEXT=${BASH_REMATCH[2]}; (( NEXT=NEXT+1 )) # for BRANCH=0.1.x, get BASE=0, NEXT=2
  NEXT_VERSION_Y="${BASE}.${NEXT}.0-SNAPSHOT"
  bump_version "${NEXT_VERSION_Y}" "${BASEBRANCH}"
fi
# bump the z digit
[[ ${VERSION#v} =~ ^([0-9]+)\.([0-9]+)\.([0-9]+) ]] && BASE="${BASH_REMATCH[1]}.${BASH_REMATCH[2]}"; NEXT="${BASH_REMATCH[3]}"; (( NEXT=NEXT+1 )) # for VERSION=0.1.2, get BASE=0.1, NEXT=3
NEXT_VERSION_Z="${BASE}.${NEXT}-SNAPSHOT"
bump_version "${NEXT_VERSION_Z}" "${BRANCH}"

# cleanup tmp dir
if [[ $TMP ]] && [[ -d $TMP ]]; then
  popd > /dev/null || exit
  rm -fr "$TMP"
fi
