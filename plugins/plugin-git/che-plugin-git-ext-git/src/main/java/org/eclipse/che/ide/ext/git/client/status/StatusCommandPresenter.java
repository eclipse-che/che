/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.status;

import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.api.git.shared.StatusFormat.LONG;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Handler to process actions with displaying the status of the Git work tree.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class StatusCommandPresenter {
    public static final String STATUS_COMMAND_NAME = "Git status";

    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;

    /**
     * Create presenter.
     */
    @Inject
    public StatusCommandPresenter(GitServiceClient service,
                                  AppContext appContext,
                                  GitOutputConsoleFactory gitOutputConsoleFactory,
                                  ConsolesPanelPresenter consolesPanelPresenter,
                                  GitLocalizationConstant constant,
                                  NotificationManager notificationManager) {
        this.service = service;
        this.appContext = appContext;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.constant = constant;
        this.notificationManager = notificationManager;
    }

    /** Show status. */
    public void showStatus() {
        final CurrentProject project = appContext.getCurrentProject();
        if (project == null) {
            return;
        }

        service.statusText(appContext.getDevMachine(), project.getRootProject(), LONG,
                           new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                               @Override
                               protected void onSuccess(String result) {
                                   printGitStatus(result);
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   notificationManager.notify(constant.statusFailed(), FAIL, true, project.getRootProject());
                               }
                           });
    }

    /**
     * Print colored Git status to Output
     *
     * @param statusText
     *         text to be printed
     */
    private void printGitStatus(String statusText) {
        GitOutputConsole console = gitOutputConsoleFactory.create(STATUS_COMMAND_NAME);
        console.print("");

        List<String> statusLines = Arrays.asList(statusText.split("\n"));
        boolean containsStagedChanges = statusLines.contains("Changes to be committed:");
        boolean stagedChangesAlreadyPrinted = false;
        for (String line : statusLines) {
            if ((line.startsWith("\t") || line.startsWith("#\t")) && containsStagedChanges && !stagedChangesAlreadyPrinted) {
                console.print(line, Style.getVcsConsoleStagedFilesColor());
                if (statusLines.indexOf(line) == statusLines.size() - 1 || statusLines.get(statusLines.indexOf(line) + 1).equals("")) {
                    stagedChangesAlreadyPrinted = true;
                }
                continue;
            } else if ((line.startsWith("\t") || line.startsWith("#\t"))) {
                console.print(line, Style.getVcsConsoleUnstagedFilesColor());
                continue;
            }
            console.print(line);
        }

        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
    }
}
