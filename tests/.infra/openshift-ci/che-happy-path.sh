#!/bin/bash
#
# Copyright (c) 2012-2021 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

set -e

export ARTIFACT_DIR=${ARTIFACT_DIR:-"/tmp/che-happy-path"}
mkdir -p "${ARTIFACT_DIR}"

SCRIPT_DIR=$(dirname $(readlink -f "$0"))

export CHE_NAMESPACE="eclipse-che"
export HAPPY_PATH_POD_NAME=happy-path-che
export HAPPY_PATH_TEST_PROJECT='https://github.com/che-samples/java-spring-petclinic/tree/devfilev2'

# Create cluster-admin user inside of openshift cluster and login
function provisionOpenShiftOAuthUser() {
  if oc login -u che-user -p user --insecure-skip-tls-verify=false; then
    echo "Che User already exists. Using it"
    return 0
  fi
  echo "Che User does not exist. Setting up htpassw oauth for it."
  oc delete secret che-htpass-secret -n openshift-config || true
  oc create secret generic che-htpass-secret --from-file=htpasswd="$SCRIPT_DIR/resources/users.htpasswd" -n openshift-config

# The idea was to patch oauth not to break existing ones but our selenium test is not adapted to it yet.
# So, just override ATM
#  oc patch oauth/cluster --type=json \
#    -p '[{"op": "add", "path": "/spec/identityProviders/0", "value": {"name":"che-htpasswd","mappingMethod":"claim","type":"HTPasswd","htpasswd":{"fileData":{"name":"che-htpass-secret"}}}}]'
  oc apply -f "$SCRIPT_DIR/resources/htpasswdProvider.yaml"

  oc adm policy add-cluster-role-to-user cluster-admin che-user

  echo -e "[INFO] Waiting for htpasswd auth to be working up to 5 minutes"
  CURRENT_TIME=$(date +%s)
  ENDTIME=$(($CURRENT_TIME + 300))
  while [ $(date +%s) -lt $ENDTIME ]; do
      if oc login -u che-user -p user --insecure-skip-tls-verify=false; then
          break
      fi
      sleep 10
  done
}

startHappyPathTest() {
  # patch happy-path-che.yaml 
  ECLIPSE_CHE_URL=http://$(oc get route -n "${CHE_NAMESPACE}" che -o jsonpath='{.status.ingress[0].host}')
  TS_SELENIUM_DEVWORKSPACE_URL="${ECLIPSE_CHE_URL}/#${HAPPY_PATH_TEST_PROJECT}"
  HAPPY_PATH_POD_FILE=${SCRIPT_DIR}/resources/pod-che-happy-path.yaml
  sed -i "s@CHE_URL@${ECLIPSE_CHE_URL}@g" ${HAPPY_PATH_POD_FILE}
  sed -i "s@WORKSPACE_ROUTE@${TS_SELENIUM_DEVWORKSPACE_URL}@g" ${HAPPY_PATH_POD_FILE}
  sed -i "s@CHE-NAMESPACE@${CHE_NAMESPACE}@g" ${HAPPY_PATH_POD_FILE}

  echo "[INFO] Applying the following patched Che Happy Path Pod:"
  cat ${HAPPY_PATH_POD_FILE}
  echo "[INFO] --------------------------------------------------"
  oc apply -f ${HAPPY_PATH_POD_FILE}
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

startHappyPathTest

echo "Waiting until happy path pod finished"
oc logs -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c happy-path-test -f
# just to sleep
sleep 3

echo "Downloading test report."
mkdir -p /tmp/e2e
oc rsync -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME}:/tmp/e2e/report/ /tmp/e2e -c download-reports
oc exec -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c download-reports -- touch /tmp/done
cp -r /tmp/e2e ${ARTIFACT_DIR}
EXIT_CODE=$(oc logs -n ${CHE_NAMESPACE} ${HAPPY_PATH_POD_NAME} -c happy-path-test | grep EXIT_CODE)
if [[ ${EXIT_CODE} != "+ EXIT_CODE=0" ]]; then
    echo "[ERROR] Happy-path test failed."
    exit 1
fi
