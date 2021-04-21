#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
set -o pipefail
# set -ex

fetch_conf_files_content() {
  RAW_CONTENT=$(cat che.properties multiuser.properties)
}

parse_content() {
  while read -r LINE
  do
    if [[ $LINE == '##'* ]]; then                       # line starting with two or more #s means new topic is started

      TOPIC="${LINE//#}"                                # read topic, stripping #s

    elif [[ $LINE == '#'* ]] && [[ -n $TOPIC ]]; then   # line starting with single # means property description (can be multi-line)
      TRIM_LINE=${LINE/\#}                              # read description, stripping first #
      DESCR_BUFF="$DESCR_BUFF${TRIM_LINE}"              # collect all description lines into buffer
    elif [[ -z $LINE ]] && [[ -n $TOPIC ]]; then
      DESCR_BUFF=""                                     # empty line is a separator -> cleanup description and property name + value
      KEY=""
      VALUE=""
    elif [[ -n $TOPIC ]]; then                          # non-empty line after any topic that doesn't start with # -> treat as property line
      IFS=$'=' read -r KEY VALUE <<< "$LINE"            # property split into key and value
      ENV="${KEY//_/__}"                          # replace single underscores with double
      ENV=${ENV//./_}                                   # replace dots with single underscore
      FILENAME="ref_${ENV}.adoc"
      IDLINE="[id=\"${ENV}_{context}\"]"
      ENV="\`+${ENV}+\`"                          # replace single underscores with double
      ENV="${ENV^^}"
      TITLE="= ${ENV}"
      VALUE="${VALUE/ }"                                # trim first space
      # VALUE="\`+${VALUE}+\`"                            # make sure asciidoc doesn't mix it up with attributes
      VALUE="${VALUE/\`++\`}"                           # remove empty value `++`
      
      DESCR_BUFF="$(sed 's|\${\([^}]*\)}|$++{\1}++|g' <<< $DESCR_BUFF)"   # make sure asciidoc doesn't mix it up with attributes
     
      DESCR_BUFF="${DESCR_BUFF/ }"                      # trim first space
      echo -e "$IDLINE\n$TITLE\n\n$DESCR_BUFF\n\n
.Default value for $ENV
====
----
$VALUE
----
====
"  > "topics/$FILENAME"                         # flush buffer into file
    fi
  done <<< "$RAW_CONTENT"
}


fetch_conf_files_content
parse_content
