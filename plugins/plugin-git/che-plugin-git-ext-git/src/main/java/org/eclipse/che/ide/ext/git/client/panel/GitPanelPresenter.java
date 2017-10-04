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
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.inject.Inject;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.AlteredFiles;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;

/**
 * Presenter for the left-side Git panel.
 *
 * @author Mykola Morhun
 */
@Singleton
public class GitPanelPresenter extends BasePresenter
    implements GitPanelView.ActionDelegate, ActivePartChangedHandler {

  private static final String REVISION = "HEAD";

  private final GitPanelView view;
  private final GitServiceClient service;
  private final ChangesPanelPresenter changesPanelPresenter;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final GitLocalizationConstant locale;

  private Project currentProject;

  @Inject
  public GitPanelPresenter(
      GitPanelView view,
      GitServiceClient service,
      ChangesPanelPresenter changesPanelPresenter,
      WorkspaceAgent workspaceAgent,
      AppContext appContext,
      EventBus eventBus,
      NotificationManager notificationManager,
      GitLocalizationConstant locale) {
    this.view = view;
    this.service = service;
    this.changesPanelPresenter = changesPanelPresenter;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.locale = locale;

    this.view.setDelegate(this);
    this.view.setChangesPanelView(this.changesPanelPresenter.getView());

    this.currentProject = null;

    if (partStack == null || !partStack.containsPart(this)) {
      workspaceAgent.openPart(this, PartStackType.NAVIGATION);
    }
    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  /** Invoked each time when panel is activated. */
  private void update() {
    Project selectedProject = appContext.getRootProject();
    // TODO handle deleted project
    //if (selectedProject != currentProject) {
    currentProject = selectedProject;

    service
        .diff(currentProject.getLocation(), null, NAME_STATUS, false, 0, REVISION, false)
        .then(
            diff -> {
              updateChangedFiles(new AlteredFiles(currentProject, diff));
            })
        .catchError(
            arg -> {
              currentProject = null; // To retry on next panel update TODO delete it
              notificationManager.notify(locale.diffFailed(), FAIL, NOT_EMERGE_MODE);
            });
    //}
  }

  // TODO invoke from file watcher subscriber or on open panel
  private void updateChangedFiles(AlteredFiles alteredFiles) {
    changesPanelPresenter.show(alteredFiles);
  }

  @Override
  public String getTitle() {
    return locale.panelTitle();
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

  public void showGitPanel() {
    onActivate();
  }

  public void hideGitPanel() {
    partStack.minimize();
  }

  public boolean isGitPanelOpened() {
    return partStack.getActivePart() == this;
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    if (event.getActivePart() != null && event.getActivePart() instanceof GitPanelPresenter) {
      update();
    }
  }
}
