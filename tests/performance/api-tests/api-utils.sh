#!/bin/bash
set -e

# This function starts workspace.
#
# Arguments:
#   $1: DevSpaces URL
#   $2: path to devfile in devfile-registry
#   $3: workspace name
function startWorkspace() {
  curl --insecure $1 -o devfile.yaml
  echo "  routingClass: che" >> devfile.yaml
  cat devfile.yaml
  oc apply -f devfile.yaml

  start=$(date +%s)
  oc wait --for=condition=Ready dw $2 --timeout=360s
  end=$(date +%s)
  echo "Workspace started in $(($end - $start)) seconds"
}

# This function checks that project was imported.
#
# Arguments:
#   $1: workspace name
#   $2: container name
#   $3: project name
function testProjectImported() {
  export WS_ID=$(oc get dw $1 --template='{{.status.devworkspaceId}}')
  export POD_NAME=$(oc get pods | grep $WS_ID | awk '{print $1}')

  # Test if workspace project is imported into workspace
  echo "---- Test project is imported ----"
  oc exec $POD_NAME -c $2 -- sh -c "pwd && ls -la && ls -la $3"
}

# This function starts command in workspace pod.
#
# Arguments:
#   $1: workspace name
#   $2: container name
#   $3: command to execute
#   $4: expected command output
function testCommand() {
  export WS_ID=$(oc get dw $1 --template='{{.status.devworkspaceId}}')
  export POD_NAME=$(oc get pods | grep $WS_ID | awk '{print $1}')

  echo "---- Test #$3# command ----"
  start=$(date +%s)
  export LOG=$(oc exec $POD_NAME -c $2 -- /bin/bash -c "$3")
  end=$(date +%s)

  echo $LOG

  if echo $LOG | grep -q "$4"; then
    echo "Command succeeded in $(($end - $start)) seconds"
  else
    echo "Command failed."
    exit 1
  fi
}

# This function deletes created workspace(dw and dwt objects).
#
# Arguments:
#   $1: workspace name
function deleteWorkspace() {
  oc delete dw $1 || true
  oc delete dwt che-code-$1 || true
}
