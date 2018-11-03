#!/bin/sh
#
# Copyright (c) 2018-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

cd ${HOME}

function human_readable {
    local -i kilobytes=$1;
    if [[ ${kilobytes} -lt 1024 ]]; then
        echo "${kilobytes}KB"
    else
        echo "$(( (kilobytes + 1023)/1024 ))MiB"
    fi
}

# Size before
SIZE_BEFORE=$(du -s . | cut -f1)
LAST_SIZE=${SIZE_BEFORE}

cat /tmp/builder/scripts/cleanup-find | while read line
do
  # skip empty lines
  [ -z "${line}" ] && continue

  if [[ ! ${line} == \#* ]]; then
    printf "Cleaning up pattern ${line}..."   
    find ${home} -name ${line} | xargs rm -rf {}
    SIZE_NOW=$(du -s . | cut -f1)
    printf "free up $(human_readable $((${LAST_SIZE}-${SIZE_NOW})))\n"
    LAST_SIZE=${SIZE_NOW}
  fi

done

cat /tmp/builder/scripts/cleanup-exact | while read line
do
  # skip empty lines
  [ -z "${line}" ] && continue

  if [[ ! ${line} == \#* ]]; then
    printf "Cleaning up ${line}..."   
    rm -rf ${line}
    SIZE_NOW=$(du -s . | cut -f1)
    printf "free up $(human_readable $((${LAST_SIZE}-${SIZE_NOW})))\n"
    LAST_SIZE=${SIZE_NOW}
  fi

done

# Size after
SIZE_AFTER=$(du -s . | cut -f1)
echo "Current gain $(human_readable $((${SIZE_BEFORE}-${SIZE_AFTER})))"
