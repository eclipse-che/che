#!/bin/sh
#
# Copyright (c) 2016-2017 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Florent Benoit - Initial Implementation
#

# Global variables
UNAME_R=${UNAME_R:-$(uname -r)}
IP_A_SHOW=${IP_A_SHOW:-$(ip a show)}

# Helper function that check if we're running with boot2docker or not
# Global variables used inside this function :
#   ${UNAME_R} : output of $(uname -r)
is_boot2docker() {
  if $(echo ${UNAME_R} | grep -q 'boot2docker'); then
    return 0
  else
    return 1
  fi
}

# Helper function that check if we're running with docker 4 mac / windows or not
# Global variables used inside this function :
#   ${UNAME_R} : output of $(uname -r)
is_docker4MacOrWin() {
  if $(echo ${UNAME_R} | grep -q 'linuxkit'); then
    return 0
  elif $(echo ${UNAME_R} | grep -q 'moby'); then
    return 0
  else
    return 1
  fi
}

# Helper function that check if given arg is present in the list of network interfaces
# Global variables used inside this function :
#   ${IP_A_SHOW} : output of $(ip a show)
# Function Arguments :
#   $1 : name of the network interface
has_network_interface() {
   get_network_interfaces_list | grep -Fxq "${1}"
}

# Helper function that check if given network interface has an ip defined or not
# interface may exists nut no ip is attached
# Global variables used inside this function :
#   ${IP_A_SHOW} : output of $(ip a show)
# Function Arguments :
#   $1 : name of the network interface
has_ip_on_network_interface() {
  local IP_OF_INTERFACE=$(get_ip_from_network_interface $1)
  if [ ! -z ${IP_OF_INTERFACE} ]; then
    return 0
  fi
  return 1
}


# Helper function that search for a given network interface
# 1. If docker4mac/windows
#    - check for windows hvint0
#    - or use default eth0
# 2. If boot2docker
#    - use default eth1
# 3. Native case
#    - search one of the matching eth* interface
#    - search for ubuntu ens** interface
#    - default to docker0 interface
# Global variables used inside this function :
#   ${IP_A_SHOW} : output of $(ip a show)
#   ${UNAME_R} : output of $(uname -r)
find_network_interface() {

  # handle docker for windows/mac
  if is_docker4MacOrWin; then
    if has_network_interface "hvint0"; then
      NETWORK_IF=hvint0
    elif has_network_interface "eth0"; then
      NETWORK_IF=eth0
    fi
  # handle boot2docker
  elif is_boot2docker; then
    if has_network_interface "eth1"; then
      NETWORK_IF=eth1
    fi
  else
    # native mode
    if test -z ${NETWORK_IF}; then
      for i in $(get_network_interfaces_list); do
        if [ ${i:0:3} = eth ] || [ ${i:0:3} = enp ] || [ ${i:0:3} = ens ] || [ ${i:0:4} = wlan ] ;then
            if has_ip_on_network_interface $i; then
              NETWORK_IF=$i
              break
            fi
        fi
      done
    fi
    # else, default to docker0 interface
    if test -z ${NETWORK_IF}; then
      if has_network_interface "docker0"; then
        NETWORK_IF="docker0"
      fi
    fi
  fi

  echo ${NETWORK_IF}
}

# Extract ip interface from the given argument
# Global variables used inside this function :
#   ${IP_A_SHOW} : output of $(ip a show)
# Function Arguments :
#   $1 : Name of the interface : like eth0
get_ip_from_network_interface() {
  echo "${IP_A_SHOW}" | grep -e "scope.*${1}" | \
                grep -v ':' | \
                cut -d/ -f1 | \
                awk 'NR==1{print $2}'
}


# Helper functions that extract from $(ip a show) the network interfaces
# It returns list separated with newline like:
#    eth0
#    eth1
#    eth2
# Global variables used inside this function :
#   ${IP_A_SHOW} : output of $(ip a show)
get_network_interfaces_list() {
  echo "${IP_A_SHOW}" | grep -e "^.*:.*: <" | cut -d':' -f2 | cut -d'@' -f1 | awk '{print $1}'
}

# Root function that will print ip of external docker
# by first finding the network interface
# Global variables used inside this function :
#   ${IP_A_SHOW} : output of $(ip a show)
#   ${UNAME_R} : output of $(uname -r)
get_ip_of_docker() {
  NETWORK_IF=$(find_network_interface)

  if [[ ! -z ${NETWORK_IF} ]]; then
    echo $(get_ip_from_network_interface "${NETWORK_IF}")
  else
    echo ""
  fi
}
