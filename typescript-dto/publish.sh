#!/usr/bin/env sh

#get the latest version of @eclipse-che/api
api_version=$(yarn -s info @eclipse-che/api version)

#publish only if latest version doesn't match current version
if [ $api_version != $1 ];
then
    yarn publish  --registry=https://registry.npmjs.org/ --no-git-tag-version --new-version $1
fi
