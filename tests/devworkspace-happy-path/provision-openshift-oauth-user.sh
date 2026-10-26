#!/bin/bash
#
# Copyright (c) 2012-2026 Red Hat, Inc.
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
# set -x

SCRIPT_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
WORKDIR_KUBE="${WORKDIR:-${SCRIPT_DIR}/workdir/.kube}"

# Credentials of the htpasswd user from resources/users.htpasswd.
# The e2e pod logs into OpenShift with them (see resources/pod-che-happy-path.yaml).
HAPPY_PATH_USER="happypath-dev"
HAPPY_PATH_PASSWORD="dev"

rm -rf "${WORKDIR_KUBE}" && mkdir -p "${WORKDIR_KUBE}"

# HyperShift hosted clusters cannot be configured through the OAuth CR:
# creating, updating or deleting it is denied by the ValidatingAdmissionPolicy,
# and identity providers must be set on the HostedCluster object instead.
# The hypershift-hostedcluster-create-hostedcluster ci-operator step leaves
# the management cluster kubeconfig and the hosted cluster name in the shared
# directory, so their presence reliably detects a hosted cluster environment.
function isHyperShift() {
  [[ -n "${SHARED_DIR:-}" \
    && -f "${SHARED_DIR}/mgmt_kubeconfig" \
    && -f "${SHARED_DIR}/cluster-name" ]]
}

# Provisions the htpasswd identity provider on a HyperShift hosted cluster
# by patching the HostedCluster object on the management cluster.
function provisionHtpasswdIdentityProviderHyperShift() {
  echo "[INFO] HyperShift environment: provisioning the htpasswd identity provider via the HostedCluster API"

  local CLUSTER_NAME
  CLUSTER_NAME=$(cat "${SHARED_DIR}/cluster-name")
  local MGMT_KUBECONFIG="${SHARED_DIR}/mgmt_kubeconfig"
  local HYPERSHIFT_NS
  HYPERSHIFT_NS=$(cat "${SHARED_DIR}/hypershift-clusters-namespace" 2>/dev/null || echo "clusters")

  # the identity provider secret must live next to the HostedCluster,
  # i.e. in the hosted clusters namespace on the management cluster
  oc create secret generic dev-htpasswd-secret \
    --from-file=htpasswd="${SCRIPT_DIR}/resources/users.htpasswd" -n "${HYPERSHIFT_NS}" \
    --dry-run=client -o yaml | KUBECONFIG="${MGMT_KUBECONFIG}" oc apply -f -

  # Like on standalone clusters, preserve identity providers that are already
  # configured on the HostedCluster: a JSON merge patch replaces the whole
  # identityProviders list instead of merging its entries, so it may only
  # be used when the list is empty.
  local CURRENT_IDPS
  CURRENT_IDPS=$(KUBECONFIG="${MGMT_KUBECONFIG}" oc get hostedcluster "${CLUSTER_NAME}" \
    -n "${HYPERSHIFT_NS}" -o json | jq -c '.spec.configuration.oauth.identityProviders // []')

  if [[ $(echo "${CURRENT_IDPS}" | jq 'length') -eq 0 ]]; then
    # there are no identity providers. We can do merge and set the whole .spec.identityProviders field
    echo "[INFO] No identity providers found, provisioning Che one."
    KUBECONFIG="${MGMT_KUBECONFIG}" oc patch hostedcluster "${CLUSTER_NAME}" \
      -n "${HYPERSHIFT_NS}" --type=merge -p '{
        "spec": {
          "configuration": {
            "oauth": {
              "identityProviders": [{
                "name": "dev-htpasswd",
                "mappingMethod": "add",
                "type": "HTPasswd",
                "htpasswd": {
                  "fileData": {"name": "dev-htpasswd-secret"}
                }
              }]
            }
          }
        }
      }'
  elif ! echo "${CURRENT_IDPS}" | jq -e '.[]? | select(.name == "dev-htpasswd")' > /dev/null; then
    # there are some identity providers. We should do add patch not to override existing identity providers
    echo "[INFO] HostedCluster is found but dev-htpasswd provider missing. Provisioning it."
    KUBECONFIG="${MGMT_KUBECONFIG}" oc patch hostedcluster "${CLUSTER_NAME}" \
      -n "${HYPERSHIFT_NS}" --type=json -p '[{
        "op": "add",
        "path": "/spec/configuration/oauth/identityProviders/-",
        "value": {
          "name": "dev-htpasswd",
          "mappingMethod": "add",
          "type": "HTPasswd",
          "htpasswd": {
            "fileData": {"name": "dev-htpasswd-secret"}
          }
        }
      }]'
  else
    echo "[INFO] dev-htpasswd oauth provider is found on the HostedCluster. Using it"
  fi

  echo "[INFO] HostedCluster OAuth configuration is applied"
}

# Provisions the htpasswd identity provider on a standalone OpenShift cluster
# by patching the cluster OAuth CR.
function provisionHtpasswdIdentityProvider() {
  oc delete secret dev-htpasswd-secret -n openshift-config || true
  oc create secret generic dev-htpasswd-secret --from-file=htpasswd="$SCRIPT_DIR/resources/users.htpasswd" -n openshift-config

  # CustomResources don't support strategic merge. So, we need to merge or add array item depending on the object state
  if [[ $(oc get oauth/cluster -o=json | jq -e 'select (.spec.identityProviders == null)') ]]; then
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
}

# Waits until the htpasswd user is able to log into the cluster.
function waitHtpasswdAuthWorks() {
  TIMEOUT_SECONDS=$1
  echo "[INFO] Waiting for htpasswd auth to be working up to $((TIMEOUT_SECONDS / 60)) minutes"
  CURRENT_TIME=$(date +%s)
  ENDTIME=$(($CURRENT_TIME + TIMEOUT_SECONDS))
  while [ $(date +%s) -lt $ENDTIME ]; do
      if oc login -u ${HAPPY_PATH_USER} -p ${HAPPY_PATH_PASSWORD} --kubeconfig "${TMP_KUBECONFIG}"; then
          return 0
      fi
      sleep 10
  done
  echo "[ERROR] Che htpasswd changes are not affected after timeout."
  exit 1
}

# Provision the OpenShift OAuth user used by the happy-path test
function run() {
  echo "[INFO] Testing if Che User exists."
  KUBECONFIG="${KUBECONFIG:-${HOME}/.kube/config}"
  TMP_KUBECONFIG="$WORKDIR_KUBE/kubeconfig"
  cp "$KUBECONFIG" "$TMP_KUBECONFIG"

  if oc login -u ${HAPPY_PATH_USER} -p ${HAPPY_PATH_PASSWORD} --kubeconfig "${TMP_KUBECONFIG}"; then
    echo "[INFO] Che User already exists. Using it"
    return 0
  fi
  echo "[INFO] Che User does not exist. Setting up htpasswd oauth for it."

  if isHyperShift; then
    # On HyperShift the OAuth pods are rolled out by the control plane
    # once the HostedCluster object is reconfigured, so there is nothing
    # to wait for with 'oc rollout status' — polling the login below
    # covers the whole reconfiguration. It is also slower than a plain
    # OAuth CR patch, hence the extended timeout.
    provisionHtpasswdIdentityProviderHyperShift
    waitHtpasswdAuthWorks 600
  else
    provisionHtpasswdIdentityProvider
    waitHtpasswdAuthWorks 300
  fi
}

run
