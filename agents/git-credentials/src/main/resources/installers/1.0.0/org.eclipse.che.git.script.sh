#
# Copyright (c) 2012-2018 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Red Hat, Inc. - initial API and implementation
#

SCRIPT_FILE=~/.ssh/git.sh
mkdir -p ~/.ssh

token=$(if [ "$CHE_MACHINE_TOKEN" != "dummy_token" ]; then echo "$CHE_MACHINE_TOKEN"; fi)
che_host=$(cat /etc/hosts | grep che-host | awk '{print $1;}')
api_url=$(if [ "$CHE_API" != "http://che-host:8080/api" ]; then echo "$CHE_API"; else echo "$che_host:8080/api"; fi)

CURL_INSTALLED=false
WGET_INSTALLED=false
command -v curl >/dev/null 2>&1 && CURL_INSTALLED=true
command -v wget >/dev/null 2>&1 && WGET_INSTALLED=true

# no curl, no wget, install curl
if [ ${CURL_INSTALLED} = false ] && [ ${WGET_INSTALLED} = false ]; then
  PACKAGES=${PACKAGES}" curl";
  CURL_INSTALLED=true
fi

request=$(if ${CURL_INSTALLED}; then echo 'curl -s'; else echo 'wget -qO-'; fi)

echo 'host=$(echo $(if [ "$1" = "-p" ]; then echo "$3" ; else echo "$1"; fi) | sed -e "s/git@//")' > ${SCRIPT_FILE}
echo 'token='"$token" >> ${SCRIPT_FILE}
echo 'api_url='"$api_url" >> ${SCRIPT_FILE}
echo 'request="'${request}'"' >> ${SCRIPT_FILE}
# Ssh key request may return key with decoded '=' symbol, so need to replace '\u003d' to '='.
# TODO remove the replacement after https://github.com/eclipse/che/issues/5253 will be fixed.
echo 'ssh_key=$(${request} "$api_url/ssh/vcs/find?name=$host$(if [ -n "$token" ]; then echo "&token=$token"; fi)"| grep -Po '\''"privateKey":.*?[^\\\\]",'\''| sed -e "s/\"privateKey\":\"//" | sed -e "s/\\\\\u003d/=/g")' >> ${SCRIPT_FILE}
echo 'if [ -n "$ssh_key" ]' >> ${SCRIPT_FILE}
echo 'then' >> ${SCRIPT_FILE}
echo '    key_file=$(mktemp)' >> ${SCRIPT_FILE}
echo '    echo "$ssh_key" > "$key_file"' >> ${SCRIPT_FILE}
echo '    ssh -i "$key_file" "$@"' >> ${SCRIPT_FILE}
echo '    rm "$key_file"' >> ${SCRIPT_FILE}
echo 'else' >> ${SCRIPT_FILE}
echo '    ssh "$@"' >> ${SCRIPT_FILE}
echo 'fi' >> ${SCRIPT_FILE}

chmod +x ${SCRIPT_FILE}

user_name="$(${request} "$api_url/preferences$(if [ -n "$token" ]; then echo "?token=$token"; fi)" | grep -Po '"git.committer.name":.*?[^\\]",' | sed -e "s/\"git.committer.name\":\"//" | sed -e "s/\",//")"
user_email="$(${request} "$api_url/preferences$(if [ -n "$token" ]; then echo "?token=$token"; fi)" | grep -Po '"git.committer.email":.*?[^\\]",' | sed -e "s/\"git.committer.email\":\"//" | sed -e "s/\",//")"
git config --global user.name \""$user_name"\"
git config --global user.email \""$user_email"\"

if [ -z "$(cat ~/.bashrc | grep GIT_SSH)" ]
then
    printf '\n export GIT_SSH='"$SCRIPT_FILE" >> ~/.bashrc
fi
