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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Allows to run predefined command without UI.
 *
 * @author Max Shaposhnik
 */
public class RunCommandAction extends BaseAction {

  public static final String NAME_PARAM_ID = "name";

  private final CommandManager commandManager;
  private final CommandExecutor commandExecutor;
  private final WsAgentServerUtil wsAgentServerUtil;
  private final CoreLocalizationConstant localizationConstant;

  @Inject
  public RunCommandAction(
      CommandManager commandManager,
      CoreLocalizationConstant localizationConstant,
      CommandExecutor commandExecutor,
      WsAgentServerUtil wsAgentServerUtil) {
    this.commandManager = commandManager;
    this.localizationConstant = localizationConstant;
    this.commandExecutor = commandExecutor;
    this.wsAgentServerUtil = wsAgentServerUtil;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (event.getParameters() == null) {
      Log.error(getClass(), localizationConstant.runCommandEmptyParamsMessage());
      return;
    }

    final String name = event.getParameters().get(NAME_PARAM_ID);
    if (name == null) {
      Log.error(getClass(), localizationConstant.runCommandEmptyNameMessage());
      return;
    }

    wsAgentServerUtil
        .getWsAgentServerMachine()
        .ifPresent(
            m ->
                commandManager
                    .getCommand(name)
                    .ifPresent(command -> commandExecutor.executeCommand(command, m.getName())));
  }
}
