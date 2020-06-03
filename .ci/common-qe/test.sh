#!/bin/bash
# set -x


TTT="$(cat <<EOL
spec:
  server:
    devfileRegistryImage: $IMAGE_NAME
    selfSignedCert: true
  auth:
    updateAdminPassword: false
EOL
)"



echo "$TTT" > aa.txt

echo "$(cat aa.txt)"
