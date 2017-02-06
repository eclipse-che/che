#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html


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
    rmi|upgrade|version|ssh|sync|action|test|compile|dir|help)
    ;;
  *)
    error "You passed an unknown command."
    usage
    return 2
    ;;
  esac
}

cli_execute() {
  cmd_lifecycle "$@"
}

cmd_lifecycle() {
  PRE_COMMAND="pre_cmd_$1"
  POST_COMMAND="post_cmd_$1"
  HELP_COMMAND="help_cmd_$1"
  COMMAND="cmd_$1"

  if [ -f "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/commands/cmd_$1.sh ]; then
    source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/commands/cmd_$1.sh
  fi

  if [ -f "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_$1.sh ]; then
    source "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_$1.sh
  fi

  shift

  if get_command_help; then
    if [ -n "$(type -t $HELP_COMMAND)" ] && [ "$(type -t $HELP_COMMAND)" = function ]; then
      eval $HELP_COMMAND "$@"
      return 2
    else 
      error "No help function found for $1"
    fi
  fi

  if [ -n "$(type -t $PRE_COMMAND)" ] && [ "$(type -t $PRE_COMMAND)" = function ]; then
    eval $PRE_COMMAND "$@"
  fi

  eval $COMMAND "$@"

  if [ -n "$(type -t $POST_COMMAND)" ] && [ "$(type -t $POST_COMMAND)" = function ]; then
    eval $POST_COMMAND "$@"
  fi
}
