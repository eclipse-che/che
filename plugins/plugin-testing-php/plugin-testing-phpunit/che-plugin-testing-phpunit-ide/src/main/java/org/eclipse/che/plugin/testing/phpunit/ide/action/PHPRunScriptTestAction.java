/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.phpunit.ide.action;

import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunDebugTestAbstractAction;
import org.eclipse.che.plugin.testing.ide.detector.TestDetector;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestLocalizationConstant;
import org.eclipse.che.plugin.testing.phpunit.ide.PHPUnitTestResources;

/**
 * "Run Script" PHPUnit test action.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPRunScriptTestAction extends RunDebugTestAbstractAction {

  private final AppContext appContext;
  private final SelectionAgent selectionAgent;

  @Inject
  public PHPRunScriptTestAction(
      TestDetector testDetector,
      TestResultPresenter testResultPresenter,
      DebugConfigurationsManager debugConfigurationsManager,
      TestingHandler testingHandler,
      TestServiceClient client,
      NotificationManager notificationManager,
      PHPUnitTestResources resources,
      AppContext appContext,
      DtoFactory dtoFactory,
      SelectionAgent selectionAgent,
      PHPUnitTestLocalizationConstant localization) {
    super(
        testDetector,
        testResultPresenter,
        testingHandler,
        debugConfigurationsManager,
        client,
        dtoFactory,
        appContext,
        notificationManager,
        singletonList(PROJECT_PERSPECTIVE_ID),
        localization.actionRunScriptDescription(),
        localization.actionRunScriptTitle(),
        resources.testIcon());
    this.appContext = appContext;
    this.selectionAgent = selectionAgent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Pair<String, String> frameworkAndTestName = Pair.of("PHPUnit", null);
    actionPerformed(frameworkAndTestName, false);
  }

  @Override
  public void updateInPerspective(ActionEvent e) {
    if ((appContext.getRootProject() == null)) {
      e.getPresentation().setVisible(true);
      e.getPresentation().setEnabled(false);
      return;
    }
    final Selection<?> selection = selectionAgent.getSelection();
    if (selection == null || selection.isEmpty()) {
      e.getPresentation().setEnabled(false);
      return;
    }
    if (selection.isMultiSelection()) {
      e.getPresentation().setEnabled(false);
      return;
    }
    final Object possibleNode = selection.getHeadElement();
    boolean enable = false;
    if (possibleNode instanceof FileNode) {
      FileNode fileNode = (FileNode) possibleNode;
      File data = fileNode.getData();
      String extension = data.getExtension();
      if ("php".equals(extension) || "phtml".equals(extension)) {
        enable = true;
        selectedNodePath = data.getLocation().toString();
      }
    }

    e.getPresentation().setEnabled(enable);
    e.getPresentation().setVisible(enable);
  }
}
