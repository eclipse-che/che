#!/bin/bash

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

function print() {
  echo -e "${GREEN}$1${NC}"
}

function print_error() {
  echo -e "${RED}$1${NC}"
}

function cleanup() {
  echo "Clean up the environment"
  kubectl delete dw --all > /dev/null
  kubectl delete dwt --all > /dev/null
}

while getopts "c:" opt; do 
  case $opt in
    c) export COMPLETITIONS_COUNT=$OPTARG
      ;;
    \?) # invalid option
      exit 1
      ;;
    :)
      echo "Option \"$opt\" needs an argument."
      exit 1
      ;;
  esac
done

# Set the number of workspaces to start
if [ -z $COMPLETITIONS_COUNT ]; then
  echo "Parameter -c wasn't set, setting completitions count to 3."
  export COMPLETITIONS_COUNT=3
fi

# Delete all dw and dwt objects from test namespace
cleanup

# Delete logs
rm dw* || true  > /dev/null

for ((i=1; i<=$COMPLETITIONS_COUNT; i++)); do
  cat devfile.yaml | sed "0,/name: code-latest/s//name: dw$i/" | kubectl apply -f -  &
done

wait

for ((i=1; i<=$COMPLETITIONS_COUNT; i++)); do
  kubectl wait --for=condition=Ready "dw/dw$i" --timeout=120s || true &
done

wait

total_time=0
succeeded=0
echo  "Wait for all workspaces are started and calculate average workspaces starting time"
for ((i=1; i<=$COMPLETITIONS_COUNT; i++)); do
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
    kubectl describe dw dw$i > dw$i-log.log
    kubectl logs $(kubectl get dw dw$i --template='{{.status.devworkspaceId}}') > dw$i-pod.log || true
fi
done

wait

print "==================== Test results ===================="
print "Average workspace starting time for $succeeded workspaces from $COMPLETITIONS_COUNT started: $((total_time / succeeded)) seconds"
print "$((COMPLETITIONS_COUNT - succeeded)) workspaces failed. See failed workspace pod logs in the current folder for details."

trap cleanup ERR EXIT
