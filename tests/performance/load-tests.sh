#!/bin/bash
set -e

# set constants and defaults
TIMESTAMP=$(date +%s)
TEST_FOLDER=$(pwd)
LOG_LEVEL="INFO"
SERVER_SETTING="oauth"
TEST_SUITE="load-test"
POD_SPEC="pod-oauth.yaml"

function printHelp {
  YELLOW="\\033[93;1m"
  WHITE="\\033[0;1m"
  GREEN="\\033[32;1m"
  NC="\\033[0m" # No Color
  
  echo -e "${YELLOW}$(basename "$0") ${WHITE}[-u <username>] [-n <number-of-users>] [-p <passwd>] [-r <url>] [-f <folder>] [-t <workspace_starts>]" 
  echo -e "\n${NC}Script for running load tests against Che 7."
  echo -e "${GREEN}where:${WHITE}"
  echo -e "-u    base username"
  echo -e "-p    common password"
  echo -e "-n    number of users ${NC} usernames will be set in format <username>1, <username>2, ..."${WHITE}
  echo -e "-i    image with test"
  echo -e "-r    URL of Che"
  echo -e "-f    full path to folder ${NC} all reports will be saved in this folder"${WHITE}
  echo -e "-c    credential file ${NC} with usernames and passwords in *.csv format: \`user,pass\`"${WHITE}
  echo -e "-t    count on how many times one user should run a workspace"
  echo -e "-l    log level for test [ 'INFO' (default), 'DEBUG', 'TRACE' ]" 
  echo -e "-s    setting of CRW [ 'oauth' (default), 'no-oauth'] "
  echo -e "-v    cluster URL to obtain OC user token"
  echo -e "-w    test suite ${NC} default to load-test"${NC}
}

oc whoami 1>/dev/null
if [ $? -gt 0 ] ; then
  echo "ERROR: You are not logged! Please login to oc before running this script again."
  exit 1
fi

echo "You are logged in OC: $(oc whoami -c)"
while getopts "c:f:hi:l:n:p:r:t:u:s:v:w:" opt; do 
  case $opt in
    h) printHelp
      exit 0
      ;;
    c) export CRED_FILE=$OPTARG
      ;;
    f) export FOLDER=$OPTARG
      ;;
    i) export TEST_IMAGE=$OPTARG
      ;;
    l) export LOG_LEVEL=$OPTARG
      ;;
    n) export USER_COUNT=$OPTARG
      ;;
    p) export PASSWORD=$OPTARG
      ;;
    r) export URL=$OPTARG
      ;;
    s) export SERVER_SETTING=$OPTARG
      ;;
    t) export COMPLETITIONS_COUNT=$OPTARG
      ;;
    u) export USERNAME=$OPTARG
      ;;
    v) export cheClusterUrl=$OPTARG
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

function exists {
  resource=$1
  name=$2
  if ( oc get $1 $2 > /dev/null 2>&1 ); then
    return 0
  else 
    return 1
  fi
}

# check that all parameters are set
if [ -z $CRED_FILE ]; then
  if [ -z $USERNAME ] || [ -z $PASSWORD ] || [ -z $USER_COUNT ]; then
    echo "ERROR: No credentials given! You need to set username, password and user count or set credentials file."
    printHelp
    exit 1
  fi
fi

if [ -z $URL ] || [ -z $FOLDER ] || [ -z $TEST_IMAGE ]; then
  echo "Some parameters are not set! Exitting load tests." 
  printHelp
  exit 1
else
  echo "Running load tests, result will be stored in $FOLDER in $TIMESTAMP subfolder."
fi

if [ -z $COMPLETITIONS_COUNT ]; then
  echo "Parameter -t wasn't set, setting completitions count to 1."
  COMPLETITIONS_COUNT=1
fi

# ----------- PREPARE ENVIRONMENT ----------- #
echo "-- Preparing environment."
# create pvc
clean_pvc=false
if ( exists pvc load-test-pvc ); then
  echo "PVC load-test-pvc already exists. Reusing and cleaning PVC."
  clean_pvc=true
else
  oc create -f pvc.yaml
fi

# create ftp server
if ( exists pod ftp-server ); then
  echo "Pod ftp-server already exists. Skipping creation."
else
  oc create -f ftp-server.yaml
fi

# create service
if ( exists service load-tests-ftp-service ); then
  echo "Service load-tests-ftp-service already exists. Skipping creation."
else
  oc create -f ftp-service.yaml
fi

# load users if credential file was provided
if [ ! -z $CRED_FILE ]; then
  cred_file_size=$(wc -l $CRED_FILE)
fi

# wait for ftp-server to be running
echo "wait for ftp server to be running"
status=$(oc get pod ftp-server | awk '{print $3}' | tail -n 1)
while [[ $status != "Running" ]]
do
  echo "ftp-server is not running, sleep 5 sec and re-check"
  sleep 5
  status=$(oc get pod ftp-server | awk '{print $3}' | tail -n 1)
done
echo "ftp-server pod is running"

