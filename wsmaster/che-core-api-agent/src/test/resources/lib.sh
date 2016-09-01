#!/bin/bash
#
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

. ./config.sh

trap cleanUp EXIT

cleanUp() {
   ${CHE_PATH}/che.sh stop
}

printAndLog() {
    echo $@
    log $@
}

logStartCommand() {
    log
    log "=== [ "`date`" ] COMMAND STARTED: "$@
}

logEndCommand() {
    log "=================================== COMMAND COMPLETED: "$@
    log
}

log() {
    echo "TEST: "$@ >> ${TEST_LOG}
}

fetchJsonParameter() {
    OUTPUT=`echo ${OUTPUT} | sed 's/.*"'$1'"\s*:\s*"\([^"]*\)*".*/\1/'`
}

# --method={POST|GET|...}
# --content-type=...
# --cookie=...
# --body=...
# --url=...
# --output-http-code
# --verbose
doHttpRequest() {
    for var in "$@"; do
        if [[ "$var" =~ --content-type=.+ ]]; then
            CONTENT_TYPE_OPTION=`echo "-H \"Content-Type: $var\"" | sed -e "s/--content-type=//g"`

        elif [[ "$var" =~ --body=.+ ]]; then
            local BODY_OPTION=`echo "-d '$var'" | sed -e "s/--body=//g"`

        elif [[ "$var" =~ --url=.+ ]]; then
            local URL=`echo "'$var'" | sed -e "s/--url=//g"`

        elif [[ "$var" =~ --method=.+ ]]; then
            local METHOD_OPTION=`echo "-X $var" | sed -e "s/--method=//g"`
            
        elif [[ "$var" == "--output-http-code" ]]; then
            local OUTPUT_HTTP_CODE_OPTION="-o /dev/null -w \"%{http_code}\""

        elif [[ "$var" == "--verbose" ]]; then
            local VERBOSE_OPTION="-v"

        elif [[ "$var" =~ --cookie=.+ ]]; then
            local COOKIE_OPTION=$(echo "-H \"Cookie: session-access-key=$var\"" | sed -e "s/--cookie=//g")

        fi
    done

    local COMMAND="curl -s $VERBOSE_OPTION $OUTPUT_HTTP_CODE_OPTION $CONTENT_TYPE_OPTION $COOKIE_OPTION $BODY_OPTION $METHOD_OPTION $URL"
    
    logStartCommand $COMMAND
    
    OUTPUT=$(eval $COMMAND)
    EXIT_CODE=$?
    log ${OUTPUT}

    logEndCommand "curl"
}

doPost() {
    doHttpRequest --method=POST \
                  --content-type=$1 \
                  --body="$2" \
                  --url=$3 \
                  --cookie=$4
}

doGet() {
    doHttpRequest --method=GET \
                  --url=$1
}

doDelete() {
    doHttpRequest --method=DELETE \
                  --url=$1
}
