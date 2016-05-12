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
package org.eclipse.che.ide.ext.git.client.url;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.remote.RemotePresenter.REMOTE_REPO_COMMAND_NAME;

/**
 * Presenter for showing git url.
 *
 * @author Ann Zhuleva
 * @author Oleksii Orel
 */
@Singleton
public class ShowProjectGitReadOnlyUrlPresenter implements ShowProjectGitReadOnlyUrlView.ActionDelegate {
    private static final String READ_ONLY_URL_COMMAND_NAME = "Git read only url";

    private final DtoUnmarshallerFactory        dtoUnmarshallerFactory;
    private final ShowProjectGitReadOnlyUrlView view;
    private final GitOutputConsoleFactory       gitOutputConsoleFactory;
    private final ConsolesPanelPresenter        consolesPanelPresenter;
    private final GitServiceClient              service;
    private final AppContext                    appContext;
    private final GitLocalizationConstant       constant;
    private final NotificationManager           notificationManager;

    @Inject
    public ShowProjectGitReadOnlyUrlPresenter(ShowProjectGitReadOnlyUrlView view,
                                              GitServiceClient service,
                                              AppContext appContext,
                                              GitLocalizationConstant constant,
                                              NotificationManager notificationManager,
                                              DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                              GitOutputConsoleFactory gitOutputConsoleFactory,
                                              ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /** Show dialog. */
    public void showDialog() {
        final CurrentProject project = appContext.getCurrentProject();
        view.showDialog();
        service.remoteList(appContext.getDevMachine(), project.getRootProject(), null, true,
                           new AsyncRequestCallback<List<Remote>>(dtoUnmarshallerFactory.newListUnmarshaller(Remote.class)) {
                               @Override
                               protected void onSuccess(List<Remote> result) {
                                   view.setRemotes(result);
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   view.setRemotes(null);
                                   String errorMessage =
                                           exception.getMessage() != null ? exception.getMessage()
                                                                          : constant.remoteListFailed();
                                   GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                                   console.printError(errorMessage);
                                   consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                                   notificationManager.notify(constant.remoteListFailed(), FAIL, FLOAT_MODE, project.getRootProject());
                               }
                           }
                          );
        service.getGitReadOnlyUrl(appContext.getDevMachine(), project.getRootProject(),
                                  new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                                      @Override
                                      protected void onSuccess(String result) {
                                          view.setLocaleUrl(result);
                                      }

                                      @Override
                                      protected void onFailure(Throwable exception) {
                                          String errorMessage = exception.getMessage() != null && !exception.getMessage().isEmpty()
                                                                ? exception.getMessage() : constant.initFailed();
                                          final GitOutputConsole console = gitOutputConsoleFactory.create(READ_ONLY_URL_COMMAND_NAME);
                                          console.printError(errorMessage);
                                          consolesPanelPresenter
                                                  .addCommandOutput(appContext.getDevMachine().getId(), console);
                                          notificationManager.notify(constant.initFailed(), FAIL, FLOAT_MODE, project.getRootProject());
                                      }
                                  });
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }
}
