# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation

FROM alpine:3.10

RUN apk --no-cache add curl jq
ENTRYPOINT ["sh", "-c", "adresses_length=0; until [ $adresses_length -gt 0 ]; do echo waiting for $ENDPOINT to be ready...; sleep 2; endpoints=`curl -s --cacert /var/run/secrets/kubernetes.io/serviceaccount/ca.crt -H \"Authorization: Bearer $(cat /var/run/secrets/kubernetes.io/serviceaccount/token)\"     https://kubernetes.default/api/v1/namespaces/$POD_NAMESPACE/endpoints/$ENDPOINT`; adresses_length=`echo $endpoints | jq -r \".subsets[]?.addresses // [] | length\"`; done;"]