# clean pvc
if [[ $clean_pvc == true ]]; then
  oc exec ftp-server -- rm -rf /home/vsftpd/user/*
fi

# setup and run vsftpd in ftp-server pod
oc exec ftp-server -- sed -i 's/connect_from_port_20/#connect_from_port_20/' /etc/vsftpd/vsftpd.conf
oc exec ftp-server -- sed -i 's/ftp_data_port/#ftp_data_port/' /etc/vsftpd/vsftpd.conf
oc exec ftp-server -- sh /usr/sbin/run-vsftpd.sh &

# set common variables to template.yaml
if [ $SERVER_SETTING == "multi-host" ]; then
  SINGLE_HOST="false"
elif [ $SERVER_SETTING == "single-host"]; then
  SINGLE_HOST="true"
fi

echo "set common variables to template.yaml"
cp $POD_SPEC template.yaml
parsed_url=$(echo $URL | sed 's/\//\\\//g')
parsed_image=$(echo $TEST_IMAGE | sed 's/\//\\\//g')

sed -i "s/REPLACE_COMPLETITIONS/$COMPLETITIONS_COUNT/g" template.yaml
sed -i "s/REPLACE_URL/\"$parsed_url\"/g" template.yaml
sed -i "s/REPLACE_TIMESTAMP/\"$TIMESTAMP\"/g" template.yaml
sed -i "s/REPLACE_IMAGE/\"$parsed_image\"/g" template.yaml
sed -i "s/REPLACE_LOG_LEVEL/$LOG_LEVEL/g" template.yaml
sed -i "s/REPLACE_SINGLE_HOST/\"$SINGLE_HOST\"/g" template.yaml
if [ $TEST_SUITE == "load-test" ]; then
  sed -i_.bak '/USERSTORY/d' template.yaml
  sed -i "s/REPLACE_TEST_SUITE/$TEST_SUITE/g" template.yaml
  sed -i "s/MEMORY_REQUEST/\"900Mi\"/g" template.yaml
  sed -i "s/MEMORY_LIMIT/\"900Mi\"/g" template.yaml
else 
  sed -i_.bak '/TEST_SUITE/d' template.yaml
  sed -i "s/REPLACE_USERSTORY/$TEST_SUITE/g" template.yaml
  sed -i "s/MEMORY_REQUEST/\"1300Mi\"/g" template.yaml
  sed -i "s/MEMORY_LIMIT/\"2500Mi\"/g" template.yaml
fi

echo "File template.yaml that will be used for generating jobs:"
cat template.yaml

# ----------- RUNNING TEST ----------- #
echo "-- Running pods with tests."
echo "Searching for already created jobs..."
jobs=$(oc get jobs -l group=load-tests)
if [[ ! -z $jobs ]]; then
  echo "[WARNING] There are some jobs already running. Removing all jobs with label \"load-tests\" and creating new ones."
  oc delete jobs -l group=load-tests
  oc delete pods -l group=load-tests
fi

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
    echo "Getting OC token for this cluster url: ${cheClusterUrl}"
    oc login ${cheClusterUrl} -u $USERNAME$users_assigned -p $PASSWORD
    oc_token=$(oc whoami -t)
    sed -i "s/REPLACE_TOKEN/$oc_token/g" final.yaml
    oc config use-context $executor_context
    oc create -f final.yaml
  done
fi
if [ ! -z $CRED_FILE ]; then
  oldifs=$IFS
  while IFS=, read -r cred_file_username cred_file_pass
  do
    users_assigned=$((users_assigned+1))
    cp template.yaml final.yaml
    sed -i "s/REPLACE_NAME/load-test-$users_assigned/g" final.yaml
    sed -i "s/REPLACE_USERNAME/$cred_file_username/g" final.yaml
    sed -i "s/REPLACE_PASSWORD/$cred_file_pass/g" final.yaml
    oc create -f final.yaml
  done < $CRED_FILE
  IFS=$oldifs
fi

echo "-- Waiting for all pods to be completed."

#waiting for jobs to be completed
all_completed=false
while [ $all_completed == false ] 
do
  sleep 20
  all_completed=true
  for job_name in $(oc get jobs -o name )
  do
    if [ $(oc get $job_name -o json | jq .status.completionTime) == null ]; then
      echo "Some jobs are still not completed. Waiting for 20 seconds."
      all_completed=false
      break
    fi
  done
done

echo "All jobs are completed!"

statuses=""
for p in $(oc get pods -l group=load-tests -o name)
do
  status=$(oc get $p | awk '{print $3}' | tail -n 1)
  statuses="$statuses $status"
done
echo "Pods ended with those statuses: $statuses"

# ----------- GATHERING LOGS ----------- #
echo "-- Gathering logs."

echo "Syncing files from PVC to local folder."
mkdir $FOLDER/$TIMESTAMP
cd $FOLDER/$TIMESTAMP
oc rsync --no-perms ftp-server:/home/vsftpd/user/ $FOLDER/$TIMESTAMP
echo "Tar files rsynced, untarring..."
for filename in *.tar; do 
  tar xf $filename; 
done
rm ./*.tar
cd ..
set +x

# ----------- CLEANING ENVIRONMENT ----------- #
echo "-- Cleaning environment."

oc delete jobs -l group=load-tests
oc delete pods -l group=load-tests

oc delete pod ftp-server
oc delete service load-tests-ftp-service
oc delete pvc load-test-pvc

# ----------- PROCESSING TEST RESULTS ----------- #
$TEST_FOLDER/process-logs.sh -t $TIMESTAMP -f $FOLDER -u $users_assigned -c $COMPLETITIONS_COUNT -s $SERVER_SETTING -v $TEST_SUITE
