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

# --- create sum up file for gathering logs ---
results="./reports/results.txt"
rm -rf $results || true

wst_array=()
bt_array=()

for d in reports/load-test*.txt ; do
    workspaceStartingTime=$(cat $d | grep "Workspace started" | grep -o '[0-9]\+')
    wst_array+=($workspaceStartingTime)

    buildTime=$(cat $d | grep "Build succeeded" | grep -o '[0-9]\+')
    bt_array+=($buildTime)
done

sorted_array=($(echo "${wst_array[@]}" | tr ' ' '\n' | sort -n | tr '\n' ' '))
wsStartMin=${sorted_array[0]}
wsStartMax=${sorted_array[${#sorted_array[@]}-1]}
wsStartAvr=$(average "${sorted_array[@]}")

# Fill load-testing report
echo "Load testing for $USER_COUNT user x $COMPLETITIONS_COUNT workspaces took $1 seconds" >> $results

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