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
package org.eclipse.che.ide.command.palette;

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.machine.chooser.MachineChooser;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/**
 * Presenter for Commands Palette.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandsPalettePresenter implements CommandsPaletteView.ActionDelegate {

  private final CommandsPaletteView view;
  private final CommandManager commandManager;
  private final CommandExecutor commandExecutor;
  private final DialogFactory dialogFactory;
  private final AppContext appContext;
  private final MachineChooser machineChooser;
  private final CommandUtils commandUtils;
  private final PaletteMessages messages;

  @Inject
  public CommandsPalettePresenter(
      CommandsPaletteView view,
      CommandManager commandManager,
      CommandExecutor commandExecutor,
      DialogFactory dialogFactory,
      AppContext appContext,
      MachineChooser machineChooser,
      CommandUtils commandUtils,
      PaletteMessages messages) {
    this.view = view;
    this.commandManager = commandManager;
    this.commandExecutor = commandExecutor;
    this.dialogFactory = dialogFactory;
    this.appContext = appContext;
    this.machineChooser = machineChooser;
    this.commandUtils = commandUtils;
    this.messages = messages;

    view.setDelegate(this);
  }

  public void showDialog() {
    view.show();
    view.setCommands(commandUtils.groupCommandsByGoal(commandManager.getCommands()));
  }

  @Override
  public void onFilterChanged(String filterValue) {
    final List<CommandImpl> filteredCommands = commandManager.getCommands();

    if (!filterValue.isEmpty()) {
      filteredCommands.removeIf(command -> !containsIgnoreCase(command.getName(), filterValue));
    }

    view.setCommands(commandUtils.groupCommandsByGoal(filteredCommands));
  }

  @Override
  public void onCommandExecute(CommandImpl command) {
    view.close();

    if (getMachines().isEmpty()) {
      // should not happen, but let's play safe
      dialogFactory.createMessageDialog("", messages.messageNoMachine(), null).show();
    } else {
      machineChooser
          .show()
          .then(
              machine -> {
                commandExecutor.executeCommand(command, machine.getName());
              });
    }
  }

  private List<? extends Machine> getMachines() {
    final Runtime runtime = appContext.getWorkspace().getRuntime();

    if (runtime != null) {
      return new ArrayList<>(runtime.getMachines().values());
    }

    return emptyList();
  }
}
