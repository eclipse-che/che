#!/bin/bash
# set -x
set -e

function aaa(){
  return 0

  echo "11111"
}

function bbb(){
  echo "It has been launched"
}


aaa
bbb

