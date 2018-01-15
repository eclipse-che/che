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
package org.eclipse.che.ide.actions.switching;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
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
import org.eclipse.che.ide.search.presentation.FindResultPresenter;

/**
 * Switches Find part display mode depends on the current state of IDE:
 *
 * <ul>
 *   <li>Find part is invisible -> make it visible and active
 *   <li>Find part is active -> make it invisible
 *   <li>Find part is inactive -> make it active
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class FindResultDisplayingModeAction extends AbstractPerspectiveAction {
  private EditorAgent editorAgent;
  private WorkspaceAgent workspaceAgent;
  private Provider<FindResultPresenter> findResultPresenterProvider;

  @Inject
  public FindResultDisplayingModeAction(
      Resources resources,
      EditorAgent editorAgent,
      CoreLocalizationConstant localizedConstant,
      WorkspaceAgent workspaceAgent,
      Provider<FindResultPresenter> findResultPresenterProvider) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localizedConstant.switchFindPartDisplayingTitle(),
        localizedConstant.switchFindPartDisplayingDescription(),
        resources.find());
    this.editorAgent = editorAgent;
    this.workspaceAgent = workspaceAgent;
    this.findResultPresenterProvider = findResultPresenterProvider;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    FindResultPresenter findResultPresenter = findResultPresenterProvider.get();
    PartPresenter activePart = workspaceAgent.getActivePart();
    if (activePart != null && activePart instanceof FindResultPresenter) {
      workspaceAgent.hidePart(findResultPresenter);

      EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
      if (activeEditor != null) {
        workspaceAgent.setActivePart(activeEditor);
      }
      return;
    }

    workspaceAgent.openPart(findResultPresenter, INFORMATION);
    workspaceAgent.setActivePart(findResultPresenter);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setEnabledAndVisible(true);
  }
}
