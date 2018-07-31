/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client.keycloak.cli;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.che.selenium.core.executor.DockerCliCommandExecutor;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;

/**
 * This class is aimed to call Keycloak CLI commands inside Docker container.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class DockerKeycloakCliCommandExecutor implements KeycloakCliCommandExecutor {
  @Inject private DockerCliCommandExecutor dockerCliCommandExecutor;

  @Inject private ProcessAgent processAgent;

  private String keycloakContainerId;

  @Override
  public String execute(String command) throws IOException {
    if (keycloakContainerId == null || keycloakContainerId.trim().isEmpty()) {
      obtainKeycloakContainerId();
    }

    String dockerKeycloakCliCommand =
        format("exec -i %s sh -c 'keycloak/bin/kcadm.sh %s'", keycloakContainerId, command);
    return dockerCliCommandExecutor.execute(dockerKeycloakCliCommand);
  }

  private void obtainKeycloakContainerId() throws ProcessAgentException {
    // obtain id of keycloak docker container
    keycloakContainerId =
        processAgent.process("echo $(docker ps | grep che_keycloak | cut -d ' ' -f1)");

    if (keycloakContainerId.trim().isEmpty()) {
      throw new RuntimeException(
          "Keycloak container is not found. Make sure that correct value is set for `CHE_INFRASTRUCTURE`, and product to test is run locally");
    }
  }
}
