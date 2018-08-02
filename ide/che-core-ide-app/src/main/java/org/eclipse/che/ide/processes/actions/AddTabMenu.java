/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.actions;

import static org.eclipse.che.api.workspace.shared.Constants.SERVER_SSH_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionGroup;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.action.Separator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.menu.ContextMenu;
import org.eclipse.che.ide.processes.DisplayMachineOutputEvent;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.processes.runtime.RuntimeInfoLocalization;
import org.eclipse.che.ide.terminal.TerminalOptionsJso;

/**
 * Menu for adding new tab in processes panel.
 *
 * @author Vitaliy Guliy
 * @author Vlad Zhukovskyi
 */
public class AddTabMenu extends ContextMenu {

  private final AppContext appContext;
  private final ProcessesPanelPresenter processesPanelPresenter;
  private final CoreLocalizationConstant coreLocalizationConstant;
  private final MachineResources machineResources;
  private final RuntimeInfoLocalization runtimeInfoLocalization;
  private final EventBus eventBus;

  @Inject
  public AddTabMenu(
      ActionManager actionManager,
      KeyBindingAgent keyBindingAgent,
      Provider<PerspectiveManager> managerProvider,
      AppContext appContext,
      ProcessesPanelPresenter processesPanelPresenter,
      CoreLocalizationConstant coreLocalizationConstant,
      MachineResources machineResources,
      RuntimeInfoLocalization runtimeInfoLocalization,
      EventBus eventBus) {
    super(actionManager, keyBindingAgent, managerProvider);

    this.appContext = appContext;
    this.processesPanelPresenter = processesPanelPresenter;
    this.coreLocalizationConstant = coreLocalizationConstant;
    this.machineResources = machineResources;
    this.runtimeInfoLocalization = runtimeInfoLocalization;
    this.eventBus = eventBus;
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

      ShowServersAction showServersAction = new ShowServersAction(machine.getName());
      actionGroup.add(showServersAction);

      ShowMachineOutputAction showMachineOutputAction =
          new ShowMachineOutputAction(machine.getName());
      actionGroup.add(showMachineOutputAction);
    }

    return actionGroup;
  }

  /** Action to add new Terminal tab. */
  public class NewTerminalMenuAction extends BaseAction {

    private String machineName;

    public NewTerminalMenuAction(
        CoreLocalizationConstant locale, MachineResources machineResources, String machineName) {
      super(
          locale.newTerminal(),
          locale.newTerminalDescription(),
          machineResources.addTerminalIcon());
      this.machineName = machineName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      processesPanelPresenter.onAddTerminal(machineName, TerminalOptionsJso.createDefault());
    }
  }

  /** Action to add new SSH tab. */
  public class AddSSHMenuAction extends BaseAction {

    private String machineName;

    public AddSSHMenuAction(String machineName) {
      super("SSH", "SSH", FontAwesome.RETWEET);
      this.machineName = machineName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      processesPanelPresenter.onPreviewSsh(machineName);
    }
  }

  /** Action to display bound servers. */
  public class ShowServersAction extends BaseAction {

    private String machineName;

    public ShowServersAction(String machineName) {
      super(
          runtimeInfoLocalization.showInfoActionTitle(),
          runtimeInfoLocalization.showInfoActionDescription(),
          machineResources.remote());
      this.machineName = machineName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      processesPanelPresenter.onPreviewServers(machineName);
    }
  }

  /** Action to display machine output. */
  public class ShowMachineOutputAction extends BaseAction {
    private String machineName;

    public ShowMachineOutputAction(String machineName) {
      super(
          coreLocalizationConstant.machineOutputActionTitle(),
          coreLocalizationConstant.machineOutputActionDescription(),
          machineResources.output());
      this.machineName = machineName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      eventBus.fireEvent(new DisplayMachineOutputEvent(machineName));
    }
  }
}
