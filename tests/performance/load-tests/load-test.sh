#!/bin/bash

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Do cleanup if the script is interrupted or fails
trap cleanup ERR SIGINT
# Detect the operating system
OS="$(uname)"

# Capture start time
start=$(date +%s)
# Current namespace where the script is running
current_namespace=$(kubectl config view --minify --output 'jsonpath={..namespace}')
start_separately=false
# Default value for test namespace name
test_namespace_name=load-test-namespace-
# Default value for test devworkspace name
dw_name=load-test-dw-
# default values for label selector
label_type="test-type"
label_key="load-test"

function print() {
  echo -e "${GREEN}$1${NC}"
}

function print_error() {
  echo -e "${RED}$1${NC}"
}

# Function to display help information
function display_help() {
  echo "Usage: $0 [OPTIONS]"
  echo "Options:"
  echo "  -t <SECONDS>   Set the timeout in seconds (default: 120)"
  echo "  -c <COUNT>     Set the number of workspaces to start (default: 3)"
  echo "  -l             Set the link to the devworkspace.yaml file"
  echo "  -s             Start workspaces in separate namespaces(one workspace per namespace)"
  echo "  --help         Display this help message"
  exit 0
}

function parseArguments() {
  for arg in "$@"; do
    case $arg in
    # Check for --help argument
    --help)
      display_help
      ;;
    esac
  done

  while getopts "t:c:l:sh" opt; do
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
    l)
      export DEVWORKSPACE_LINK=$OPTARG
      ;;
    s)
      export start_separately=true
      ;;
    h)
      display_help
      ;;
    \?)
      print_error "Invalid option -c. Try for example something like ./load-test.sh -c 7"
      exit 1
      ;;
    esac
  done
}

function cleanup() {
  echo "Clean up the environment"

  if [ $start_separately = true ]; then
    echo "Delete test namespaces"
    kubectl delete namespace --selector=$label_type=$label_key >/dev/null 2>&1 || true
  else
    kubectl delete dw --selector=$label_type=$label_key -n $current_namespace >/dev/null 2>&1 || true
  fi
}

function checkScriptVariables() {
  # Set the number of workspaces to start
  if [ -z $COMPLETITIONS_COUNT ]; then
    echo "Parameter -c wasn't set, setting completitions count to 3."
    export COMPLETITIONS_COUNT=3
    echo "Parameter -c was set to  $COMPLETITIONS_COUNT ."
  fi

  # Set the number of timeouts for waiting for workspaces to start
  if [ -z $WORKSPACE_IDLE_TIMEOUT ]; then
    echo "Parameter -t wasn't set, setting timeout to 120 second."
    export WORKSPACE_IDLE_TIMEOUT=120
  else
    echo "Parameter -t was set to $WORKSPACE_IDLE_TIMEOUT second."
  fi

  # Get devworkspace yaml from link if it is set
  # example - https://raw.githubusercontent.com/eclipse/che/main/tests/performance/load-tests/samples/simple-ephemeral.yaml
  if [ -n "$DEVWORKSPACE_LINK" ]; then
    if curl --fail --insecure "$DEVWORKSPACE_LINK" -o devworkspace.yaml; then
      echo "Download succeeded, saved to devworkspace.yaml file."

      echo "Check the devworkspace.yaml file content correctness."
      if ! kubectl apply -f devworkspace.yaml --dry-run=server; then
        print_error "Devworkspace.yaml file is not correct."
        exit 1
      fi
    else
      print_error "Download of $DEVWORKSPACE_LINK file failed"
      exit 1
    fi
  else
    print "Local devworkspace.yaml file will be used."
    cp -f samples/simple-ephemeral.yaml devworkspace.yaml
  fi
}

function getDwStartingTime() {
  start_time=$(kubectl get dw $dw_name$1 -n $2 --template='{{range .status.conditions}}{{if eq .type "Started"}}{{.lastTransitionTime}}{{end}}{{end}}')
  end_time=$(kubectl get dw $dw_name$1 -n $2 --template='{{range .status.conditions}}{{if eq .type "Ready"}}{{.lastTransitionTime}}{{end}}{{end}}')
  start_timestamp=$(getTimestamp $start_time)
  end_timestamp=$(getTimestamp $end_time)
  dw_starting_time=$((end_timestamp - start_timestamp))

  print "Devworkspace $dw_name$1 in $2 namespace starting time: $dw_starting_time seconds"
  echo $dw_starting_time >>logs/sum.log
  kubectl delete dw $dw_name$1 -n $2 >/dev/null 2>&1
}

