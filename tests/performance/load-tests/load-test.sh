#!/bin/bash

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Do cleanup if the script is interrupted or fails
trap cleanup ERR SIGINT

function print() {
  echo -e "${GREEN}$1${NC}"
}

function print_error() {
  echo -e "${RED}$1${NC}"
}

function cleanup() {
  echo "Clean up the environment"
  kubectl delete dw --all >/dev/null
  kubectl delete dwt --all >/dev/null
}

function parseArguments() {
  while getopts "t:c:" opt; do
    case $opt in
    t)
      export WORKSPACE_IDLE_TIMEOUT=$OPTARG
      ;;
    c) # Check if the argument to -c is a number
      if ! [[ $OPTARG =~ ^[0-9]+$ ]]; then
        print_error "Error: Option -c requires a numeric argument." >&2
        exit 1
      fi
      export COMPLETITIONS_COUNT=$OPTARG
      ;;
    \?)
      print_error "Invalid option -c. Try for example something like ./load-test.sh -c 7"
      exit 1
      ;;
    esac
  done
}

function setCompletitionsCount() {
  # Set the number of workspaces to start
  if [ -z $COMPLETITIONS_COUNT ]; then
    echo "Parameter -c wasn't set, setting completitions count to 3."
    export COMPLETITIONS_COUNT=3
  else
    echo "Parameter -c was set to  $COMPLETITIONS_COUNT ."
  fi

  # Set the number of timeouts for waiting for workspaces to start
  if [ -z $WORKSPACE_IDLE_TIMEOUT ]; then
    echo "Parameter -t wasn't set, setting timeout to 120 second."
    export WORKSPACE_IDLE_TIMEOUT=120
  else
    echo "Parameter -t was set to $WORKSPACE_IDLE_TIMEOUT second."
  fi
}

function runTest() {
  # start COMPLETITIONS_COUNT workspaces in parallel
  for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
    cat devworkspace.yaml | sed "0,/name: code-latest/s//name: dw$i/" | kubectl apply -f - &
  done
  wait

  # wait for all workspaces to be started
  echo "Wait for all workspaces are started"
  for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
    kubectl wait --for=condition=Ready "dw/dw$i" --timeout=${WORKSPACE_IDLE_TIMEOUT}s || true &
  done
  wait

  # Delete logs on file system if it exists
  rm -f logs/dw*

  total_time=0
  succeeded=0
  echo "Calculate average workspaces starting time"
  for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
    if [ "$(kubectl get dw dw$i --template='{{.status.phase}}')" == "Running" ]; then
      start_time=$(kubectl get dw dw$i --template='{{range .status.conditions}}{{if eq .type "Started"}}{{.lastTransitionTime}}{{end}}{{end}}')
      end_time=$(kubectl get dw dw$i --template='{{range .status.conditions}}{{if eq .type "Ready"}}{{.lastTransitionTime}}{{end}}{{end}}')
      start_timestamp=$(date -d $start_time +%s)
      end_timestamp=$(date -d $end_time +%s)
      dw_starting_time=$((end_timestamp - start_timestamp))

      print "Devworkspace dw$i starting time: $dw_starting_time seconds"
      total_time=$((total_time + dw_starting_time))
      succeeded=$((succeeded + 1))
    else
      print_error "Timeout waiting for dw$i to become ready or an error occurred."
      kubectl describe dw dw$i >logs/dw$i-log.log
      kubectl logs $(oc get dw dw$i --template='{{.status.devworkspaceId}}') >logs/dw$i-pod.log || true
    fi
  done

}

function printResults() {
  print "==================== Test results ===================="
  if [ $succeeded -eq 0 ]; then
    print_error "No workspaces started successfully."
    exit 1
  else
    print "Average workspace starting time for $succeeded workspaces from $COMPLETITIONS_COUNT started: $((total_time / succeeded)) seconds"
  fi
  print "$((COMPLETITIONS_COUNT - succeeded)) workspaces failed. See failed workspace pod logs in the current folder for details."
}

parseArguments "$@"
setCompletitionsCount
cleanup
runTest
printResults
cleanup
