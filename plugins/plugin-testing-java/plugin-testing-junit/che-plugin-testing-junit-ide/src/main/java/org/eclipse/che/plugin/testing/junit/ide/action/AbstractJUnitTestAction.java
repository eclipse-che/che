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
package org.eclipse.che.plugin.testing.junit.ide.action;

import static com.google.common.collect.Sets.newHashSet;
import static org.eclipse.che.api.testing.shared.TestExecutionContext.ContextType.PROJECT;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

import com.google.common.base.Optional;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.plugin.testing.ide.TestServiceClient;
import org.eclipse.che.plugin.testing.ide.action.RunDebugTestAbstractAction;
import org.eclipse.che.plugin.testing.ide.detector.TestDetector;
import org.eclipse.che.plugin.testing.ide.handler.TestingHandler;
import org.eclipse.che.plugin.testing.ide.view.TestResultPresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Abstract action which describes base JUnit action. */
public abstract class AbstractJUnitTestAction extends RunDebugTestAbstractAction {
  public static final String JUNIT_FRAMEWORK_NAME = "junit";

  private TestDetector testDetector;
  private AppContext appContext;

  public AbstractJUnitTestAction(
      TestDetector testDetector,
      TestResultPresenter testResultPresenter,
      TestingHandler testingHandler,
      DebugConfigurationsManager debugConfigurationsManager,
      TestServiceClient client,
      DtoFactory dtoFactory,
      AppContext appContext,
      NotificationManager notificationManager,
      List<String> perspectives,
      String description,
      String text,
      SVGResource icon) {
    super(
        testDetector,
        testResultPresenter,
        testingHandler,
        debugConfigurationsManager,
        client,
        dtoFactory,
        appContext,
        notificationManager,
        perspectives,
        description,
        text,
        icon);
    this.testDetector = testDetector;
    this.appContext = appContext;
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    Presentation presentation = event.getPresentation();
    boolean isProjectExplorerActive =
        testDetector.getActivePart() instanceof ProjectExplorerPresenter;
    presentation.setVisible(isProjectExplorerActive);
    if (!isProjectExplorerActive) {
      return;
    }
    if (!testDetector.isEditorInFocus()) {
      analyzeProjectTreeSelection(presentation);
    } else {
      presentation.setEnabled(testDetector.isEnabled());
    }
  }

  public abstract void actionPerformed(ActionEvent e);

  private void analyzeProjectTreeSelection(Presentation presentation) {
    Resource[] resources = appContext.getResources();
    if (resources == null || resources.length > 1) {
      presentation.setEnabled(false);
      return;
    }

    Resource resource = resources[0];
    if (resource.isProject() && isJavaProject((Project) resource)) {
      testDetector.setContextType(PROJECT);
      presentation.setEnabled(true);
      return;
    }

    Project project = resource.getProject();
    if (!isJavaProject(project)) {
      presentation.setEnabled(false);
      return;
    }

    if (isJavaTestFile(resource)) {
      testDetector.setContextType(TestExecutionContext.ContextType.FILE);
    } else if (resource instanceof Container) {
      Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);
      if (!srcFolder.isPresent() || resource.getLocation().equals(srcFolder.get().getLocation())) {
        presentation.setEnabled(false);
        return;
      }
      testDetector.setContextType(TestExecutionContext.ContextType.FOLDER);
    }
    presentation.setEnabled(true);
    selectedNodePath = resource.getLocation().toString();
  }

  private boolean isJavaTestFile(Resource resource) {
    if (resource.getResourceType() != FILE) {
      return false;
    }
    final String ext = ((File) resource).getExtension();

    return newHashSet("java", "class", "xml").contains(ext);
  }
}
