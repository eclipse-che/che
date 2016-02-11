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
package org.eclipse.che.ide.ext.git.client.reset.files;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.git.shared.ResetRequest.ResetType;
import static org.eclipse.che.ide.ext.git.client.status.StatusCommandPresenter.STATUS_COMMAND_NAME;

/**
 * Presenter for resetting files from index.
 * <p/>
 * When user tries to reset files from index:
 * 1. Find Git work directory by selected item in browser tree.
 * 2. Get status for found work directory.
 * 3. Display files ready for commit in grid. (Checked items will be reseted from index).
 *
 * @author Ann Zhuleva
 */
@Singleton
public class ResetFilesPresenter implements ResetFilesView.ActionDelegate {
    private static final String RESET_COMMAND_NAME = "Git reset";

    private final DtoFactory              dtoFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final DialogFactory           dialogFactory;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final ResetFilesView          view;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;
    private final String                  workspaceId;

    private CurrentProject  project;
    private List<IndexFile> indexedFiles;

    /** Create presenter. */
    @Inject
    public ResetFilesPresenter(ResetFilesView view,
                               GitServiceClient service,
                               AppContext appContext,
                               GitLocalizationConstant constant,
                               NotificationManager notificationManager,
                               DtoFactory dtoFactory,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               DialogFactory dialogFactory,
                               GitOutputConsoleFactory gitOutputConsoleFactory,
                               ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dialogFactory = dialogFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;

        this.workspaceId = appContext.getWorkspaceId();
    }

    /** Show dialog. */
    public void showDialog() {
        project = appContext.getCurrentProject();

        service.status(workspaceId, project.getRootProject(),
                       new AsyncRequestCallback<Status>(dtoUnmarshallerFactory.newUnmarshaller(Status.class)) {
                           @Override
                           protected void onSuccess(Status result) {
                               if (result.isClean()) {
                                   dialogFactory.createMessageDialog(constant.messagesWarningTitle(), constant.indexIsEmpty(), null).show();
                                   return;
                               }

                               List<IndexFile> values = new ArrayList<>();
                               List<String> valuesTmp = new ArrayList<>();

                               valuesTmp.addAll(result.getAdded());
                               valuesTmp.addAll(result.getChanged());
                               valuesTmp.addAll(result.getRemoved());

                               for (String value : valuesTmp) {
                                   IndexFile indexFile = dtoFactory.createDto(IndexFile.class);
                                   indexFile.setPath(value);
                                   indexFile.setIndexed(true);
                                   values.add(indexFile);
                               }

                               if (values.isEmpty()) {
                                   dialogFactory.createMessageDialog(constant.messagesWarningTitle(), constant.indexIsEmpty(), null).show();
                                   return;
                               }

                               view.setIndexedFiles(values);
                               indexedFiles = values;
                               view.showDialog();
                           }

                           @Override
                           protected void onFailure(Throwable exception) {
                               String errorMassage = exception.getMessage() != null ? exception.getMessage() : constant.statusFailed();
                               GitOutputConsole console = gitOutputConsoleFactory.create(STATUS_COMMAND_NAME);
                               console.printError(errorMassage);
                               consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                               notificationManager.notify(errorMassage, project.getRootProject());
                           }
                       });
    }

    /** {@inheritDoc} */
    @Override
    public void onResetClicked() {
        List<String> files = new ArrayList<>();
        for (IndexFile indexFile : indexedFiles) {
            if (!indexFile.isIndexed()) {
                files.add(indexFile.getPath());
            }
        }
        final GitOutputConsole console = gitOutputConsoleFactory.create(RESET_COMMAND_NAME);
        if (files.isEmpty()) {
            view.close();
            console.printInfo(constant.nothingToReset());
            consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
            notificationManager.notify(constant.nothingToReset(), project.getRootProject());
            return;
        }
        view.close();

        service.reset(workspaceId, project.getRootProject(), "HEAD", ResetType.MIXED, files, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                console.printInfo(constant.resetFilesSuccessfully());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                notificationManager.notify(constant.resetFilesSuccessfully(), project.getRootProject());
            }

            @Override
            protected void onFailure(Throwable exception) {
                String errorMassage = exception.getMessage() != null ? exception.getMessage() : constant.resetFilesFailed();
                console.printError(errorMassage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                notificationManager.notify(errorMassage, project.getRootProject());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }
}
