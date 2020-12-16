#!/bin/bash

mvn fmt:format
mvn sortpom:sort
mvn clean install -DskipTests=true

pushd dockerfiles/che || true

./build.sh --tag:nightly

popd || true

docker tag quay.io/eclipse/che-server:nightly quay.io/aandriienko/che-server:nightly

docker push quay.io/aandriienko/che-server:nightly
# chectl server:deploy -n eclipse-che -p minishift -n eclipse-che --installer=operator --cheimage=quay.io/aandriienko/che-server:nightly
