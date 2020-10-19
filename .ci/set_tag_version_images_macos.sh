#!/bin/bash
# Copyright (c) 2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

sed -i '' "s/che.factory.default_editor=eclipse\/che-theia\/next/che.factory.default_editor=eclipse\/che-theia\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed -i '' "s/che.factory.default_plugins=eclipse\/che-machine-exec-plugin\/nightly/che.factory.default_plugins=eclipse\/che-machine-exec-plugin\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed -i '' "s/che.workspace.devfile.default_editor=eclipse\/che-theia\/next/che.workspace.devfile.default_editor=eclipse\/che-theia\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties
sed -i '' "s/che.workspace.devfile.default_editor.plugins=eclipse\/che-machine-exec-plugin\/nightly/che.workspace.devfile.default_editor.plugins=eclipse\/che-machine-exec-plugin\/$1/g" ../assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties

sed -i '' "s/che-plugin-registry:nightly/che-plugin-registry:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-plugin-registry/values.yaml
sed -i '' "s/che-devfile-registry:nightly/che-devfile-registry:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-devfile-registry/values.yaml
sed -i '' "s/che-postgres:nightly/che-postgres:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-postgres/values.yaml
sed -i '' "s/che-keycloak:nightly/che-keycloak:$1/g" ../deploy/kubernetes/helm/che/custom-charts/che-keycloak/values.yaml
sed -i '' "s/eclipse\/che-server:nightly/eclipse\/che-server:$1/g" ../deploy/kubernetes/helm/che/values.yaml
sed -i '' "s/eclipse\/che-dashboard:next/eclipse\/che-dashboard:$1/g" ../deploy/kubernetes/helm/che/values.yaml


