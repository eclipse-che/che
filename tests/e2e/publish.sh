#!/bin/bash

TIME=$(date +%s)

if [[ $CHE_VERSION == *SNAPSHOT ]] ; then 
    echo "Building and pushing latest E2E package version $CHE_VERSION"
    VERSION=${CHE_VERSION%"-SNAPSHOT"}-$TIME
    TAG="nightly"
else 
    echo "Building and pushing E2E package release version $CHE_VERSION"
    VERSION=$CHE_VERSION-$TIME
    TAG=$CHE_VERSION
fi

npm version $VERSION
#npm publish --tag $TAG 
