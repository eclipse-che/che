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
package org.eclipse.che.ide.part.explorer.project.synchronize;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.core.ErrorCodes.NO_PROJECT_ON_FILE_SYSTEM;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.api.resources.Project.ProblemProjectMarker.PROBLEM_PROJECT;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.marker.Marker;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Synchronize projects in workspace with projects on file system. The synchronization performs
 * based on project problems. Project problem with code 10 means that project exist in workspace but
 * it is absent on file system.
 */
@Singleton
public class ProjectConfigSynchronized {
  private final AppContext appContext;
  private final DialogFactory dialogFactory;
  private final CoreLocalizationConstant locale;
  private final NotificationManager notificationManager;
  private final ChangeLocationWidget changeLocationWidget;

  @Inject
  public ProjectConfigSynchronized(
      AppContext appContext,
      ProjectExplorerPresenter projectExplorerPresenter,
      DialogFactory dialogFactory,
      CoreLocalizationConstant locale,
      NotificationManager notificationManager,
      ChangeLocationWidget changeLocationWidget) {
    this.appContext = appContext;
    this.dialogFactory = dialogFactory;
    this.locale = locale;
    this.notificationManager = notificationManager;
    this.changeLocationWidget = changeLocationWidget;

    projectExplorerPresenter
        .getTree()
        .getNodeLoader()
        .addBeforeLoadHandler(
            event -> {
              final Node requestedNode = event.getRequestedNode();
              if (requestedNode == null || !(requestedNode instanceof ResourceNode)) {
                return;
              }

              final ResourceNode resourceNode = (ResourceNode) requestedNode;
              final Resource data = resourceNode.getData();
              if (!data.isProject()) {
                return;
              }

              checkProjectProblems((Project) data);
            });
  }

  private void checkProjectProblems(Project project) {
    final Optional<Marker> marker = project.getMarker(PROBLEM_PROJECT);
    if (!marker.isPresent()) {
      return;
    }

    final Project.ProblemProjectMarker problemProjectMarker =
        (Project.ProblemProjectMarker) marker.get();
    final Map<Integer, String> problems = problemProjectMarker.getProblems();

    // If no project folder on file system
    final String noProjectFolderProblem = problems.get(NO_PROJECT_ON_FILE_SYSTEM);
    final List<String> importingProjects = appContext.getImportingProjects();
    if (!isNullOrEmpty(noProjectFolderProblem) && !importingProjects.contains(project.getPath())) {
      showImportDialog(project);
    }
  }

  private void showImportDialog(Project project) {
    dialogFactory
        .createConfirmDialog(
            locale.synchronizeDialogTitle(),
            locale.existInWorkspaceDialogContent(project.getName()),
            locale.buttonImport(),
            locale.buttonRemove(),
            () -> importCallback(project),
            () -> deleteCallback(project))
        .show();
  }

  private Promise<Void> deleteCallback(Project project) {
    return project
        .delete()
        .then(
            deletedProject -> {
              notificationManager.notify(
                  locale.projectRemoved(project.getName()), SUCCESS, EMERGE_MODE);
            });
  }

  private void importCallback(Project project) {
    final String location = project.getSource().getLocation();
    if (isNullOrEmpty(location)) {
      changeLocation(project);

      return;
    }

    importProject(project);
  }

  private void changeLocation(Project project) {
    dialogFactory
        .createConfirmDialog(
            locale.synchronizeDialogTitle(),
            changeLocationWidget,
            () -> {
              final SourceStorageDto source = (SourceStorageDto) project.getSource();

              source.setLocation(changeLocationWidget.getText());
              source.setType("github");

              importProject(project);
            },
            null)
        .show();
  }

  private void importProject(Project project) {
    appContext
        .getWorkspaceRoot()
        .importProject()
        .withBody(project)
        .send()
        .then(
            new Operation<Project>() {
              @Override
              public void apply(Project project) throws OperationException {
                Log.info(getClass(), "Project " + project.getName() + " imported.");
              }
            });
  }
}
