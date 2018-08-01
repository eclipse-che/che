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
package org.eclipse.che.ide.command.toolbar.commands;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.api.command.CommandAddedEvent;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.command.CommandUpdatedEvent;
import org.eclipse.che.ide.api.command.CommandsLoadedEvent;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.command.goal.DebugGoal;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.toolbar.CommandCreationGuide;

/** Presenter drives the UI for executing commands. */
@Singleton
public class ExecuteCommandPresenter implements Presenter, ExecuteCommandView.ActionDelegate {

  private final ExecuteCommandView view;
  private final Provider<CommandExecutor> commandExecutorProvider;
  private final CommandCreationGuide commandCreationGuide;

  /** Command goals to display in the view. */
  private final Set<CommandGoal> goals;

  @Inject
  public ExecuteCommandPresenter(
      ExecuteCommandView view,
      CommandManager commandManager,
      Provider<CommandExecutor> commandExecutorProvider,
      CommandCreationGuide commandCreationGuide,
      RunGoal runGoal,
      DebugGoal debugGoal,
      EventBus eventBus) {
    this.view = view;
    this.commandExecutorProvider = commandExecutorProvider;
    this.commandCreationGuide = commandCreationGuide;

    view.setDelegate(this);

    goals = new HashSet<>();
    goals.add(runGoal);
    goals.add(debugGoal);

    eventBus.addHandler(
        CommandsLoadedEvent.getType(), e -> commandManager.getCommands().forEach(view::addCommand));
    eventBus.addHandler(CommandAddedEvent.getType(), e -> view.addCommand(e.getCommand()));
    eventBus.addHandler(CommandRemovedEvent.getType(), e -> view.removeCommand(e.getCommand()));
    eventBus.addHandler(
        CommandUpdatedEvent.getType(),
        e -> {
          view.removeCommand(e.getInitialCommand());
          view.addCommand(e.getUpdatedCommand());
        });
  }

  @Override
  public void go(AcceptsOneWidget container) {
    view.setGoals(goals);

    container.setWidget(view);
  }

  @Override
  public void onCommandExecute(CommandImpl command) {
    commandExecutorProvider.get().executeCommand(command);
  }

  @Override
  public void onCommandExecute(CommandImpl command, MachineImpl machine) {
    commandExecutorProvider.get().executeCommand(command, machine.getName());
  }

  @Override
  public void onGuide(CommandGoal goal) {
    commandCreationGuide.guide(goal);
  }
}
