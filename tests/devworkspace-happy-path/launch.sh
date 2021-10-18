#!/bin/bash
#
# Copyright (c) 2012-2021 Red Hat, Inc.
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

START=$(date +%s.%N)
SCRIPT_DIR=$(dirname $(readlink -f "$0"))

export WORKDIR="${WORKDIR:-${SCRIPT_DIR}/workdir}"
export CHE_NAMESPACE="${CHE_NAMESPACE:-eclipse-che}"
export E2E_TEST_IMAGE="${E2E_TEST_IMAGE:-quay.io/eclipse/che-e2e:next}"
export HAPPY_PATH_POD_NAME=happy-path-che
export HAPPY_PATH_TEST_PROJECT='https://github.com/che-samples/java-spring-petclinic/tree/devfilev2'

rm -rf ${WORKDIR}
mkdir -p ${WORKDIR}

# Create cluster-admin user inside of OpenShift cluster and login
function provisionOpenShiftOAuthUser() {
  echo "[INFO] Testing if Che User exists."
  # preparing temp kubeconfig for testing che user availability
  KUBECONFIG="${KUBECONFIG:-${HOME}/.kube/config}"
  cp "$KUBECONFIG" "$WORKDIR/kubeconfig"
  KUBECONFIG="$WORKDIR/kubeconfig"

  if KUBECONFIG="$KUBECONFIG" oc login -u che-user -p user --insecure-skip-tls-verify=false; then
    echo "[INFO] Che User already exists. Using it"
    return 0
  fi
  echo "[INFO] Che User does not exist. Setting up htpasswd oauth for it."
  oc delete secret che-htpasswd-secret -n openshift-config || true
  oc create secret generic che-htpasswd-secret --from-file=htpasswd="$SCRIPT_DIR/resources/users.htpasswd" -n openshift-config

  if [[ $(oc get oauth cluster --ignore-not-found) == "" ]]; then
    echo "[INFO] Creating a new OAuth Cluster since it's not found."
    oc apply -f ${SCRIPT_DIR}/resources/cluster-oauth.yaml
  elif [[ ! $(oc get oauth/cluster -o=json | jq -e '.spec.identityProviders[]?.name? | select ( . == ("che-htpasswd"))') ]]; then
    echo "[INFO] OAuth Cluster is found but che-htpasswd missing. Provisioning it."
    oc patch oauth/cluster --type=json \
      -p '[{"op": "add", "path": "/spec/identityProviders/0", "value": {"name":"che-htpasswd","mappingMethod":"claim","type":"HTPasswd","htpasswd":{"fileData":{"name":"che-htpasswd-secret"}}}}]'
  else
    echo "[INFO] che-htpasswd oauth provider is found. Using it"
  fi

  echo -e "[INFO] Waiting for htpasswd auth to be working up to 5 minutes"
  CURRENT_TIME=$(date +%s)
  ENDTIME=$(($CURRENT_TIME + 300))
  while [ $(date +%s) -lt $ENDTIME ]; do
      if KUBECONFIG="$KUBECONFIG" oc login -u che-user -p user --insecure-skip-tls-verify=false; then
          return 0
      fi
      sleep 10
  done
  echo "[ERROR] Che htpasswd changes are not affected after timeout."
  exit 1
}

startHappyPathTest() {
  oc delete pod happy-path-che -n eclipse-che --grace-period=30 --ignore-not-found
  # patch happy-path-che.yaml
  ECLIPSE_CHE_URL=http://$(oc get route -n "${CHE_NAMESPACE}" che -o jsonpath='{.status.ingress[0].host}')
  TS_SELENIUM_DEVWORKSPACE_URL="${ECLIPSE_CHE_URL}/#${HAPPY_PATH_TEST_PROJECT}"
  HAPPY_PATH_POD_FILE=${SCRIPT_DIR}/resources/pod-che-happy-path.yaml
  cp $HAPPY_PATH_POD_FILE ${WORKDIR}/e2e-pod.yaml
  sed -i "s@CHE_URL@${ECLIPSE_CHE_URL}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@WORKSPACE_ROUTE@${TS_SELENIUM_DEVWORKSPACE_URL}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@CHE-NAMESPACE@${CHE_NAMESPACE}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@image: .*@image: ${E2E_TEST_IMAGE}@g" ${WORKDIR}/e2e-pod.yaml
  echo "[INFO] Applying the following patched Che Happy Path Pod:"
  cat ${WORKDIR}/e2e-pod.yaml
  echo "[INFO] --------------------------------------------------"
  oc apply -f ${WORKDIR}/e2e-pod.yaml
  # wait for the pod to start
  n=0
  while [ $n -le 120 ]
  do
    PHASE=$(oc get pod -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} \
        --template='{{ .status.phase }}')
    if [[ ${PHASE} == "Running" ]]; then
      echo "[INFO] Happy-path test started successfully."
      return
    fi

    sleep 5
    n=$(( n+1 ))
  done

  echo "[ERROR] Failed to start happy-path test."
  exit 1
}

provisionOpenShiftOAuthUser

source "${SCRIPT_DIR}"/common.sh
# Catch the finish of the job and write logs in artifacts.
trap 'collectLogs $?' EXIT SIGINT

startHappyPathTest

echo "[INFO] Waiting until happy path pod finished"
oc logs -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c happy-path-test -f
# just to sleep
sleep 3

echo "[INFO] Downloading test report."
mkdir ${ARTIFACT_DIR}/e2e
oc rsync -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME}:/tmp/e2e/report/ ${ARTIFACT_DIR}/e2e -c download-reports
oc exec -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c download-reports -- touch /tmp/done
EXIT_CODE=$(oc logs -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c happy-path-test | grep EXIT_CODE)
if [[ ${EXIT_CODE} != "+ EXIT_CODE=0" ]]; then
    echo "[ERROR] Happy-path test failed. Check report at ${ARTIFACT_DIR}. Or happy path pod in eclipse-che namespace"
    exit 1
fi
echo "[INFO] Happy-path test succeed."

END=$(date +%s.%N)
echo "[INFO] Happy-path execution took $(echo "$END - $START" | bc) seconds."
