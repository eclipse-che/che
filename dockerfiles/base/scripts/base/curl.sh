#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Contains curl scripts

has_curl() {
  hash curl 2>/dev/null && return 0 || return 1
}

curl() {

 # In situations where we are performing internal pings using curl, then
 # we should append the CHE_HOST as a no proxy. It seems that curl does
 # not respect the NO_PROXY environment variable set on the system.
 local NO_PROXY_CONFIG_FOR_CURL=("")
 if [[ ! "${HTTP_PROXY}" = "" ]] ||
    [[ ! "${HTTPS_PROXY}" = "" ]]; then
      if is_var_defined "${CHE_PRODUCT_NAME}_HOST"; then
        NO_PROXY_CONFIG_FOR_CURL=("--noproxy" $(eval "echo \$${CHE_PRODUCT_NAME}_HOST"))
      fi
 fi

 if ! has_curl; then
   log "docker run --rm --net=host appropriate/curl \"$@\""
   docker run --rm --net=host appropriate/curl ${NO_PROXY_CONFIG_FOR_CURL[@]} "$@"
 else
   log "$(which curl) ${NO_PROXY_CONFIG_FOR_CURL[@]} \"$@\""
   $(which curl) ${NO_PROXY_CONFIG_FOR_CURL[@]} "$@"
 fi
}

is_var_defined()
{
    if [ $# -ne 1 ]
    then
        echo "Expected exactly one argument: variable name as string, e.g., 'my_var'"
        exit 1
    fi
    eval "[ ! -z \${$1:-} ]"
    return $?
}

