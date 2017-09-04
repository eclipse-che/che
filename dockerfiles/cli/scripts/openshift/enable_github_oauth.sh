#!/bin/bash
# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# This script supplemenss the other che oenshfit deployment scripts, to have this working correctly
# please refer to http://www.eclipse.org/che/docs/ide/git-svn/index.html#github-oauth on setting up 
# github oAuth2
#  ``` bash
#   DEPLOY_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/deploy_che.sh
#   curl -fsSL ${DEPLOY_SCRIPT_URL} -o get-che.sh
#   WAIT_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/wait_until_che_is_available.sh
#   curl -fsSL ${WAIT_SCRIPT_URL} -o wait-che.sh
#   STACKS_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/replace_stacks.sh
#   curl -fsSL ${STACKS_SCRIPT_URL} -o stacks-che.sh
#   ENABLE_GITHUB_SCRIPT_URL=https://raw.githubusercontent.com/eclipse/che/master/dockerfiles/cli/scripts/openshift/enable_gitub_oauth.sh
#   curl -fsSL ${ENABLE_GITHUB_SCRIPT_URL} -o enable_gitub_oauth.sh
#   bash get-che.sh && wait-che.sh && stacks-che.sh && enable_gitub_oauth.sh
#   ```
#
set -e

# --------------------------------------------------------
# Check pre-requisites
# --------------------------------------------------------
command -v oc >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool oc (https://docs.openshift.org/latest/cli_reference/get_started_cli.html) is required but it's not installed. Aborting."; exit 1; }
command -v jq >/dev/null 2>&1 || { echo >&2 "[CHE] [ERROR] Command line tool jq (http://stedolan.github.io/jq/) is required but it's not installed. Aborting."; exit 1; }

# ---------------------------------------
# Defaults for github oAuth2 env vars
# ---------------------------------------

DEFAULT_CHE_OAUTH_GITHUB_AUTHURI="https://github.com/login/oauth/authorize"
CHE_OAUTH_GITHUB_AUTHURI=${CHE_OAUTH_GITHUB_AUTHURI:-${DEFAULT_CHE_OAUTH_GITHUB_AUTHURI}}

DEFAULT_CHE_OAUTH_GITHUB_TOKENURI="https://github.com/login/oauth/access_token"
CHE_OAUTH_GITHUB_TOKENURI=${CHE_OAUTH_GITHUB_TOKENURI:-${DEFAULT_CHE_OAUTH_GITHUB_TOKENURI}}

DEFAULT_CHE_OAUTH_GITHUB_REDIRECTURIS="http://localhost:${SERVER_PORT}/wsmaster/api/oauth/callback"
CHE_OAUTH_GITHUB_REDIRECTURIS=${CHE_OAUTH_GITHUB_REDIRECTURIS:-${DEFAULT_CHE_OAUTH_GITHUB_REDIRECTURIS}}


# ---------------------------------------
# Verify that we have all env var are set
# ---------------------------------------

if [ -z "${CHE_OAUTH_GITHUB_CLIENTID+x}" ]; then echo "[CHE] **ERROR**Env var CHE_OAUTH_GITHUB_CLIENTID is unset. You need to set it to continue. Aborting"; exit 1; fi

if [ -z "${CHE_OAUTH_GITHUB_CLIENTSECRET+x}" ]; then echo "[CHE] **ERROR**Env var CHE_OAUTH_GITHUB_CLIENTSECRET is unset. You need to set it to continue. Aborting"; exit 1; fi


# --------------------------------------------------------
# Edit che deployment config to add CHE_GITHUB* vars
# --------------------------------------------------------
oc get dc che -ojson  > che.json

HAS_GITHUB_OAUTH_CLIENTID=$(cat che.json | jq '.spec.template.spec.containers[0].env | .[] | select(.name | . and . == "CHE_OAUTH_GITHUB_CLIENTID" )')

if [ "${HAS_GITHUB_OAUTH_CLIENTID}x" == "x" ]; then 
    cat che-test.json | jq  -r --arg CHE_OAUTH_GITHUB_CLIENTID "$CHE_OAUTH_GITHUB_CLIENTID"  \
    --arg CHE_OAUTH_GITHUB_CLIENTSECRET "$CHE_OAUTH_GITHUB_CLIENTSECRET" \
    --arg CHE_OAUTH_GITHUB_CLIENTSECRET "$CHE_OAUTH_GITHUB_CLIENTSECRET" \
    --arg CHE_OAUTH_GITHUB_AUTHURI "$CHE_OAUTH_GITHUB_AUTHURI" \
    --arg CHE_OAUTH_GITHUB_TOKENURI "$CHE_OAUTH_GITHUB_TOKENURI" \
    --arg CHE_OAUTH_GITHUB_REDIRECTURIS "$CHE_OAUTH_GITHUB_REDIRECTURIS" \
    '.spec.template.spec.containers[0].env += [{name: "CHE_OAUTH_GITHUB_CLIENTID", value:$CHE_OAUTH_GITHUB_CLIENTID}, {name: "CHE_OAUTH_GITHUB_CLIENTSECRET", value:$CHE_OAUTH_GITHUB_CLIENTSECRET}, {name: "CHE_OAUTH_GITHUB_AUTHURI", value:$CHE_OAUTH_GITHUB_AUTHURI} ,
     {name: "CHE_OAUTH_GITHUB_TOKENURI", value:$CHE_OAUTH_GITHUB_TOKENURI},{name: "CHE_OAUTH_GITHUB_REDIRECTURIS", value:$CHE_OAUTH_GITHUB_REDIRECTURIS} ]' |   oc apply --force=true -f -
else 
    # TODO do we need to do update values and do redeploy in case of credentials already available
    echo "[CHE] **WARNING** Che is already configured with github oAuth2, skipping"     
    exit 1 
fi

