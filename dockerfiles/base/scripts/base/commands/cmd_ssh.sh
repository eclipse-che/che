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

help_cmd_ssh() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} ssh WORKSPACE [MACHINE] [PARAMETERS]\n"
  text "\n"
  text "Connect to a workspace in ${CHE_MINI_PRODUCT_NAME} over SSH\n"
  text "\n"
  text "WORKSPACE:             Accepts workspace name, ID, or namespace:ws-name\n"
  text "                       List all workspaces with 'action list-workspaces'\n"
  text "\n"
  text "MACHINE:               Choose machine (default is dev machine) if workspace as multiple containers\n"
  text "\n"
  text "PARAMETERS:\n"
  text "  --url                Location of ${CHE_MINI_PRODUCT_NAME}\n"
  text "  --user               User name of ${CHE_MINI_PRODUCT_NAME} if accessing authenticated system\n"
  text "  --password           Password of ${CHE_MINI_PRODUCT_NAME} if accessing authenticated system\n"
}

pre_cmd_ssh() {
  :
}

post_cmd_ssh() {
  :
}

cmd_ssh() {
  cmd_lifecycle action "workspace-ssh" "$@"  
}
