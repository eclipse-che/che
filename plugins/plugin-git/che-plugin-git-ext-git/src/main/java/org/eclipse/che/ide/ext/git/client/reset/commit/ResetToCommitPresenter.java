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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.OpenProjectEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.history.HistoryPresenter.LOG_COMMAND_NAME;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for resetting head to commit.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class ResetToCommitPresenter implements ResetToCommitView.ActionDelegate {
    public static final String RESET_COMMAND_NAME = "Git reset to commit";

    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final ResetToCommitView       view;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final DialogFactory           dialogFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;
    private final EventBus                eventBus;

    private Revision                  selectedRevision;

    @Inject
    public ResetToCommitPresenter(ResetToCommitView view,
                                  GitServiceClient service,
                                  GitLocalizationConstant constant,
                                  EventBus eventBus,
                                  DialogFactory dialogFactory,
                                  AppContext appContext,
                                  NotificationManager notificationManager,
                                  DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                  GitOutputConsoleFactory gitOutputConsoleFactory,
                                  ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.eventBus = eventBus;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    /**
     * Show dialog.
     */
    public void showDialog() {
        service.log(appContext.getDevMachine(), appContext.getCurrentProject().getRootProject(), null, false,
                    new AsyncRequestCallback<LogResponse>(dtoUnmarshallerFactory.newUnmarshaller(LogResponse.class)) {
                        @Override
                        protected void onSuccess(LogResponse result) {
                            view.setRevisions(result.getCommits());
                            view.setMixMode(true);
                            view.setEnableResetButton(selectedRevision != null);
                            view.showDialog();
                        }

                        @Override
                        protected void onFailure(Throwable exception) {
                            if (getErrorCode(exception) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                                dialogFactory.createMessageDialog(constant.resetCommitViewTitle(),
                                                                  constant.initCommitWasNotPerformed(),
                                                                  null).show();
                                return;
                            }
                            String errorMessage = (exception.getMessage() != null) ? exception.getMessage() : constant.logFailed();
                            GitOutputConsole console = gitOutputConsoleFactory.create(LOG_COMMAND_NAME);
                            console.printError(errorMessage);
                            consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                            notificationManager.notify(constant.logFailed(), FAIL, FLOAT_MODE, appContext.getCurrentProject().getRootProject());
                        }
                    }
                   );
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

        final ResetRequest.ResetType finalType = type;
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        final GitOutputConsole console = gitOutputConsoleFactory.create(RESET_COMMAND_NAME);
        service.reset(appContext.getDevMachine(), project, selectedRevision.getId(), finalType, null,
                      new AsyncRequestCallback<Void>() {
                          @Override
                          protected void onSuccess(Void result) {
                              if (ResetRequest.ResetType.HARD.equals(finalType) || ResetRequest.ResetType.MERGE.equals(finalType)) {
                                  // Only in the cases of <code>ResetRequest.ResetType.HARD</code>  or <code>ResetRequest.ResetType
                                  // .MERGE</code>
                                  // must change the workdir
                                  //In this case we can have unconfigured state of the project,
                                  //so we must repeat the logic which is performed when we open a project
                                  eventBus.fireEvent(new OpenProjectEvent(project));
                              }
                              console.print(constant.resetSuccessfully());
                              consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                              notificationManager.notify(constant.resetSuccessfully(), project);

                          }

                          @Override
                          protected void onFailure(Throwable exception) {
                              String errorMessage = (exception.getMessage() != null) ? exception.getMessage() : constant.resetFail();
                              console.printError(errorMessage);
                              consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                              notificationManager.notify(constant.resetFail(), FAIL, FLOAT_MODE, project);
                          }
                      });
    }
}

