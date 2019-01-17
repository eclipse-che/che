# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

###
# Builder Image
#
#FROM maven:3.3-jdk-8 as builder

#ADD ./dto-pom.xml /generator/pom.xml

#RUN cd /generator && mvn -U -DskipTests=true -Dfindbugs.skip=true -Dskip-validate-sources install

###
# Publish image
#
FROM node:6.11.2

RUN npm i -g yarn@1.9.4

ADD package.json /che/package.json

COPY ./index.d.ts /che/index.d.ts

ADD publish.sh publish.sh

ARG NPM_AUTH_TOKEN

ARG CHE_VERSION

RUN cd /che && ../publish.sh ${CHE_VERSION}
