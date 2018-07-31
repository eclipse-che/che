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
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.ide.FontAwesome.FILE_TEXT;
import static org.eclipse.che.ide.api.parts.PartStack.State.HIDDEN;
import static org.eclipse.che.ide.api.parts.PartStack.State.MINIMIZED;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStack.State;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;

/**
 * Switches Editor display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Editor is inactive -> make it active
 *   <li>Editor is active -> maximize it
 *   <li>Editor is maximized -> set normal mode
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;

  @Inject
  public EditorDisplayingModeAction(
      EditorAgent editorAgent,
      WorkspaceAgent workspaceAgent,
      CoreLocalizationConstant localizedConstant) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchEditorDisplayingTitle(),
        localizedConstant.switchEditorDisplayingDescription(),
        FILE_TEXT);
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Set<PartStack> partStacks = new HashSet<>(3);
    partStacks.add(workspaceAgent.getPartStack(NAVIGATION));
    partStacks.add(workspaceAgent.getPartStack(TOOLING));
    partStacks.add(workspaceAgent.getPartStack(INFORMATION));

    Set<State> states = partStacks.stream().map(PartStack::getPartStackState).collect(toSet());
    if (states.stream().anyMatch(state -> MINIMIZED == state)) {
      partStacks.forEach(PartStack::restore);
      activateEditor();
      return;
    }

    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart == null || !(activePart instanceof EditorPartPresenter)) {
      activateEditor();
      return;
    }

    if (states.stream().allMatch(state -> HIDDEN == state)) {
      partStacks.forEach(PartStack::show);
      activateEditor();
      return;
    }

    partStacks.forEach(PartStack::minimize);
    activateEditor();
  }

  private void activateEditor() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor != null) {
      workspaceAgent.setActivePart(activeEditor);
    }
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
