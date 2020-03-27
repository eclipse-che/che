#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

sed -i "s/DEFAULT_PLUGIN_REGISTRY_IMAGE_TAG=\".*\"/DEFAULT_PLUGIN_REGISTRY_IMAGE_TAG=\"$1\"/g" ../deploy/openshift/deploy_che.sh
sed -i "s/DEFAULT_DEVFILE_REGISTRY_IMAGE_TAG=\".*\"/DEFAULT_DEVFILE_REGISTRY_IMAGE_TAG=\"$1\"/g" ../deploy/openshift/deploy_che.sh
sed -i "s/DEFAULT_CHE_IMAGE_TAG=\".*\"/DEFAULT_CHE_IMAGE_TAG=\"$1\"/g" ../deploy/openshift/deploy_che.sh
sed -i "s/DEFAULT_KEYCLOAK_IMAGE_TAG=\".*\"/DEFAULT_KEYCLOAK_IMAGE_TAG=\"$1\"/g" ../deploy/openshift/deploy_che.sh

sed -i "s/che.factory.default_editor=eclipse\/che-theia\/.*/che.factory.default_editor=eclipse\/che-theia\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed -i "s/che.factory.default_plugins=eclipse\/che-machine-exec-plugin\/.*/che.factory.default_plugins=eclipse\/che-machine-exec-plugin\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed -i "s/che.workspace.devfile.default_editor=eclipse\/che-theia\/.*/che.workspace.devfile.default_editor=eclipse\/che-theia\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed -i "s/che.workspace.devfile.default_editor.plugins=eclipse\/che-machine-exec-plugin\/.*/che.workspace.devfile.default_editor.plugins=eclipse\/che-machine-exec-plugin\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties

sed -i "s/che-plugin-registry:.*/che-plugin-registry:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-plugin-registry/values.yaml
sed -i "s/che-devfile-registry:.*/che-devfile-registry:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-devfile-registry/values.yaml
sed -i "s/che-postgres:.*/che-postgres:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-postgres/values.yaml
sed -i "s/che-keycloak:.*/che-keycloak:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-keycloak/values.yaml
sed -i "s/eclipse\/che-server:.*/eclipse\/che-server:$1/g" ../deploy/kubernetes/helm/che/values.yaml

sed -i "s/che-endpoint-watcher:nightly/che-endpoint-watcher:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-keycloak/templates/deployment.yaml
sed -i "s/che-endpoint-watcher:nightly/che-endpoint-watcher:$1/g" ../deploy/kubernetes/helm/che/templates/deployment.yaml
