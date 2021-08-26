# --- PROCESSING LOGS FROM LOAD_TESTS ---

function printHelp {
  echo "Script for processing logs of laod tests."
  echo "-f     folder where logs are stored"
  echo "-t     timestamp of the run"
  echo "-c     completitions count"
  echo "-u     users count"
  echo "-s     server settings"
  echo "-v     test suite"
}

while getopts "f:t:c:u:hs:v:" opt; do 
  case $opt in
    h) printHelp
      ;;
    f) export FOLDER=$OPTARG
      ;;
    t) export TIMESTAMP=$OPTARG
      ;;
    c) export COMPLETITIONS_COUNT=$OPTARG
      ;;
    u) export USERS_COUNT=$OPTARG
      ;;
    s) export SERVER_SETTING=$OPTARG
      ;;
    v) export TEST_SUITE=$OPTARG
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

TEST_COUNT=$((USERS_COUNT*COMPLETITIONS_COUNT))

CURRENT_DIR=$(pwd)

echo "-- Generate sum-up for load tests."

# --- create sum up file for gathering logs ---
sumupFile="$FOLDER/$TIMESTAMP/load-test-sumup.txt"
touch $sumupFile

# gather users with failed tests
failedUsers=""
i=1
failedCounter=0
user_for_getting_test_names=""

cd $FOLDER/$TIMESTAMP
rm -rf "lost+found"
for d in */ ; do
  if [ -d ${d}report ]; then
    failedUsers="$failedUsers ${d%"/"}"
    failedCounter=$((failedCounter + 1))
  else
    if [[ $user_for_getting_test_names == "" ]]; then
      user_for_getting_test_names=${d%"/"}
    fi
  fi
done
cd $CURRENT_DIR

echo "Tests setup: $USERS_COUNT users, each running $COMPLETITIONS_COUNT workspaces" 
echo -e "Tests setup: \n  $USERS_COUNT users, each running $COMPLETITIONS_COUNT workspaces \n  Server settings: $SERVER_SETTING \n  Test suite used: $TEST_SUITE \n \n" > $sumupFile

if [[ $failedUsers == "" ]]; then
  echo "All tests has passed, yay!"
  echo -e "Tests passed for all $TEST_COUNT workspaces, yay! \n" >> $sumupFile
else
  echo "Test failed for $failedCounter/$TEST_COUNT workspaces: $failedUsers"
  echo -e "Test failed for $failedCounter/$TEST_COUNT workspaces: $failedUsers \n" >> $sumupFile
fi

if [[ $user_for_getting_test_names == "" ]]; then
  echo "Tests failed for all users. Skipping generation logs."
  exit
fi

# change \r to \n in files
for file in $(find $FOLDER/$TIMESTAMP -name 'load-test-results.txt'); do
  sed -i 's/\r/\n/g' $file
done

lineCounter=1
tests=$(wc -l < $FOLDER/$TIMESTAMP/$user_for_getting_test_names/load-test-folder/load-test-results.txt)

while [[ $lineCounter -le $tests ]]; do
  sum=0
  min=-1
  max=-1
  count=0
  sed "${lineCounter}q;d" $FOLDER/$TIMESTAMP/$user_for_getting_test_names/load-test-folder/load-test-results.txt | awk -F ':' '{print $1}' >> $sumupFile
  for file in $(find $FOLDER/$TIMESTAMP -name 'load-test-results.txt'); do
    actual=$(sed "${lineCounter}q;d" $file | awk -F ':' '{print $2}' | awk -F ' ' '{ print $1}')
    if [[ -z $actual ]]; then
      continue
    fi  
    sum=$(($sum + $actual))
    if [[ $min == -1 ]]; then
      min=$actual
    else
      if [[ $min -gt $actual ]]; then
        min=$actual
      fi
    fi
    if [[ $max == -1 ]]; then
      max=$actual
    else
      if [[ $max -lt $actual ]]; then
        max=$actual
      fi
    fi
    count=$((count + 1))
  done
  lineCounter=$((lineCounter+1))
  if [[ $count == 0 ]]; then
    echo "No values collected. " >> $sumupFile
  else
    avg=$((sum / count))
    echo "min: $min" >> $sumupFile
    echo "max: $max" >> $sumupFile
    echo "avg: $avg" >> $sumupFile  
  fi
done

END_TIME=$(date +%s)
TEST_DURATION=$((END_TIME-TIMESTAMP))
echo "Tests are done! :) "
echo "Tests lasted $TEST_DURATION seconds."
echo "You can see load tests sum up here: $sumupFile"
