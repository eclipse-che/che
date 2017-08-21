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
package org.eclipse.che.ide.workspace;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.BrowserAddress;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.factory.utils.InitialProjectImporter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.LoaderPresenter;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.StartWorkspacePresenter;

/**
 * Performs default start of IDE - creates new or starts latest workspace. Used when no {@code
 * factory} specified.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class DefaultWorkspaceComponent extends WorkspaceComponent {

  private InitialProjectImporter initialProjectImporter;

  @Inject
  public DefaultWorkspaceComponent(
      WorkspaceServiceClient workspaceServiceClient,
      CreateWorkspacePresenter createWorkspacePresenter,
      StartWorkspacePresenter startWorkspacePresenter,
      CoreLocalizationConstant locale,
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      BrowserAddress browserAddress,
      DialogFactory dialogFactory,
      PreferencesManager preferencesManager,
      DtoFactory dtoFactory,
      LoaderPresenter loader,
      RequestTransmitter transmitter,
      InitialProjectImporter initialProjectImporter,
      WorkspaceEventsHandler handler) {
    super(
        workspaceServiceClient,
        createWorkspacePresenter,
        startWorkspacePresenter,
        locale,
        dtoUnmarshallerFactory,
        eventBus,
        appContext,
        notificationManager,
        browserAddress,
        dialogFactory,
        preferencesManager,
        dtoFactory,
        loader,
        transmitter);

    this.initialProjectImporter = initialProjectImporter;
  }

  /** {@inheritDoc} */
  @Override
  public void start(final Callback<Component, Exception> callback) {
    this.callback = callback;
    workspaceServiceClient
        .getWorkspace(browserAddress.getWorkspaceKey())
        .then(
            workspaceDto -> {
              handleWorkspaceEvents(workspaceDto, callback, null);
            })
        .catchError(
            error -> {
              needToReloadComponents = true;
              String dialogTitle = locale.getWsErrorDialogTitle();
              String dialogContent = locale.getWsErrorDialogContent(error.getMessage());
              dialogFactory.createMessageDialog(dialogTitle, dialogContent, null).show();
            });
  }

  @Override
  public void onWsAgentStarted(WsAgentStateEvent event) {
    super.onWsAgentStarted(event);

    Scheduler.get()
        .scheduleDeferred(
            () -> {
              importProjects();
            });
  }

  /** Imports all projects described in workspace configuration but not existed on file system. */
  private void importProjects() {
    final Project[] projects = appContext.getProjects();

    List<Project> importProjects = new ArrayList<>();
    for (Project project : projects) {
      if (project.getSource() == null
          || project.getSource().getLocation() == null
          || project.exists()) {
        continue;
      }

      importProjects.add(project);
    }

    initialProjectImporter.importProjects(importProjects);
  }
}
