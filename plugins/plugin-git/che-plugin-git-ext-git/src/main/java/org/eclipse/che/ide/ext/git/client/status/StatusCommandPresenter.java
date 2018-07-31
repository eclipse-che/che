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
package org.eclipse.che.ide.ext.git.client.status;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;

/**
 * Handler to process actions with displaying the status of the Git work tree.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class StatusCommandPresenter {
  public static final String STATUS_COMMAND_NAME = "Git status";

  private final GitServiceClient service;
  private final GitOutputConsoleFactory gitOutputConsoleFactory;
  private final ProcessesPanelPresenter consolesPanelPresenter;
  private final GitLocalizationConstant constant;
  private final NotificationManager notificationManager;

  /** Create presenter. */
  @Inject
  public StatusCommandPresenter(
      GitServiceClient service,
      GitOutputConsoleFactory gitOutputConsoleFactory,
      ProcessesPanelPresenter processesPanelPresenter,
      GitLocalizationConstant constant,
      NotificationManager notificationManager) {
    this.service = service;
    this.gitOutputConsoleFactory = gitOutputConsoleFactory;
    this.consolesPanelPresenter = processesPanelPresenter;
    this.constant = constant;
    this.notificationManager = notificationManager;
  }

  /** Show status. */
  public void showStatus(Project project) {
    service
        .statusText(project.getLocation())
        .then(
            status -> {
              printGitStatus(status);
            })
        .catchError(
            error -> {
              notificationManager.notify(constant.statusFailed(), FAIL, FLOAT_MODE);
            });
  }

  /**
   * Print colored Git status to Output
   *
   * @param statusText text to be printed
   */
  private void printGitStatus(String statusText) {
    GitOutputConsole console = gitOutputConsoleFactory.create(STATUS_COMMAND_NAME);
    console.print("");

    List<String> statusLines = Arrays.asList(statusText.split("\n"));
    boolean containsStagedChanges = statusLines.contains("Changes to be committed:");
    boolean stagedChangesAlreadyPrinted = false;
    for (String line : statusLines) {
      if ((line.startsWith("\t") || line.startsWith("#\t"))
          && containsStagedChanges
          && !stagedChangesAlreadyPrinted) {
        console.print(line, Style.getVcsConsoleStagedFilesColor());
        if (statusLines.indexOf(line) == statusLines.size() - 1
            || statusLines.get(statusLines.indexOf(line) + 1).equals("")) {
          stagedChangesAlreadyPrinted = true;
        }
        continue;
      } else if ((line.startsWith("\t") || line.startsWith("#\t"))) {
        console.print(line, Style.getVcsConsoleUnstagedFilesColor());
        continue;
      }
      console.print(line);
    }

    consolesPanelPresenter.addCommandOutput(console);
  }
}
