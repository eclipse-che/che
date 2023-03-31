#!/bin/bash
set -e

# ./api-load-tests.sh -u user -p load-user -n 10 -c 1 -b https://devspaces.apps.ocp410-sskoryk.crw-qe.com -o https://api.ocp410-sskoryk.crw-qe.com:6443
echo "Clean up"
oc delete jobs -l group=load-tests --all-namespaces || true
oc delete pods -l group=load-tests --all-namespaces || true

while getopts "c:f:hi:l:n:p:r:t:b:o:u:s:v:w:" opt; do 
  case $opt in
    i) export TEST_IMAGE=$OPTARG
      ;;
    n) export USER_COUNT=$OPTARG
      ;;
    c) export COMPLETITIONS_COUNT=$OPTARG
      ;;
    p) export PASSWORD=$OPTARG
      ;;
    o) export OCP_URL=$OPTARG
      ;;
    b) export BASE_URL=$OPTARG
      ;;
    u) export USERNAME=$OPTARG
      ;;
    w) export TEST_SUITE=$OPTARG
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

rm template.yaml || true
cp pod.yaml template.yaml
sed -i "s/REPLACE_COMPLETITIONS/$COMPLETITIONS_COUNT/g" template.yaml
# sed -i "s/REPLACE_URL/\"$parsed_url\"/g" template.yaml
# sed -i "s/REPLACE_IMAGE/\"$parsed_image\"/g" template.yaml

oc project load-tests || oc new-project load-tests

# set variables specific for each pod and create pods
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
    
    sed -i "s#REPLACE_OCP_SERVER_URL#$OCP_URL#g" final.yaml  
    sed -i "s#REPLACE_BASE_URL#$BASE_URL#g" final.yaml  
    oc create -f final.yaml -n load-tests
    echo "$users_assigned jobs created."
  done
fi

echo "-- Waiting for all pods to be completed."

start=$(date +%s)
#waiting for jobs to be completed
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
echo "Load testing for $USER_COUNT user x $COMPLETITIONS_COUNT workspaces took $(($end - $start)) seconds"

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


./api-process-logs.sh $(($end - $start))

# echo "Clean up"
# oc delete jobs -l group=load-tests --all-namespaces || true
# oc delete pods -l group=load-tests --all-namespaces || true