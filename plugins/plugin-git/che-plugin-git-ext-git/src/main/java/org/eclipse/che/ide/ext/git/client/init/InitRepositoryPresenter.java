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
package org.eclipse.che.ide.ext.git.client.init;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Presenter for Git command Init Repository.
 *
 * @author Ann Zhuleva
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class InitRepositoryPresenter {
  public static final String INIT_COMMAND_NAME = "Git init";

  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final GitServiceClient service;
  private final GitLocalizationConstant constant;
  private final NotificationManager notificationManager;
  private final AppContext appContext;

  @Inject
  public InitRepositoryPresenter(
      GitLocalizationConstant constant,
      NotificationManager notificationManager,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter consolesPanelPresenter,
      GitServiceClient service,
      AppContext appContext) {
    this.constant = constant;
    this.notificationManager = notificationManager;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = consolesPanelPresenter;
    this.service = service;
    this.appContext = appContext;
  }

  public void initRepository(final Project project) {
    final GitOutputConsole console = gitOutputConsoleFactory.create(INIT_COMMAND_NAME);

    service
        .init(project.getLocation(), false)
        .then(
            ignored -> {
              console.print(constant.initSuccess());
              consolesPanelPresenter.addCommandOutput(console);
              notificationManager.notify(constant.initSuccess());

              project.synchronize();
            })
        .catchError(
            error -> {
              handleError(error.getCause(), console);
              consolesPanelPresenter.addCommandOutput(console);
            });
  }

  /**
   * Handler some action whether some exception happened.
   *
   * @param e exception what happened
   */
  private void handleError(@NotNull Throwable e, GitOutputConsole console) {
    String errorMessage =
        (e.getMessage() != null && !e.getMessage().isEmpty())
            ? e.getMessage()
            : constant.initFailed();
    console.printError(errorMessage);
    notificationManager.notify(constant.initFailed(), FAIL, FLOAT_MODE);
  }
}
