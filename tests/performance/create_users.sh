#!/bin/bash
oc whoami 1>/dev/null
if [ $? -gt 0 ] ; then
  echo "ERROR: You are not logged! Please login to oc before running this script again."
  exit 1
fi

echo "You are logged in OC: $(oc whoami -c )"

function printHelp {
  echo "$(basename "$0") [-n <base_username>] [-m <user_password>] [-s <starting_index>] [-c <user_count>] [-e <tested_environment]" 
  echo -e "Script for creating users on Che or CRW."
  echo -e "where:"
  echo -e "-c    number of users that should be created"
  echo -e "-e    tested environment - can be Che or CRW"
  echo -e "-n    base part of username (e.g. 'user')"
  echo -e "-m    common password"
  echo -e "-s    starting index of a users (generated name will look like e.g. 'user1')"
  echo -e "optional:"
  echo -e "-a    admin username, default to 'admin'"
  echo -e "-p    admin password, default to 'admin'"
}

while getopts "hn:n:m:s:c:e:a:p:" opt; do 
  case $opt in
    h) printHelp
      exit 0
      ;;
    e) export TESTED_ENV=$OPTARG
      ;;
    n) export USERNAME=$OPTARG
      ;;
    m) export PASSWORD=$OPTARG
      ;;
    s) export STARTING_INDEX=$OPTARG
      ;;
    c) export USER_COUNT=$OPTARG
      ;;
    a) export ADMIN_USERNAME=$OPTARG
      ;;
    p) export ADMIN_PASS=$OPTARG
      ;;
    \?)
      echo "\"$opt\" is an invalid option!"
      exit 1
      ;;
    :)
      echo "Option \"$opt\" needs an argument."
      exit 1
      ;;
  esac
done


# check that all variables are set
if [ -z $USERNAME ] || [ -z $STARTING_INDEX ] || [ -z $USER_COUNT ] || [ -z $PASSWORD ] || [ -z $TESTED_ENV ]; then
  echo "ERROR: All variables must be set. See the text below to learn how to use this script:"
  printHelp
  exit 1
fi

if [ -z $ADMIN_USERNAME ]; then
  ADMIN_USERNAME="admin"
fi
if [ -z $ADMIN_PASS ]; then
  ADMIN_PASS="admin"
fi

if [ "$TESTED_ENV" == "Che" ]; then
  REALM="che"
else 
  if [ "$TESTED_ENV" = "CRW" ]; then
    REALM="codeready"
  else
    echo "ERROR: Wrong environment set. Can be only 'Che' or 'CRW'. You used: $TESTED_ENV."
    exit 1
  fi
fi

KEYCLOAK_URL=$(oc get route/keycloak -o jsonpath='{.spec.host}')
KEYCLOAK_BASE_URL="https://${KEYCLOAK_URL}/auth"

# get admin token
echo "Getting admin token"
ADMIN_ACCESS_TOKEN=$(curl -X POST -k -H "Content-Type: application/x-www-form-urlencoded" -d "username=${ADMIN_USERNAME}" -d "password=${ADMIN_PASS}" -d "grant_type=password" -d "client_id=admin-cli" $KEYCLOAK_BASE_URL/realms/master/protocol/openid-connect/token | jq -r .access_token)

if [ -z $ADMIN_ACCESS_TOKEN ]; then
  echo "ERROR: Could not obtain admin access token."
  exit 1
else
  echo "Admin token obtained successfully."
fi

for (( i=0; i<$USER_COUNT; i++))
do
  user_number=$(( i + $STARTING_INDEX ))
  TEST_USERNAME="${USERNAME}${user_number}"
  USER_JSON={\"username\":\"${TEST_USERNAME}\",\"enabled\":true,\"emailVerified\":true,\"email\":\"test${user_number}@user.aa\"}

  echo "Creating user"
  curl -X POST -k $KEYCLOAK_BASE_URL/admin/realms/$REALM/users -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" -H "Content-Type: application/json" -d "${USER_JSON}"
  USER_ID=$(curl -X GET -k $KEYCLOAK_BASE_URL/admin/realms/$REALM/users?username=${TEST_USERNAME} -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" | jq -r .[0].id)
  echo "User id: $USER_ID"
  
  echo "Updating password"
  CREDENTIALS_JSON="{\"type\":\"password\",\"value\":\"${PASSWORD}\",\"temporary\":false}"
  curl -X PUT -k $KEYCLOAK_BASE_URL/admin/realms/$REALM/users/${USER_ID}/reset-password -H "Authorization: Bearer ${ADMIN_ACCESS_TOKEN}" -H "Content-Type: application/json" -d "${CREDENTIALS_JSON}"
done
 
