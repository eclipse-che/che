/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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

import org.eclipse.che.api.git.shared.IndexFile;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

import static org.eclipse.che.api.git.shared.ResetRequest.ResetType;
import static org.eclipse.che.ide.ext.git.client.status.StatusCommandPresenter.STATUS_COMMAND_NAME;
import static org.eclipse.che.ide.util.Arrays.add;

/**
 * Presenter for resetting files from index.
 * <p/>
 * When user tries to reset files from index:
 * 1. Find Git work directory by selected item in browser tree.
 * 2. Get status for found work directory.
 * 3. Display files ready for commit in grid. (Checked items will be reseted from index).
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ResetFilesPresenter implements ResetFilesView.ActionDelegate {
    private static final String RESET_COMMAND_NAME = "Git reset";

    private final DtoFactory              dtoFactory;
    private final DialogFactory           dialogFactory;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;
    private final ResetFilesView          view;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;

    private IndexFile[] indexedFiles;
    private Project     project;

    /** Create presenter. */
    @Inject
    public ResetFilesPresenter(ResetFilesView view,
                               GitServiceClient service,
                               AppContext appContext,
                               GitLocalizationConstant constant,
                               NotificationManager notificationManager,
                               DtoFactory dtoFactory,
                               DialogFactory dialogFactory,
                               GitOutputConsoleFactory gitOutputConsoleFactory,
                               ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
    }

    /** Show dialog. */
    public void showDialog(Project project) {
        this.project = project;

        service.getStatus(appContext.getDevMachine(), project.getLocation()).then(new Operation<Status>() {
            @Override
            public void apply(Status status) throws OperationException {
                if (status.isClean()) {
                    dialogFactory.createMessageDialog(constant.messagesWarningTitle(), constant.indexIsEmpty(), null).show();
                    return;
                }

                indexedFiles = new IndexFile[0];

                for (String path : status.getAdded()) {
                    indexedFiles = add(indexedFiles, wrap(path));
                }

                for (String path : status.getChanged()) {
                    indexedFiles = add(indexedFiles, wrap(path));
                }

                for (String path : status.getRemoved()) {
                    indexedFiles = add(indexedFiles, wrap(path));
                }

                if (indexedFiles.length == 0) {
                    dialogFactory.createMessageDialog(constant.messagesWarningTitle(), constant.indexIsEmpty(), null).show();
                    return;
                }

                //Mark selected items to reset from index
                Resource[] resources = appContext.getResources();
                if (resources != null) {
                    for (Resource selectedItem : resources) {
                        String selectedItemPath = selectedItem.getLocation().removeFirstSegments(1).toString();
                        for (IndexFile file : indexedFiles)
                            if (file.getPath().startsWith(selectedItemPath)) {
                                file.setIndexed(false);
                            }
                    }
                }

                view.setIndexedFiles(indexedFiles);
                view.showDialog();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                String errorMassage = error.getMessage() != null ? error.getMessage() : constant.statusFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(STATUS_COMMAND_NAME);
                console.printError(errorMassage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(errorMassage);
            }
        });
    }

    protected IndexFile wrap(String path) {
        return dtoFactory.createDto(IndexFile.class).withPath(path).withIndexed(true);
    }

    /** {@inheritDoc} */
    @Override
    public void onResetClicked() {

        Path[] paths = new Path[0];
        for (IndexFile file : indexedFiles) {
            if (!file.isIndexed()) {
                paths = add(paths, Path.valueOf(file.getPath()));
            }
        }

        final GitOutputConsole console = gitOutputConsoleFactory.create(RESET_COMMAND_NAME);
        if (paths.length == 0) {
            view.close();
            console.print(constant.nothingToReset());
            consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
            notificationManager.notify(constant.nothingToReset());
            return;
        }
        view.close();

        service.reset(appContext.getDevMachine(), project.getLocation(), "HEAD", ResetType.MIXED, paths).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                console.print(constant.resetFilesSuccessfully());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.resetFilesSuccessfully());
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                String errorMassage = error.getMessage() != null ? error.getMessage() : constant.resetFilesFailed();
                console.printError(errorMassage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(errorMassage);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }
}
