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

import java.io.IOException;
import org.eclipse.che.selenium.core.utils.process.ProcessAgentException;

/**
 * Executor of command of 'keycloak/bin/kcadm.sh' command line application.
 *
 * @author Dmytro Nochevnov
 */
public interface KeycloakCommandExecutor {

  /**
   * Executes command-line interface command.
   *
   * @param command CLI command to execute
   * @return response of CLI command
   * @throws ProcessAgentException
   */
  String execute(String command) throws IOException;
}
