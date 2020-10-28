#!/bin/bash

mvn fmt:format
mvn clean install -DskipTests=true

pushd dockerfiles/che || true

./build.sh --tag:nightly

popd || true

docker tag quay.io/eclipse/che-server:nightly quay.io/aandriienko/che-server:nightly

docker push quay.io/aandriienko/che-server:nightly
