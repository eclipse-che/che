#!/bin/bash -e
#
# Copyright (c) 2019 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0

base_dir=$(cd "$(dirname "$0")"; pwd)
current_dir=$(pwd)
if [ -z "${AKAMAI_CHE_AUTH:-}" ]; then
  exit 0
fi

echo "Pushing CDN files to the Akamai directory..."
cd "${base_dir}"
cat > akamai.conf << EOF
${AKAMAI_CHE_AUTH}
EOF
for file in $(find theia_artifacts -type f -print | grep -v 'cdn.json'); do
  echo "   Pushing $file" 
  docker run -it --rm -v "${base_dir}/akamai.conf:/root/.akamai-cli/.netstorage/auth" -v "${base_dir}/theia_artifacts:/theia_artifacts" akamai/cli netstorage upload --directory "${AKAMAI_CHE_DIR:-che}" "${file}"
done
rm -f "${base_dir}/akamai.conf"
cd "${current_dir}"
