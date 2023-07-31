#!/bin/bash
#
# Copyright (c) 2012-2023 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

# exit immediately when a command fails
set -e
# only exit with zero if all commands of the pipeline exit successfully
set -o pipefail
# error on unset variables
set -u
# uncomment to print each command before executing it
set -x

SCRIPT_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
WORKDIR_KUBE="${WORKDIR:-${SCRIPT_DIR}/workdir/.kube}"

rm -rf "${WORKDIR_KUBE}" && mkdir -p "${WORKDIR_KUBE}"

# Create cluster-admin user inside of OpenShift cluster and login
function run() {
  echo "[INFO] Testing if Che User exists."
  KUBECONFIG="${KUBECONFIG:-${HOME}/.kube/config}"
  TMP_KUBECONFIG="$WORKDIR_KUBE/kubeconfig"
  cp "$KUBECONFIG" "$TMP_KUBECONFIG"

  if oc login -u che-user -p user --kubeconfig "${TMP_KUBECONFIG}"; then
    echo "[INFO] Che User already exists. Using it"
    return 0
  fi
  echo "[INFO] Che User does not exist. Setting up htpasswd oauth for it."
  oc delete secret dev-htpasswd-secret -n openshift-config || true
  oc create secret generic dev-htpasswd-secret --from-file=htpasswd="$SCRIPT_DIR/resources/users.htpasswd" -n openshift-config

  if [[ $(oc get oauth cluster --ignore-not-found) == "" ]]; then
    echo "[INFO] Creating a new OAuth Cluster since it's not found."
    oc apply -f ${SCRIPT_DIR}/resources/cluster-oauth.yaml
  # CustomResources don't support strategic merge. So, we need to merge or add array item depending on the object state
  elif [[ $(oc get oauth/cluster -o=json | jq -e 'select (.spec.identityProviders == null)') ]]; then
    # there are no identity providers. We can do merge and set the whole .spec.identityProviders field
    echo "[INFO] No identity providers found, provisioning Che one."
    oc patch oauth/cluster --type=merge -p "$(cat $SCRIPT_DIR/resources/cluster-oauth-patch.json)"
  elif [[ ! $(oc get oauth/cluster -o=json | jq -e '.spec.identityProviders[]?.name? | select ( . == ("dev-htpasswd"))') ]]; then
    # there are some identity providers. We should do add patch not to override existing identity providers
    echo "[INFO] OAuth Cluster is found but dev-htpasswd provider missing. Provisioning it."
    oc patch oauth/cluster --type=json -p '[{
      "op": "add",
      "path": "/spec/identityProviders/0",
      "value": {
        "name":"dev-htpasswd",
        "mappingMethod":"add",
        "type":"HTPasswd",
        "htpasswd": {
          "fileData":{"name":"dev-htpasswd-secret"}
        }
      }
    }]'

  else
    echo "[INFO] dev-htpasswd oauth provider is found. Using it"
  fi

  echo "[INFO] rollout oauth-openshift pods for applying OAuth configuration"
  # apply just added identity providers, we need to rollout deployment for make sure
  # that new IDP item will appear in the IDP table
  # https://github.com/eclipse/che/issues/20822

  oc rollout status -n openshift-authentication deployment/oauth-openshift
  echo -e "[INFO] Waiting for htpasswd auth to be working up to 5 minutes"
  CURRENT_TIME=$(date +%s)
  ENDTIME=$(($CURRENT_TIME + 300))
  while [ $(date +%s) -lt $ENDTIME ]; do
      if oc login -u happypath-dev -p dev --kubeconfig $TMP_KUBECONFIG; then
          return 0
      fi
      sleep 10
  done
  echo "[ERROR] Che htpasswd changes are not affected after timeout."
  exit 1
}

run
