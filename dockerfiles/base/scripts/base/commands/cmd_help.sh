#!/bin/bash
#
# Copyright (c) 2012-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
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
