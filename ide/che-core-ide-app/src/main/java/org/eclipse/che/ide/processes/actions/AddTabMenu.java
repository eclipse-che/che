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
package org.eclipse.che.ide.processes.actions;

import static org.eclipse.che.api.workspace.shared.Constants.SERVER_SSH_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Map;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.menu.ContextMenu;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.terminal.TerminalOptionsJso;

/**
 * Menu for adding new tab in processes panel.
 *
 * @author Vitaliy Guliy
 */
public class AddTabMenu extends ContextMenu {

  private AppContext appContext;

  private ProcessesPanelPresenter processesPanelPresenter;

  private CoreLocalizationConstant coreLocalizationConstant;
  private MachineResources machineResources;

  @Inject
  public AddTabMenu(
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      Provider<PerspectiveManager> managerProvider,
      AppContext appContext,
      ProcessesPanelPresenter processesPanelPresenter,
      CoreLocalizationConstant coreLocalizationConstant,
      MachineResources machineResources) {
    super(actionManager, keyBindingAgent, managerProvider);

    this.appContext = appContext;
    this.processesPanelPresenter = processesPanelPresenter;
    this.coreLocalizationConstant = coreLocalizationConstant;
    this.machineResources = machineResources;
  }

  /** {@inheritDoc} */
  @Override
  protected String getGroupMenu() {
    return IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
  }

  @Override
  protected ActionGroup updateActions() {
    final DefaultActionGroup actionGroup = new DefaultActionGroup(actionManager);

    Map<String, MachineImpl> machines = appContext.getWorkspace().getRuntime().getMachines();
    for (MachineImpl machine : machines.values()) {
      Separator separ = new Separator(machine.getName() + ":");
      actionGroup.add(separ);

      if (machine.getServerByName(SERVER_TERMINAL_REFERENCE).isPresent()) {
        NewTerminalMenuAction newTerminalMenuAction =
            new NewTerminalMenuAction(
                coreLocalizationConstant, machineResources, machine.getName());
        actionGroup.add(newTerminalMenuAction);
      }

      if (machine.getServerByName(SERVER_SSH_REFERENCE).isPresent()) {
        AddSSHMenuAction addSSHMenuAction = new AddSSHMenuAction(machine.getName());
        actionGroup.add(addSSHMenuAction);
      }
    }

    return actionGroup;
  }

  /** Action to add new Terminal tab. */
  public class NewTerminalMenuAction extends Action {

    private String machineName;

    public NewTerminalMenuAction(
        CoreLocalizationConstant locale, MachineResources machineResources, String machineName) {
      super(
          locale.newTerminal(),
          locale.newTerminalDescription(),
          null,
          machineResources.addTerminalIcon());
      this.machineName = machineName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      processesPanelPresenter.onAddTerminal(machineName, TerminalOptionsJso.createDefault());
    }
  }

  /** Action to add new SSH tab. */
  public class AddSSHMenuAction extends Action {

    private String machineName;

    public AddSSHMenuAction(String machineName) {
      super("SSH", "SSH", null, null, FontAwesome.RETWEET);
      this.machineName = machineName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      processesPanelPresenter.onPreviewSsh(machineName);
    }
  }
}
