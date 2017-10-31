/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.command;

import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.ide.api.macro.Macro;

/** Allows to execute a command. */
public interface CommandExecutor {

  /**
   * Sends the the given {@code command} to the specified {@code machine} for execution.
   *
   * <p><b>Note</b> that all {@link Macro}s will be expanded into real values before sending the
   * {@code command} for execution.
   *
   * @param command command to execute
   * @param machineName name of the machine where execute the command
   * @see Macro
   */
  void executeCommand(Command command, String machineName);

  /**
   * Sends the the given {@code command} for execution.
   *
   * <p>If any machine is currently selected it will be used as execution target. Otherwise user
   * will be asked for choosing execution target.
   *
   * <p><b>Note</b> that all {@link Macro}s will be expanded into real values before sending the
   * {@code command} for execution.
   *
   * @param command command to execute
   * @see Macro
   */
  void executeCommand(CommandImpl command);
}
