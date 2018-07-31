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
package org.eclipse.che.selenium.core.utils.executor;

import java.io.IOException;

/**
 * Executes commands of CLI application.
 *
 * @author Dmytro Nochevnov
 */
public interface CommandExecutor {

  /**
   * Executes CLI application command.
   *
   * @param command CLI command to execute
   * @return response of CLI command
   * @throws IOException if there is a problem with command execution.
   */
  String execute(String command) throws IOException;
}
