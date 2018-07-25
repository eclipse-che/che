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
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.TERMINAL_NODE;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.ProcessTreeNodeSelectedEvent;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Switches Terminal display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Terminal part is invisible -> make it visible and active
 *   <li>Terminal part is active -> make it invisible
 *   <li>Terminal part is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class TerminalDisplayingModeAction extends AbstractPerspectiveAction
    implements ProcessTreeNodeSelectedEvent.Handler {
  private EditorAgent editorAgent;
  private Provider<WorkspaceAgent> workspaceAgentProvider;
  private Provider<ProcessesPanelPresenter> processesPanelPresenterProvider;
  private ProcessTreeNode selectedNode;

  @Inject
  public TerminalDisplayingModeAction(
      Resources resources,
      EventBus eventBus,
      EditorAgent editorAgent,
      CoreLocalizationConstant localizedConstant,
      Provider<WorkspaceAgent> workspaceAgentProvider,
      Provider<ProcessesPanelPresenter> processesPanelPresenterProvider) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchTerminalDisplayingTitle(),
        localizedConstant.switchTerminalDisplayingDescription(),
        resources.terminal());
    this.editorAgent = editorAgent;
    this.workspaceAgentProvider = workspaceAgentProvider;
    this.processesPanelPresenterProvider = processesPanelPresenterProvider;

    eventBus.addHandler(ProcessTreeNodeSelectedEvent.TYPE, this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    WorkspaceAgent workspaceAgent = workspaceAgentProvider.get();
    ProcessesPanelPresenter processesPanelPresenter = processesPanelPresenterProvider.get();
    if (isProcessesPanelActive() && isTerminalActive()) {
      workspaceAgent.hidePart(processesPanelPresenter);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    processesPanelPresenter.provideTerminal();
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }

  @Override
  public void onProcessTreeNodeSelected(ProcessTreeNodeSelectedEvent event) {
    selectedNode = event.getProcessTreeNode();
  }

  private boolean isProcessesPanelActive() {
    PartPresenter activePart = workspaceAgentProvider.get().getActivePart();
    return activePart != null && activePart instanceof ProcessesPanelPresenter;
  }

  private boolean isTerminalActive() {
    return selectedNode != null && selectedNode.getType() == TERMINAL_NODE;
  }
}
