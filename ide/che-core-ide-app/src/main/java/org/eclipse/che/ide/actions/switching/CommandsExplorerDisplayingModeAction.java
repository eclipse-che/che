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
package org.eclipse.che.ide.actions.switching;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;

/**
 * Switches Commands Explorer display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Commands Explorer is invisible -> make it visible and active
 *   <li>Commands Explorer is active -> make it invisible
 *   <li>Commands Explorer is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class CommandsExplorerDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;
  private Provider<CommandsExplorerPresenter> commandsExplorerPresenterProvider;

  @Inject
  public CommandsExplorerDisplayingModeAction(
      Resources resources,
      EditorAgent editorAgent,
      CoreLocalizationConstant localizedConstant,
      WorkspaceAgent workspaceAgent,
      Provider<CommandsExplorerPresenter> commandsExplorerPresenterProvider) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchCommandExplorerDisplayingTitle(),
        localizedConstant.switchCommandExplorerDisplayingDescription(),
        resources.editCommands());
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
    this.commandsExplorerPresenterProvider = commandsExplorerPresenterProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    CommandsExplorerPresenter commandsExplorerPresenter = commandsExplorerPresenterProvider.get();
    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart != null && activePart instanceof CommandsExplorerPresenter) {
      workspaceAgent.hidePart(commandsExplorerPresenter);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    workspaceAgent.openPart(commandsExplorerPresenter, NAVIGATION);
    workspaceAgent.setActivePart(commandsExplorerPresenter);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
