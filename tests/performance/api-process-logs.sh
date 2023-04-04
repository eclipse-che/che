#!/bin/bash
set -e

function average() {
  local arr=("$@")  # copy arguments to local array
  local sum=0
  for i in "${arr[@]}"; do
    sum=$((sum + i))
  done
  echo "$((sum / ${#arr[@]}))"
}

# Create sum up file for gathering logs
results="./reports/results.txt"
rm -rf $results || true

wst_array=() # workspace starting time
bt_array=()  # build time
passed=0     # number of passed tests
failed=0     # number of failed tests

for d in reports/load-test*.txt ; do
  workspaceStartingTime=$(cat $d | grep "Workspace started" | grep -o '[0-9]\+')
  if [ -n $workspaceStartingTime ]; then
    passed=$((passed+1))
    wst_array+=($workspaceStartingTime)
  else
    failed=$((failed+1))
  fi

  buildTime=$(cat $d | grep "Command succeeded in" | grep -o '[0-9]\+')
  if [ -n $workspaceStartingTime ]; then
    bt_array+=($buildTime)
  fi    
done

sorted_array=($(echo "${wst_array[@]}" | tr ' ' '\n' | sort -n | tr '\n' ' '))
wsStartMin=${sorted_array[0]}
wsStartMax=${sorted_array[${#sorted_array[@]}-1]}
wsStartAvr=$(average "${sorted_array[@]}")

# Fill out load testing report
echo "Load testing for $USER_COUNT users x $COMPLETITIONS_COUNT workspaces took $1 seconds" >> $results
echo "$passed tests passed" >> $results
echo "$failed tests failed" >> $results

echo "Workspace startup time: " >> $results
echo "min: $wsStartMin second" >> $results
echo "avr: $wsStartAvr second" >> $results
echo "max: $wsStartMax second" >> $results

sorted_array=($(echo "${bt_array[@]}" | tr ' ' '\n' | sort -n | tr '\n' ' '))
buildTimeMin=${sorted_array[0]}
buildTimeMax=${sorted_array[${#sorted_array[@]}-1]}
buildTimeAvr=$(average "${sorted_array[@]}")

echo "Project build time:" >> $results
echo "min: $buildTimeMin second" >> $results
echo "avr: $buildTimeAvr second" >> $results
echo "max: $buildTimeMax second" >> $results

cat $results