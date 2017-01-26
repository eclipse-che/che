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

cmd_action() {
  debug $FUNCNAME

  # Not loaded as part of the init process to save on download time
  load_utilities_images_if_not_done
  docker_run -it ${UTILITY_IMAGE_CHEACTION} "$@"
}
