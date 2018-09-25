#!/bin/bash
#
# Copyright (c) 2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#

echo "Configuring Keycloak..."

cat /scripts/che-users-0.json.erb | \
				    sed -e "/<% if scope.lookupvar('keycloak::che_keycloak_admin_require_update_password') == 'true' -%>/d" | \
				    sed -e "/<% else -%>/d" | \
				    sed -e "/<% end -%>/d" | \
				    sed -e "/\"requiredActions\" : \[ \],/d" | \
				    jq .users[] > /scripts/che-user.json

if [ "${CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD}" == "false" ]; then
   sed -i -e "s#\"UPDATE_PASSWORD\"##" /scripts/che-user.json
fi

cat /scripts/che-realm.json.erb | sed -e "s@<%= scope\.lookupvar('che::che_server_url') %>@${HTTP_PROTOCOL}://${CHE_HOST}@" > /scripts/realm.json

echo "Creating Che realm and che-public client..."

cd /opt/jboss/keycloak/bin

./kcadm.sh create realms -f /scripts/realm.json --no-config --server ${HTTP_PROTOCOL}://${KC_HOST}/auth --realm master --user admin --password admin

echo "Creating default Che user with the following credentials 'admin:admin'"

./kcadm.sh create users -r che -f /scripts/che-user.json --no-config --server ${HTTP_PROTOCOL}://${KC_HOST}/auth --realm master --user admin --password admin

echo "Done!"