function precreateNamespaces() {
  if [ $start_separately = true ]; then
    echo "Create test namespaces in parallel"
    for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
      namespace=$test_namespace_name$i
      kubectl create namespace $namespace &
      # Store the PID to wait for later
      pids[$i]=$!
    done
    
    # Wait for all namespace creation to complete
    for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
      wait ${pids[$i]}
    done
    
    # Label all namespaces in parallel
    echo "Label test namespaces"
    for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
      namespace=$test_namespace_name$i
      oc label namespace $namespace $label_type=$label_key >/dev/null 2>&1 &
    done
    wait
  fi
}

function getTimestamp() {
  if [ "$OS" = "Darwin" ]; then
    date -j -f "%Y-%m-%dT%H:%M:%S" "$1" "+%s"
  else
    [ "$OS" = "Linux" ]
    date -d $1 +%s
  fi
}

function runTest() {
  # add label to devworkspace.yaml
  cat devworkspace.yaml | awk -v key="$label_type" -v value=" $label_key" '/metadata:/{$0=$0"\n  labels:\n    "key":"value}1' >patched-dw.yaml

  # start COMPLETITIONS_COUNT workspaces in parallel
  namespace=$current_namespace
  for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
    if [ $start_separately = true ]; then
      namespace=$test_namespace_name$i
    fi
    awk '/name:/ && !modif { sub(/name: .*/, "name: '"$dw_name$i"'"); modif=1 } {print}' patched-dw.yaml | kubectl apply -n $namespace -f - &
  done
  wait

  # wait for all workspaces to be started
  echo "Wait for all workspaces are started"
  namespace=$current_namespace
  for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
    if [ $start_separately = true ]; then
      namespace=$test_namespace_name$i
    fi
    kubectl wait --for=condition=Ready "dw/$dw_name$i" --timeout=${WORKSPACE_IDLE_TIMEOUT}s -n $namespace || true &
  done
  wait

  # Delete logs on file system if it exists
  rm -f logs/*
  # Create logs directory
  mkdir logs || true
  touch logs/sum.log

  # Get events from all cluster in start_separately mode and only from current namespace in default mode
  if [ $start_separately = true ]; then
    kubectl get events --field-selector involvedObject.kind=Pod --all-namespaces >logs/events.log
  else
    kubectl get events --field-selector involvedObject.kind=Pod >logs/events.log
  fi

  total_time=0
  succeeded=0
  echo "Calculate average workspaces starting time"
  namespace=$current_namespace
  for ((i = 1; i <= $COMPLETITIONS_COUNT; i++)); do
    if [ $start_separately = true ]; then
      namespace=$test_namespace_name$i
    fi

    if ! kubectl get dw $dw_name$i -n $namespace &>/dev/null; then
      echo "$dw_name$i devworkspace does not exist. Moving to the next iteration."
      continue
    fi

    if [ "$(kubectl get dw $dw_name$i -n $namespace --template='{{.status.phase}}')" == "Running" ]; then
      getDwStartingTime $i $namespace &
      succeeded=$((succeeded + 1))
    else
      print_error "Timeout waiting for $dw_name$i to become ready or an error occurred."
      devworkspace_id=$(kubectl get dw $dw_name$i -n $namespace --template='{{.status.devworkspaceId}}')
      kubectl describe dw $dw_name$i -n $namespace >logs/$dw_name$i-describe.log
      cat logs/events.log | grep $devworkspace_id >logs/$dw_name$i-$devworkspace_id-events.log || true
    fi
  done

  wait
}

function printResults() {
  # Calculate average workspace starting time
  while IFS= read -r line; do
    ((total_time += line))
  done <"logs/sum.log"

  echo "==================== Test results ===================="
  if [ $succeeded -eq 0 ]; then
    print_error "No workspaces started successfully."
  else
    print "Average workspace starting time for $succeeded workspaces from $COMPLETITIONS_COUNT started: $((total_time / succeeded)) seconds"
  fi
  print "$((COMPLETITIONS_COUNT - succeeded)) workspaces failed. See failed workspace pod logs in the current folder for details."

  # Calculate and display the elapsed time
  end=$(date +%s)
  print "Elapsed time: $((end - start)) seconds"
}

parseArguments "$@"
checkScriptVariables "$@"
cleanup

precreateNamespaces
runTest
printResults

cleanup
