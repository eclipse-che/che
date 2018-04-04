#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

help_cmd_test() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} test TEST [PARAMETERS]\n"
  text "\n"
  text "Synchronizes a ${CHE_MINI_PRODUCT_NAME} workspace to a local path mounted to ':/sync'\n"
  text "\n"
  text "TESTS:\n"
  text "  post-flight-check    Performs post-flight check to validate ${CHE_MINI_PRODUCT_NAME} install\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --quiet              Display not output during test\n"
  text "  --user               User name of ${CHE_MINI_PRODUCT_NAME} if accessing authenticated system\n"
  text "  --password           Password of ${CHE_MINI_PRODUCT_NAME} if accessing authenticated system\n"
  text "  --port               Define an optional port to use for the test\n"
}

pre_cmd_test() {
  # Not loaded as part of the init process to save on download time
  load_utilities_images_if_not_done
}

post_cmd_test() {
  :
}


cmd_test() {
  docker_run $(get_docker_run_terminal_options) ${UTILITY_IMAGE_CHETEST} "$@"
}
