#!/bin/sh
#
# Copyright (c) 2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

cli_load() {
  # Library contains reusable functions
  source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/library.sh

  if [ -f "${SCRIPTS_CONTAINER_SOURCE_DIR}"/library.sh ]; then
    source "${SCRIPTS_CONTAINER_SOURCE_DIR}"/library.sh
  fi
}

cli_parse () {
  COMMAND="cmd_$1"

  case $1 in
    init|config|start|stop|restart|backup|restore|info|offline|destroy|download|\
    rmi|upgrade|version|ssh|sync|action|test|compile|dir|help|archetype)
    ;;
  *)
    error "You passed an unknown command: $1"
    usage
    return 2
    ;;
  esac
}

cli_execute() {
  cmd_lifecycle "$@"
}

cmd_lifecycle() {
  local PRE_COMMAND="pre_cmd_$1"
  local POST_COMMAND="post_cmd_$1"
  local HELP_COMMAND="help_cmd_$1"
  local COMMAND="cmd_$1"

  if [ -f "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/commands/cmd_$1.sh ]; then
    source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/commands/cmd_$1.sh
  fi

  if [ -f "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_$1.sh ]; then
    source "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_$1.sh
  fi

  shift

  if get_command_help; then
    ANSWER=$(declare -f $HELP_COMMAND > /dev/null)
    if [ $? = "0" ]; then
      eval $HELP_COMMAND "$@"
      return 2
    else 
      error "No help function found for $1"
    fi
  fi

  local PRE_COMMAND_STATUS=0
  ANSWER=$(declare -f $PRE_COMMAND ) # > /dev/null)
  if [ $? = "0" ]; then
    eval $PRE_COMMAND "$@"
    PRE_COMMAND_STATUS=$?
  fi

  eval $COMMAND "$@"
  local COMMAND_STATUS=$?



  local POST_COMMAND_STATUS=0
  ANSWER=$(declare -f $POST_COMMAND > /dev/null)
  if [ $? = "0" ]; then
    eval $POST_COMMAND "$@"
    POST_COMMAND_STATUS=$?
  fi

  if [[ POST_COMMAND_STATUS -ne 0 ]]; then
    return ${POST_COMMAND_STATUS};
  else
    return ${COMMAND_STATUS};
  fi
}
