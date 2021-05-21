#!/bin/sh
# Copyright (c) 2020 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# ./.repositories-update-contributing.sh
#    no parameter = execute it with docker command
# ./.repositories-update-contributing.sh -w
#   execute in native mode (need to have mikefarah/yq installed )

set -e
set -u

YQ_IMAGE="mikefarah/yq:3.3.2"

DIR=$(cd "$(dirname "$0")"; pwd)

init() {
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
  BOLD='\033[1m'
}

LAUNCH_IN_DOCKER=true
CHECK_MODE=false
analyze_args() {
  if [ $# -eq 0 ]; then
    return
  fi
  if [ "$1" = "IN_DOCKER" ] || [ "$1" = "-w" ]; then
    LAUNCH_IN_DOCKER=false
    shift
  fi
  if [ $# -eq 0 ]; then
    return
  fi
  if [ "$1" = "check" ] ; then
    CHECK_MODE=true
  fi
}

markdown_link() {
  name=$1
  url=$2
  if [ "${url}" != "" ]; then
    echo "[${name}](${url})"
  else
    echo ""
  fi
}

compute() {
  repo_names=$(yq r .repositories.yaml "repositories.*.name")
  echo "Repository | Component | Description | Devfile | Documentation"
  echo "--- | --- | ---  | --- | ---"
  for repo_name in ${repo_names}; do
    url=$(yq r .repositories.yaml "repositories.name==${repo_name}.url")
    description=$(yq r .repositories.yaml "repositories.name==${repo_name}.description")
    devfile=$(yq r .repositories.yaml "repositories.name==${repo_name}.devfile")
    doc=$(yq r .repositories.yaml "repositories.name==${repo_name}.doc")
    echo "$(markdown_link "${repo_name}" "${url}") | | ${description} | $(markdown_link "devfile" "${devfile}") | $(markdown_link "doc" "${doc}")"
    # components ?
    components_names=$(yq r .repositories.yaml "repositories.(name==${repo_name}).components.*.name")
    for component_name in ${components_names}; do
      component_url=$(yq r .repositories.yaml "repositories.(name==${repo_name}).components.(name==${component_name}).url")
      component_description=$(yq r .repositories.yaml "repositories.(name==${repo_name}).components.(name==${component_name}).description")
      echo "---| $(markdown_link "${component_name}" "${component_url}") | ${component_description} | | |"
    done   
  done
}

update_contributing() {
  CONTRIBUTING_FILE=${DIR}/CONTRIBUTING.md
  TARGET_FILE=${DIR}/CONTRIBUTING.md

  if [ ${CHECK_MODE} = true ]; then
    TARGET_FILE=${DIR}/target/CONTRIBUTING.md
  fi


  content_to_include=$(compute)

  # get begin/end lines
  begin_line=$(grep -n '<!-- begin repository list -->' "${CONTRIBUTING_FILE}" | cut -d ":" -f 1 | head -n 1) 
  end_line=$(grep -n '<!-- end repository list -->' "${CONTRIBUTING_FILE}" | cut -d ":" -f 1 | head -n 1)

  # update content
  mkdir -p "${DIR}/target"
  cp "${CONTRIBUTING_FILE}" "${DIR}/target/contributing.original"
  (head -n "${begin_line}" "${CONTRIBUTING_FILE}" && echo "${content_to_include}" && tail -n "+${end_line}" "${CONTRIBUTING_FILE}") > "${DIR}/.tmp-contributing"
  mv "${DIR}/.tmp-contributing" "${TARGET_FILE}"
}

run_in_docker() {
  printf "%bRunning%b $*\n" "${BOLD}" "${NC}"
  GIT_ROOT_DIRECTORY=$(git rev-parse --show-toplevel)
  if docker run --rm -v "${GIT_ROOT_DIRECTORY}":/workdir --entrypoint=/bin/sh "${YQ_IMAGE}" "/workdir/$0" "IN_DOCKER" "$@"
  then
    printf "Script execution %b[OK]%b\n" "${GREEN}" "${NC}"
  else
    printf "%bFail to run the script%b\n" "${RED}" "${NC}"
    exit 1
  fi
}

init "$@"
analyze_args "$@"
if [ ${LAUNCH_IN_DOCKER} = true ]; then
  run_in_docker "$@"
else
  update_contributing
fi

if [ ${CHECK_MODE} = true ]; then
 if ! diff "${DIR}/target/contributing.original" "${DIR}/target/CONTRIBUTING.md"; then
    printf "%bError: %bCONTRIBUTING.md should be updated as repositories.yaml file has been updated\n" "${RED}" "${NC}"
    printf "%s" "Run the command: ./.repositories-update-contributing.sh"
    exit 1
 fi

fi
