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
package org.eclipse.che.ide.command.producer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandProducer;

/**
 * Action for executing command which produced by {@link CommandProducer}.
 *
 * @author Artem Zatsarynnyi
 * @see CommandProducer
 */
public class CommandProducerAction extends BaseAction {

  private final CommandProducer commandProducer;
  private final CommandExecutor commandExecutor;

  @Inject
  public CommandProducerAction(
      @Assisted String name,
      @Assisted CommandProducer commandProducer,
      CommandExecutor commandExecutor) {
    super(name);

    this.commandProducer = commandProducer;
    this.commandExecutor = commandExecutor;
  }

  @Override
  public void update(ActionEvent e) {
    e.getPresentation().setEnabledAndVisible(commandProducer.isApplicable());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    CommandImpl command = commandProducer.createCommand();
    commandExecutor.executeCommand(command);
  }
}
