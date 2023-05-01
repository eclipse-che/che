#!/bin/bash
set -e

while getopts "i:n:c:p:o:b:u:s:" opt; do 
  case $opt in
    i) export TEST_IMAGE=$OPTARG
      ;;
    n) export USER_COUNT=$OPTARG
      ;;
    c) export COMPLETITIONS_COUNT=$OPTARG
      ;;
    o) export OCP_URL=$OPTARG
      ;;
    b) export BASE_URL=$OPTARG
      ;;
    u) export USERNAME=$OPTARG
      ;;
    p) export PASSWORD=$OPTARG
      ;;      
    s) export TEST_SUITE=$OPTARG
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

if [ -z $COMPLETITIONS_COUNT ]; then
  echo "Parameter -t wasn't set, setting completitions count to 1."
  export COMPLETITIONS_COUNT=1
fi

if [ -z $USER_COUNT ]; then
  echo "Parameter -t wasn't set, setting users number to 1."
  export USER_COUNT=1
fi

if [ -z $TEST_SUITE ]; then
  echo "Parameter -s wasn't set, setting testing suite to php."
  export TEST_SUITE="php"
fi

echo "Clean up"
oc delete jobs -l group=load-tests --all-namespaces || true
oc delete pods -l group=load-tests --all-namespaces || true

# Delete all workspaces in test users namespaces
for ((i=1; i<=$USER_COUNT; i++)); do
  oc delete dw --all -n user$i-devspaces || true
done

# Set common variables to template.yaml
apiTest="https://raw.githubusercontent.com/eclipse/che/patchDevfile/tests/performance/api-tests/api-test-$TEST_SUITE.sh"
rm template.yaml || true
cp api-pod.yaml template.yaml
sed -i "s/REPLACE_COMPLETITIONS/$COMPLETITIONS_COUNT/g" template.yaml
sed -i "s#REPLACE_OCP_SERVER_URL#$OCP_URL#g" template.yaml  
sed -i "s#REPLACE_BASE_URL#$BASE_URL#g" template.yaml
sed -i "s#REPLACE_API_TEST#$apiTest#g" template.yaml

oc project load-tests || oc new-project load-tests

# Set variables specific for each pod and create pods
users_assigned=0
executor_context=$(oc whoami -c)
if [ ! -z $USER_COUNT ]; then
  while [ $users_assigned -lt $USER_COUNT ] 
  do
    users_assigned=$((users_assigned+1))
    cp template.yaml final.yaml
    sed -i "s/REPLACE_NAME/load-test-$users_assigned/g" final.yaml
    sed -i "s/REPLACE_USERNAME/$USERNAME$users_assigned/g" final.yaml
    sed -i "s/REPLACE_PASSWORD/$PASSWORD/g" final.yaml
    oc create -f final.yaml -n load-tests
  done
fi

echo "-- Waiting for all pods to be completed."
start=$(date +%s)
# Waiting for jobs to be completed
all_completed=false
while [ $all_completed == false ] 
do
  sleep 5
  all_completed=true
  for job_name in $(oc get jobs -o name )
  do
    if [ $(oc get $job_name -o json | jq .status.completionTime) == null ]; then
      echo "Some jobs are still not completed. Waiting for 5 seconds."
      all_completed=false
      break
    fi
  done
done

echo "All jobs are completed!"
end=$(date +%s)

rm -rf reports || true
mkdir reports
statuses=""
for p in $(oc get pods -l group=load-tests -o name)
do
  name=$(oc get $p | awk '{print $1}' | tail -n 1)
  oc logs $name >> ./reports/$name.txt
  status=$(oc get $p | awk '{print $3}' | tail -n 1)
  statuses="$statuses $status"
done
echo "Pods ended with those statuses: $statuses"

# ----------- GATHERING LOGS ----------- #
./api-process-logs.sh $(($end - $start))

echo "Clean up"
oc delete jobs -l group=load-tests --all-namespaces || true
oc delete pods -l group=load-tests --all-namespaces || true
