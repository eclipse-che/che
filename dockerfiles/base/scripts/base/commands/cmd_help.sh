#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

help_cmd_help() {
  text "\n"
  text "Usage: ${CHE_IMAGE_FULLNAME} help"
  text "\n"
}

cmd_help() {
  usage
}

pre_cmd_help() {
  :
}

post_cmd_help() {
  :
}
