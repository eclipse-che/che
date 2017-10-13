/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.panel;

import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitEventSubscribable;
import org.eclipse.che.ide.ext.git.client.GitEventsSubscriber;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.compare.MutableAlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Presenter for the Git panel.
 *
 * @author Mykola Morhun
 */
@Singleton
public class GitPanelPresenter extends BasePresenter
    implements GitPanelView.ActionDelegate, GitEventsSubscriber {

  private static final String REVISION = "HEAD";

  private final GitPanelView view;
  private final GitServiceClient service;
  private final ChangesPanelPresenter changesPanelPresenter;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final GitEventSubscribable subscribeToGitEvents;
  private final GitResources gitResources;
  private final GitLocalizationConstant locale;

  private boolean initialized;
  private Map<String, MutableAlteredFiles> changes;
  private String selectedProjectName;

  @Inject
  public GitPanelPresenter(
      GitPanelView view,
      GitServiceClient service,
      ChangesPanelPresenter changesPanelPresenter,
      WorkspaceAgent workspaceAgent,
      AppContext appContext,
      GitEventSubscribable subscribeToGitEvents,
      NotificationManager notificationManager,
      GitResources gitResources,
      GitLocalizationConstant locale) {
    this.view = view;
    this.service = service;
    this.changesPanelPresenter = changesPanelPresenter;
    this.appContext = appContext;
    this.subscribeToGitEvents = subscribeToGitEvents;
    this.notificationManager = notificationManager;
    this.gitResources = gitResources;
    this.locale = locale;

    this.view.setDelegate(this);
    this.view.setChangesPanelView(this.changesPanelPresenter.getView());

    if (partStack == null || !partStack.containsPart(this)) {
      workspaceAgent.openPart(this, PartStackType.NAVIGATION);
    }

    this.initialized = false;
  }

  /** Invoked each time when panel is activated. */
  @Override
  public void onOpen() {
    if (!initialized) {
      loadPanelData();
      subscribeToGitEvents.addSubscriber(this);

      initialized = true;
    }
  }

  /**
   * Queries from server all data needed to initialize the panel.
   */
  private void loadPanelData() {
    this.changes = new HashMap<>();

    for (Project project : appContext.getProjects()) {
      String projectName = project.getLocation().toString().substring(1);
      view.addRepository(projectName);
      service
          .diff(project.getLocation(), null, NAME_STATUS, false, 0, REVISION, false)
          .then(
              diff -> {
                MutableAlteredFiles alteredFiles = new MutableAlteredFiles(project, diff);
                changes.put(projectName, alteredFiles);
                view.updateRepositoryChanges(projectName, alteredFiles.getFilesQuantity());
              })
          .catchError(
              arg -> {
                notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
              });
    }
  }

  private void updateChangedFiles(AlteredFiles alteredFiles) {
    changesPanelPresenter.show(alteredFiles);
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
    onActivate();
  }

  public void hide() {
    partStack.minimize();
  }

  public boolean isOpened() {
    return partStack.getActivePart() == this;
  }

  @Override
  public void onFileChanged(String endpointId, FileChangedEventDto dto) {
    String projectName = extractProjectName(dto.getPath());
    MutableAlteredFiles alteredFiles = changes.get(projectName);
    if (alteredFiles == null) { // TODO delete debug code.
      Log.error(getClass(), "Project '" + projectName + "' should be registered in the git panel.");
      return;
    }

    switch (dto.getStatus()) {
      case MODIFIED:
        if (alteredFiles.addFile(removeProjectName(dto.getPath()), Status.MODIFIED)) {
          view.updateRepositoryChanges(projectName, alteredFiles.getFilesQuantity());
          if (projectName.equals(selectedProjectName)) {
            updateChangedFiles(alteredFiles);
          }
        }
        break;
      case NOT_MODIFIED:
        if (alteredFiles.removeFile(removeProjectName(dto.getPath()))) {
          view.updateRepositoryChanges(projectName, alteredFiles.getFilesQuantity());
          if (projectName.equals(selectedProjectName)) {
            updateChangedFiles(alteredFiles);
          }
        }
        break;
      default:
        // do nothing
    }
  }

  @Override
  public void onGitStatusChanged(String endpointId, StatusChangedEventDto dto) {
    // TODO handle project name
  }

  @Override
  public void onGitCheckout(String endpointId, GitCheckoutEventDto dto) {
    // TODO handle project name
  }

  /** Removes first segment from given path. */
  private String removeProjectName(String path) {
    return path.substring(path.indexOf('/', 1) + 1);
  }

  /** Returns name of project in which given file is located. */
  private String extractProjectName(String path) {
    return path.substring(1, path.indexOf('/', 1));
  }

  @Override
  public void onRepositorySelectionChanged(String selectedProjectName) {
    this.selectedProjectName = selectedProjectName;
    updateChangedFiles(changes.get(selectedProjectName));
  }
}
