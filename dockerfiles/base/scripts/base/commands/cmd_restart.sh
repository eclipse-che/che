#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

help_cmd_restart() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} restart [PARAMETERS]\n"
  text "\n"
  text "Stops ${CHE_MINI_PRODUCT_NAME} and starts again\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --force                           Uses 'docker rmi' and 'docker pull' to forcibly retrieve latest images\n"
  text "  --no-force                        Updates images if matching tag not found in local cache\n"
  text "  --pull                            Uses 'docker pull' to check for new remote versions of images\n"
  text "  --skip:graceful                   Do not wait for confirmation that workspaces have stopped\n"
  text "  --skip:preflight                  Skip preflight checks\n"
  text "\n"
}

pre_cmd_restart() {
  true
}

cmd_restart() {

  info "restart" "Restarting..."
  cmd_lifecycle stop ${@}

  # Need to remove any stop parameters from the command line otherwise the start will fail
  set -- "${@/\-\-skip\:graceful/}"

  cmd_lifecycle start "${@}"
}
