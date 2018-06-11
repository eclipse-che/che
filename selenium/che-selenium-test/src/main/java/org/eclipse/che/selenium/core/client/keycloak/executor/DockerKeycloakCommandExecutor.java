/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client.keycloak.executor;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import org.eclipse.che.selenium.core.utils.process.ProcessAgent;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;

/**
 * This class is aimed to call Keycloak admin CLI inside Docker container.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class DockerKeycloakCommandExecutor implements KeycloakCommandExecutor {

  private final ProcessAgent processAgent;

  private String keycloakContainerId;

  @Inject
  public DockerKeycloakCommandExecutor(ProcessAgent processAgent) {
    this.processAgent = processAgent;
  }

  @Override
  public String execute(String command) throws IOException {
    if (keycloakContainerId == null || keycloakContainerId.trim().isEmpty()) {
      obtainKeycloakContainerId();
    }

    String dockerCommand =
        format("docker exec -i %s sh -c 'keycloak/bin/kcadm.sh %s'", keycloakContainerId, command);
    return processAgent.process(dockerCommand);
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
