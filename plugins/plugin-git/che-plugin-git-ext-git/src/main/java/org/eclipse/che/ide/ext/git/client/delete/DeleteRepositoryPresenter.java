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
package org.eclipse.che.ide.ext.git.client.delete;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Delete repository command handler, performs deleting Git repository.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class DeleteRepositoryPresenter {
  private static final String DELETE_REPO_COMMAND_NAME = "Git delete repository";

  private final GitServiceClient service;
  private final GitLocalizationConstant constant;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;

  @Inject
  public DeleteRepositoryPresenter(
      GitServiceClient service,
      GitLocalizationConstant constant,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter,
      AppContext appContext,
      NotificationManager notificationManager) {
    this.service = service;
    this.constant = constant;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
  }

  /** Delete Git repository. */
  public void deleteRepository(Project project) {
    final GitOutputConsole console = gitOutputConsoleFactory.create(DELETE_REPO_COMMAND_NAME);

    service
        .deleteRepository(project.getLocation())
        .then(
            ignored -> {
              console.print(constant.deleteGitRepositorySuccess());
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.deleteGitRepositorySuccess());

              appContext.getRootProject().synchronize();
            })
        .catchError(
            error -> {
              console.printError(error.getMessage());
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.failedToDeleteRepository(), FAIL, FLOAT_MODE);
            });
  }
}
