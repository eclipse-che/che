#!/bin/bash

CHE_PROPERTIES_PATH="assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/che.properties"
MULTIUSER_PROPERTIES_PATH="assembly/assembly-wsmaster-war/src/main/webapp/WEB-INF/classes/che/multiuser.properties"

HAS_DESCRIPTION=false
cat $CHE_PROPERTIES_PATH $MULTIUSER_PROPERTIES_PATH |
while read -r LINE
do
  if [[ $LINE == '#'* ]]; then
    HAS_DESCRIPTION=true
  elif [[ -z $LINE ]]; then
    HAS_DESCRIPTION=false
  else
    if [[ $HAS_DESCRIPTION == false ]]; then
      echo "Property $LINE seems to be missing a description!"
      exit 1
    fi
    HAS_DESCRIPTION=false
  fi
done
