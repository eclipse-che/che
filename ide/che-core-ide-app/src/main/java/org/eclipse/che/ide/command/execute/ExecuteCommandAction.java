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
package org.eclipse.che.ide.command.execute;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.command.CommandUtils;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Action for executing a {@link CommandImpl}. */
class ExecuteCommandAction extends BaseAction {

  private final CommandImpl command;
  private final CommandExecutor commandExecutor;
  private final CommandManager commandManager;

  @Inject
  ExecuteCommandAction(
      @Assisted CommandImpl command,
      CommandUtils commandUtils,
      CommandExecutor commandExecutor,
      CommandManager commandManager) {
    super(command.getName());

    this.command = command;
    this.commandExecutor = commandExecutor;
    this.commandManager = commandManager;

    final SVGResource commandIcon = commandUtils.getCommandTypeIcon(command.getType());
    if (commandIcon != null) {
      getTemplatePresentation().setImageElement(new SVGImage(commandIcon).getElement());
    }
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation().setEnabledAndVisible(commandManager.isCommandApplicable(command));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    commandExecutor.executeCommand(command);
  }
}
