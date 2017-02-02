#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html


cli_parse () {
  COMMAND="cmd_$1"

  case $1 in
      init|config|start|stop|restart|backup|restore|info|offline|destroy|download|rmi|upgrade|version|ssh|sync|action|test|compile|dir|help)
      ;;
      *)
         error "You passed an unknown command."
         usage
         return 2
      ;;
  esac
}

cli_execute() {
  COMMAND="cmd_$1"

  # Library contains reusable functions
  source "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/library.sh

  # add base commands
  for BASECOMMAND_FILE in "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/commands/*.sh
  do
    source "${BASECOMMAND_FILE}"
  done

  # Need to load all files in advance so commands can invoke other commands.
  for COMMAND_FILE in "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cmd_*.sh
  do
    source "${COMMAND_FILE}"
  done

  shift
  eval $COMMAND "$@"
}
