#!/bin/bash
# Copyright (c) 2017-2021 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

sed_in_place() {
    SHORT_UNAME=$(uname -s)
  if [ "$(uname)" == "Darwin" ]; then
    sed -i '' "$@"
  elif [ "${SHORT_UNAME:0:5}" == "Linux" ]; then
    sed -i "$@"
  fi
}

# only use /latest plugins so updates are smoother (old plugins are deleted from registries with each new release)
plugin_version="latest"
sed_in_place -r -e "s#che.factory.default_editor=eclipse/che-theia/.*#che.factory.default_editor=eclipse/che-theia/$plugin_version#g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed_in_place -r -e "s#che.workspace.devfile.default_editor=eclipse/che-theia/.*#che.workspace.devfile.default_editor=eclipse/che-theia/$plugin_version#g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties

# use actual image tags, so a new container is used after each update
image_version="$1"
sed_in_place -r -e "s#che-plugin-registry:.*#che-plugin-registry:$image_version#g" ../deploy/kubernetes/helm/che/custom-charts/che-plugin-registry/values.yaml
sed_in_place -r -e "s#che-devfile-registry:.*#che-devfile-registry:$image_version#g" ../deploy/kubernetes/helm/che/custom-charts/che-devfile-registry/values.yaml
sed_in_place -r -e "s#che-postgres:.*#che-postgres:$image_version#g" ../deploy/kubernetes/helm/che/custom-charts/che-postgres/values.yaml
sed_in_place -r -e "s#che-keycloak:.*#che-keycloak:$image_version#g" ../deploy/kubernetes/helm/che/custom-charts/che-keycloak/values.yaml
sed_in_place -r -e "s#eclipse/che-server:.*#eclipse/che-server:$image_version#g" ../deploy/kubernetes/helm/che/values.yaml
sed_in_place -r -e "s#eclipse/che-dashboard:.*#eclipse/che-dashboard:$image_version#g" ../deploy/kubernetes/helm/che/values.yaml

sed_in_place -r -e "s#che-endpoint-watcher:nightly#che-endpoint-watcher:$image_version#g" ../deploy/kubernetes/helm/che/custom-charts/che-keycloak/templates/deployment.yaml
sed_in_place -r -e "s#che-endpoint-watcher:nightly#che-endpoint-watcher:$image_version#g" ../deploy/kubernetes/helm/che/templates/deployment.yaml
