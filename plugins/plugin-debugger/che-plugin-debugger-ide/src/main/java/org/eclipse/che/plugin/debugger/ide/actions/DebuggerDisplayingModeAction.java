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
package org.eclipse.che.plugin.debugger.ide.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;

/**
 * Switches Debugger display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Debugger part is invisible -> make it visible and active
 *   <li>Debugger part is active -> make it invisible
 *   <li>Debugger part is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class DebuggerDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;
  private Provider<DebuggerPresenter> debuggerPresenterProvider;

  @Inject
  public DebuggerDisplayingModeAction(
      DebuggerResources resources,
      EditorAgent editorAgent,
      WorkspaceAgent workspaceAgent,
      Provider<DebuggerPresenter> debuggerPresenterProvider,
      DebuggerLocalizationConstant localizedConstant) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchDebuggerDisplayingTitle(),
        localizedConstant.switchDebuggerDisplayingDescription(),
        resources.debug());
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
    this.debuggerPresenterProvider = debuggerPresenterProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DebuggerPresenter debuggerPresenter = debuggerPresenterProvider.get();
    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart != null && activePart instanceof DebuggerPresenter) {
      workspaceAgent.hidePart(debuggerPresenter);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    workspaceAgent.openPart(debuggerPresenter, INFORMATION);
    workspaceAgent.setActivePart(debuggerPresenter);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
