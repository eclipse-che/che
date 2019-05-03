#!/bin/sh
sed -i "s/che_workspace_namespace_placeholder/${CHE_WORKSPACE_NAMESPACE}/g" /home/user/agent/traefik/traefik.toml
sed -i "s/che_workspace_name_placeholder/${CHE_WORKSPACE_NAME}/g" /home/user/agent/traefik/traefik.toml
sed -i "s/che_machine_token_placeholder/${CHE_MACHINE_TOKEN}/g" /home/user/agent/traefik/traefik.toml
