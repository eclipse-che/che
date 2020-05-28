#!/bin/bash
# shellcheck disable=SC2155,SC2125

#
# Copyright (c) 2012-2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

#Declare CN
export CA_CN=eclipse-che-signer

export DOMAIN=*.$(minishift ip).nip.io

#Create Root Key
openssl genrsa -out rootCA.key 4096

#Create and self sign the Root Certificate
openssl req -x509 -new -nodes -key rootCA.key -subj /CN=${CA_CN} -sha256 -days 1024 -out rootCA.crt

#Create the certificate key
openssl genrsa -out domain.key 2048

#Create the signing (csr)
openssl req -new -sha256 -key domain.key -subj "/C=US/ST=CK/O=RedHat/CN=${DOMAIN}" -out domain.csr

#Verify Csr
openssl req -in domain.csr -noout -text

#Generate the certificate using the domain csr and key along with the CA Root key
openssl x509 -req -in domain.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out domain.crt -days 500 -sha256

#Verify the certificate's content
openssl x509 -in domain.crt -text -noout 
