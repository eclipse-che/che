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
package org.eclipse.che.ide.ext.git.client.reset.commit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.history.HistoryPresenter.LOG_COMMAND_NAME;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for resetting head to commit.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ResetToCommitPresenter implements ResetToCommitView.ActionDelegate {
    public static final String RESET_COMMAND_NAME = "Git reset to commit";

    private final ResetToCommitView       view;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final DialogFactory           dialogFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;

    private Revision selectedRevision;
    private Project  project;

    @Inject
    public ResetToCommitPresenter(ResetToCommitView view,
                                  GitServiceClient service,
                                  GitLocalizationConstant constant,
                                  DialogFactory dialogFactory,
                                  AppContext appContext,
                                  NotificationManager notificationManager,
                                  GitOutputConsoleFactory gitOutputConsoleFactory,
                                  ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    public void showDialog(final Project project) {
        this.project = project;

        service.log(appContext.getDevMachine(), project.getLocation(), null, false).then(new Operation<LogResponse>() {
            @Override
            public void apply(LogResponse log) throws OperationException {
                view.setRevisions(log.getCommits());
                view.setMixMode(true);
                view.setEnableResetButton(selectedRevision != null);
                view.showDialog();

                project.synchronize();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                    dialogFactory.createMessageDialog(constant.resetCommitViewTitle(),
                                                      constant.initCommitWasNotPerformed(),
                                                      null).show();
                    return;
                }
                String errorMessage = (error.getMessage() != null) ? error.getMessage() : constant.logFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(LOG_COMMAND_NAME);
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.logFailed(), FAIL, FLOAT_MODE);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResetClicked() {
        view.close();
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRevisionSelected(@NotNull Revision revision) {
        selectedRevision = revision;
        view.setEnableResetButton(selectedRevision != null);
    }

    /**
     * Reset current HEAD to the specified state and refresh project in the success case.
     */
    private void reset() {
        ResetRequest.ResetType type = view.isMixMode() ? ResetRequest.ResetType.MIXED : null;
        type = (type == null && view.isSoftMode()) ? ResetRequest.ResetType.SOFT : type;
        type = (type == null && view.isHardMode()) ? ResetRequest.ResetType.HARD : type;

        final GitOutputConsole console = gitOutputConsoleFactory.create(RESET_COMMAND_NAME);

        service.reset(appContext.getDevMachine(), project.getLocation(), selectedRevision.getId(), type, null).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                console.print(constant.resetSuccessfully());
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.resetSuccessfully());

                project.synchronize();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                String errorMessage = (error.getMessage() != null) ? error.getMessage() : constant.resetFail();
                console.printError(errorMessage);
                consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.resetFail(), FAIL, FLOAT_MODE);
            }
        });
    }
}

