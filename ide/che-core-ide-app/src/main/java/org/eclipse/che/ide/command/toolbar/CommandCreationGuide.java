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
package org.eclipse.che.ide.command.toolbar;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;
import org.eclipse.che.ide.command.goal.RunGoal;
import org.eclipse.che.ide.command.node.NodeFactory;

/**
 * Guides the user into the flow of creating a command and helps him to understand how he can
 * configure his workspace's commands.
 */
@Singleton
public class CommandCreationGuide {

  private final Provider<WorkspaceAgent> workspaceAgentProvider;
  private final Provider<CommandsExplorerPresenter> commandsExplorerPresenterProvider;
  private final CommandManager commandManager;
  private final Provider<EditorAgent> editorAgentProvider;
  private final NodeFactory nodeFactory;
  private final RunGoal runGoal;

  @Inject
  public CommandCreationGuide(
      Provider<WorkspaceAgent> workspaceAgentProvider,
      Provider<CommandsExplorerPresenter> commandsExplorerPresenterProvider,
      CommandManager commandManager,
      Provider<EditorAgent> editorAgentProvider,
      NodeFactory nodeFactory,
      RunGoal runGoal) {
    this.workspaceAgentProvider = workspaceAgentProvider;
    this.commandsExplorerPresenterProvider = commandsExplorerPresenterProvider;
    this.commandManager = commandManager;
    this.editorAgentProvider = editorAgentProvider;
    this.nodeFactory = nodeFactory;
    this.runGoal = runGoal;
  }

  /** Shows the guide of creating a command of the 'Run' goal. */
  public void guide() {
    guide(runGoal);
  }

  /** Shows the guide of creating a command of the specified {@code goal}. */
  public void guide(CommandGoal goal) {
    workspaceAgentProvider.get().setActivePart(commandsExplorerPresenterProvider.get());

    commandManager
        .createCommand(goal.getId(), "custom")
        .then(
            command -> {
              editorAgentProvider.get().openEditor(nodeFactory.newCommandFileNode(command));
            });
  }
}
