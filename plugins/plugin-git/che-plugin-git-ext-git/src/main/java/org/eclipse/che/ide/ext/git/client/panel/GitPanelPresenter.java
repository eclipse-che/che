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
package org.eclipse.che.ide.ext.git.client.panel;

import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_FROM;
import static org.eclipse.che.ide.api.resources.ResourceDelta.MOVED_TO;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.RepositoryDeletedEventDto;
import org.eclipse.che.api.git.shared.RepositoryInitializedEventDto;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.ext.git.client.GitEventSubscribable;
import org.eclipse.che.ide.ext.git.client.GitEventsSubscriber;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.compare.MutableAlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the Git panel.
 *
 * @author Mykola Morhun
 */
@Singleton
public class GitPanelPresenter extends BasePresenter
    implements GitPanelView.ActionDelegate, GitEventsSubscriber, ResourceChangedHandler {

  private static final String VCS_GIT = "git";
  private static final String REVISION = "HEAD";

  private final GitPanelView view;
  private final GitServiceClient service;
  private final ChangesPanelPresenter changesPanelPresenter;
  private final AppContext appContext;
  private final EventBus eventBus;
  private final NotificationManager notificationManager;
  private final GitEventSubscribable subscribeToGitEvents;
  private final GitResources gitResources;
  private final GitLocalizationConstant locale;

  private boolean initialized;
  private Map<String, MutableAlteredFiles> changes; // project name -> changes

  @Inject
  public GitPanelPresenter(
      GitPanelView view,
      GitServiceClient service,
      ChangesPanelPresenter changesPanelPresenter,
      WorkspaceAgent workspaceAgent,
      AppContext appContext,
      EventBus eventBus,
      GitEventSubscribable subscribeToGitEvents,
      NotificationManager notificationManager,
      GitResources gitResources,
      GitLocalizationConstant locale) {
    this.view = view;
    this.service = service;
    this.changesPanelPresenter = changesPanelPresenter;
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.subscribeToGitEvents = subscribeToGitEvents;
    this.notificationManager = notificationManager;
    this.gitResources = gitResources;
    this.locale = locale;

    this.view.setDelegate(this);
    this.view.setChangesPanelView(this.changesPanelPresenter.getView());

    // TODO uncomment actionPerformed in ToggleGitPanelAction
    // TODO uncomment to show git panel
    // if (partStack == null || !partStack.containsPart(this)) {
    //   workspaceAgent.openPart(this, PartStackType.NAVIGATION);
    // }

    this.initialized = false;
  }

  private void registerEventHandlers() {
    eventBus.addHandler(ResourceChangedEvent.getType(), this);

    subscribeToGitEvents.addSubscriber(this);
  }

  /** Invoked each time when panel is activated. */
  @Override
  public void onOpen() {
    if (!initialized) {
      exposeGitRepositories();
      registerEventHandlers();

      initialized = true;
    }
  }

  /** Queries from server all data needed to initialize the panel. */
  private void exposeGitRepositories() {
    this.changes = new HashMap<>();

    for (Project project : appContext.getProjects()) {
      if (isProjectUnderGit(project)) {
        view.addRepository(project.getName());
        reloadRepositoryData(project);
      }
    }
  }

  private void reloadRepositoryData(Project project) {
    service
        .diff(project.getLocation(), null, NAME_STATUS, false, 0, REVISION, false)
        .then(
            diff -> {
              MutableAlteredFiles alteredFiles = new MutableAlteredFiles(project, diff);
              changes.put(project.getName(), alteredFiles);
              view.updateRepositoryChanges(project.getName(), alteredFiles.getFilesQuantity());
              // update changed files list if this repository is selected
              if (project.getName().equals(view.getSelectedRepository())) {
                updateChangedFiles(alteredFiles);
              }
            })
        .catchError(
            arg -> {
              notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
            });
  }

  @Override
  public String getTitle() {
    return locale.panelTitle();
  }

  @Override
  public SVGResource getTitleImage() {
    return gitResources.git();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public String getTitleToolTip() {
    return locale.panelTitleToolTip();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  public void show() {
    partStack.setActivePart(this);
    onOpen();
  }

  public void hide() {
    partStack.minimize();
  }

  public boolean isOpened() {
    return partStack.getActivePart() == this;
  }

  @Override
  public void onFileChanged(String endpointId, FileChangedEventDto fileChangedEvent) {
    String projectName = extractProjectName(fileChangedEvent.getPath());
    MutableAlteredFiles alteredFiles = changes.get(projectName);

    switch (fileChangedEvent.getStatus()) {
      case MODIFIED:
        if (alteredFiles.addFile(removeProjectName(fileChangedEvent.getPath()), Status.MODIFIED)) {
          updateRepositoryChangesView(projectName, alteredFiles);
        }
        break;
      case NOT_MODIFIED:
        if (alteredFiles.removeFile(removeProjectName(fileChangedEvent.getPath()))) {
          updateRepositoryChangesView(projectName, alteredFiles);
        }
        break;
      default:
        // do nothing
    }
  }

  /** Handles creation and deletion of projects. Handles deletion of files. */
  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    ResourceDelta delta = event.getDelta();
    Resource resource = delta.getResource();
    if (resource.isProject() && resource.getLocation().segmentCount() == 1) {
      // resource is a root project
      if (isProjectUnderGit(resource.asProject())) {
        if (delta.getKind() == ResourceDelta.ADDED) {
          if ((delta.getFlags() & (MOVED_FROM | MOVED_TO)) != 0) {
            // project renamed
            String oldProjectName = delta.getFromPath().segment(0);
            String newProjectName = delta.getToPath().segment(0);
            MutableAlteredFiles alteredFiles =
                new MutableAlteredFiles(
                    findProjectByName(newProjectName), changes.remove(oldProjectName));

            changes.put(newProjectName, alteredFiles);
            view.renameRepository(oldProjectName, newProjectName);
          } else {
            // project created
            changes.put(resource.getName(), new MutableAlteredFiles(resource.asProject()));
            view.addRepository(resource.getName());
          }
        } else if (delta.getKind() == ResourceDelta.REMOVED) {
          // project deleted
          changes.remove(resource.getName());
          view.removeRepository(resource.getName());
        }
      }
    } else {
      // a node changed
      if (delta.getKind() == ResourceDelta.REMOVED) {
        onItemDeleted(delta);
      }
    }
  }

  /** Analyzes deleted item in project explorer tree and updates the panel if necessary. */
  private void onItemDeleted(ResourceDelta delta) {
    Path pathToItem = delta.getResource().getLocation();
    String projectName = pathToItem.segment(0);
    MutableAlteredFiles alteredFiles = changes.get(projectName);
    if (alteredFiles == null) {
      // project doesn't have a git repository
      return;
    }
    String itemRelativePath = pathToItem.removeFirstSegments(1).toString();

    if (alteredFiles.getStatusByFilePath(itemRelativePath) != Status.ADDED) {
      alteredFiles.addFile(itemRelativePath, Status.DELETED);
    } else {
      alteredFiles.removeFile(itemRelativePath);
    }

    updateRepositoryChangesView(projectName, alteredFiles);
  }

  @Override
  public void onGitRepositoryInitialized(
      String endpointId, RepositoryInitializedEventDto repositoryInitializedEventDto) {
    String projectName = repositoryInitializedEventDto.getProjectName();
    changes.put(projectName, new MutableAlteredFiles(findProjectByName(projectName)));
    view.addRepository(projectName);
  }

  @Override
  public void onGitRepositoryDeleted(
      String endpointId, RepositoryDeletedEventDto repositoryDeletedEventDto) {
    String projectName = repositoryDeletedEventDto.getProjectName();
    changes.remove(projectName);
    view.removeRepository(projectName);
  }

  @Override
  public void onGitStatusChanged(String endpointId, StatusChangedEventDto statusChangedEvent) {
    updateRepositoryData(statusChangedEvent.getProjectName());
  }

  @Override
  public void onGitCheckout(String endpointId, GitCheckoutEventDto gitCheckoutEvent) {
    // this update is needed to correctly handle checkout with force
    updateRepositoryData(gitCheckoutEvent.getProjectName());
  }

  @Override
  public void onRepositorySelectionChanged(String selectedProjectName) {
    if (selectedProjectName == null) {
      updateChangedFiles(new MutableAlteredFiles(null));
      return;
    }

    AlteredFiles alteredFilesToShow = changes.get(selectedProjectName);
    if (alteredFilesToShow == null) {
      alteredFilesToShow = new MutableAlteredFiles(null);
    }
    updateChangedFiles(alteredFilesToShow);
  }

  /** Removes first segment from given path. */
  private String removeProjectName(String path) {
    return path.substring(path.indexOf('/', 1) + 1);
  }

  /** Returns name of project in which given file is located. */
  private String extractProjectName(String path) {
    return path.substring(1, path.indexOf('/', 1));
  }

  /**
   * Updates project status on git panel.
   *
   * @param projectName name of repository
   * @param alteredFiles changed files
   */
  private void updateRepositoryChangesView(String projectName, AlteredFiles alteredFiles) {
    view.updateRepositoryChanges(projectName, alteredFiles.getFilesQuantity());
    if (projectName.equals(view.getSelectedRepository())) {
      updateChangedFiles(alteredFiles);
    }
  }

  private void updateChangedFiles(AlteredFiles alteredFiles) {
    changesPanelPresenter.show(alteredFiles);
  }

  /**
   * Reloads information about specified project and updates the panel. Does nothing if project is
   * not under git or doesn't exist.
   */
  private void updateRepositoryData(String projectName) {
    Project project = findProjectByName(projectName);
    if (project != null && isProjectUnderGit(project)) {
      reloadRepositoryData(project);
    }
  }

  /** Returns project by its name or null if project with specified name doesn't exist. */
  @Nullable
  private Project findProjectByName(String projectName) {
    for (Project project : appContext.getProjects()) {
      if (projectName.equals(project.getName())) {
        return project;
      }
    }
    return null;
  }

  /** Returns true if given project is under git version control system, false otherwise. */
  private boolean isProjectUnderGit(Project project) {
    return VCS_GIT.equals(project.getAttribute(VCS_PROVIDER_NAME));
  }
}
