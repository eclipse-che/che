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
package org.eclipse.che.ide.part.perspectives.project;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;
import org.eclipse.che.ide.part.PartStackPresenterFactory;
import org.eclipse.che.ide.part.PartStackViewFactory;
import org.eclipse.che.ide.part.WorkBenchControllerFactory;
import org.eclipse.che.ide.part.editor.multipart.EditorMultiPartStackPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.part.perspectives.general.AbstractPerspective;
import org.eclipse.che.ide.part.perspectives.general.PerspectiveViewImpl;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.providers.DynaProvider;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.parts.PartStackType.EDITING;
import static org.eclipse.che.ide.api.parts.PartStackType.INFORMATION;
import static org.eclipse.che.ide.api.parts.PartStackType.NAVIGATION;
import static org.eclipse.che.ide.api.parts.PartStackType.TOOLING;

/**
 * General-purpose, displaying all the PartStacks in a default manner: Navigation at the left side;
 * Tooling at the right side; Information at the bottom of the page; Editors in the center.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProjectPerspective extends AbstractPerspective {

  public static final String PROJECT_PERSPECTIVE_ID = "Project Perspective";

  @Inject
  public ProjectPerspective(
      PerspectiveViewImpl view,
      EditorMultiPartStackPresenter editorMultiPartStackPresenter,
      PartStackPresenterFactory stackPresenterFactory,
      PartStackViewFactory partViewFactory,
      WorkBenchControllerFactory controllerFactory,
      EventBus eventBus,
      DynaProvider dynaProvider,
      ProjectExplorerPresenter projectExplorerPresenter,
      CommandsExplorerPresenter commandsExplorerPresenter,
      NotificationManager notificationManager,
      ProcessesPanelPresenter processesPanelPresenter) {
    super(
        PROJECT_PERSPECTIVE_ID,
        view,
        stackPresenterFactory,
        partViewFactory,
        controllerFactory,
        eventBus,
        dynaProvider);

    partStacks.put(EDITING, editorMultiPartStackPresenter);

    addPart(projectExplorerPresenter, NAVIGATION);
    addPart(commandsExplorerPresenter, NAVIGATION);
    addPart(notificationManager, INFORMATION);
    addPart(processesPanelPresenter, INFORMATION);

    PartStack navigatorPanel = getPartStack(NAVIGATION);
    PartStack editorPanel = getPartStack(EDITING);
    PartStack toolPanel = getPartStack(TOOLING);
    PartStack infoPanel = getPartStack(INFORMATION);

    if (navigatorPanel == null || editorPanel == null || toolPanel == null || infoPanel == null) {
      return;
    }

    navigatorPanel.go(view.getNavigationPanel());
    editorPanel.go(view.getEditorPanel());
    toolPanel.go(view.getToolPanel());
    infoPanel.go(view.getInformationPanel());
  }

  @Override
  public String getPerspectiveId() {
    return PROJECT_PERSPECTIVE_ID;
  }

  @Override
  public String getPerspectiveName() {
    return PROJECT_PERSPECTIVE_ID;
  }

  /** {@inheritDoc} */
  @Override
  public void go(@NotNull AcceptsOneWidget container) {
    PartStack navigatorPanel = getPartStack(NAVIGATION);
    PartStack editorPanel = getPartStack(EDITING);
    PartStack toolPanel = getPartStack(TOOLING);
    PartStack infoPanel = getPartStack(INFORMATION);

    if (navigatorPanel != null) {
      navigatorPanel.go(view.getNavigationPanel());
    }
    if (editorPanel != null) {
      editorPanel.go(view.getEditorPanel());
    }
    if (toolPanel != null) {
      toolPanel.go(view.getToolPanel());
    }
    if (infoPanel != null) {
      infoPanel.go(view.getInformationPanel());
    }
    container.setWidget(view);
  }
}
