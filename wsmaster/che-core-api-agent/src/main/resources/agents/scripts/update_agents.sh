#!/bin/bash
#
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#

updateAgentScript() {
  local DIR=$1
  local AGENT=$2
  local SCRIPT=$(cat ${AGENT}.script.sh | sed 's/"/\\"/g' | sed ':a;N;$!ba;s/\n/\\n/g')

  touch ${AGENT}.json.tmp
  cat ${DIR}/${AGENT}.json | while read line
  do
    if echo ${line} | grep -qi "\"script\""; then
      echo \"script\" : \"${SCRIPT}\" >> ${AGENT}.json.tmp
    else
      echo ${line} >> ${AGENT}.json.tmp
    fi
  done

  mv ${AGENT}.json.tmp ${DIR}/${AGENT}.json
}

updateAgentScript ".." "org.eclipse.che.ssh"
updateAgentScript ".." "org.eclipse.che.terminal"
updateAgentScript ".." "org.eclipse.che.ws-agent"
updateAgentScript ".." "org.eclipse.che.ls.json"
updateAgentScript ".." "org.eclipse.che.ls.csharp"
updateAgentScript ".." "org.eclipse.che.ls.php"
updateAgentScript ".." "org.eclipse.che.unison"
