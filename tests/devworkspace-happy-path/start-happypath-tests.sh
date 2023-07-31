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
# set -x

SCRIPT_DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)

export WORKDIR="${WORKDIR:-${SCRIPT_DIR}/workdir/e2e}"
export CHE_NAMESPACE="${CHE_NAMESPACE:-eclipse-che}"
export E2E_TEST_IMAGE="${E2E_TEST_IMAGE:-quay.io/eclipse/che-e2e:next}"
export HAPPY_PATH_POD_NAME=happy-path-che
export HAPPY_PATH_TEST_PROJECT='https://github.com/che-samples/java-spring-petclinic/tree/devfilev2'
export HAPPY_PATH_SUITE="${HAPPY_PATH_SUITE:-test-all-devfiles}"
export HAPPY_PATH_USERSTORY="${HAPPY_PATH_USERSTORY:-EmptyWorkspace}"

rm -rf "${WORKDIR}" && mkdir -p "${WORKDIR}"

run() {
  oc create namespace "${CHE_NAMESPACE}" || true
  oc delete pod happy-path-che -n "${CHE_NAMESPACE}" --grace-period=30 --ignore-not-found
  # patch happy-path-che.yaml
  set +u
    if [[ -z "${ECLIPSE_CHE_URL}" ]]; then
      ECLIPSE_CHE_URL=http://$(oc get route -n "${CHE_NAMESPACE}" che -o jsonpath='{.status.ingress[0].host}')
    fi
  set -u
  TS_SELENIUM_DEVWORKSPACE_URL="${ECLIPSE_CHE_URL}/#${HAPPY_PATH_TEST_PROJECT}"
  HAPPY_PATH_POD_FILE=${SCRIPT_DIR}/resources/pod-che-happy-path.yaml
  cp $HAPPY_PATH_POD_FILE ${WORKDIR}/e2e-pod.yaml
  sed -i "s@HAPPY_PATH_SUITE@${HAPPY_PATH_SUITE}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@HAPPY_PATH_USERSTORY@${HAPPY_PATH_USERSTORY}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@CHE_URL@${ECLIPSE_CHE_URL}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@WORKSPACE_ROUTE@${TS_SELENIUM_DEVWORKSPACE_URL}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s@CHE-NAMESPACE@${CHE_NAMESPACE}@g" ${WORKDIR}/e2e-pod.yaml
  sed -i "s|image: .*|image: ${E2E_TEST_IMAGE}|g" ${WORKDIR}/e2e-pod.yaml
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

run

echo "[INFO] Waiting until happy path pod finished"
oc logs -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c happy-path-test -f
# just to sleep
sleep 3

echo "[INFO] Downloading test report."
mkdir "${ARTIFACT_DIR}/e2e"
oc rsync -n "${CHE_NAMESPACE}" ${HAPPY_PATH_POD_NAME}:/tmp/e2e/report/" ${ARTIFACT_DIR}/e2e" -c download-reports
oc exec -n "${CHE_NAMESPACE}" ${HAPPY_PATH_POD_NAME} -c download-reports -- touch /tmp/done

EXIT_CODE=$(oc logs -n "${CHE_NAMESPACE}" ${HAPPY_PATH_POD_NAME} -c happy-path-test | grep EXIT_CODE)
if [[ ${EXIT_CODE} != "+ EXIT_CODE=0" ]]; then
    echo "[ERROR] Happy-path test failed. Check report at ${ARTIFACT_DIR}. Or happy path pod in eclipse-che namespace"
    exit 1
fi
echo "[INFO] Happy-path test succeed."
