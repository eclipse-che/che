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
