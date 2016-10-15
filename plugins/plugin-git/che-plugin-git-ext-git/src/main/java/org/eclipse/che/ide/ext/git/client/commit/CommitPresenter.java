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
package org.eclipse.che.ide.ext.git.client.commit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.LogResponse;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.Arrays.isNullOrEmpty;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for commit changes on git.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CommitPresenter implements CommitView.ActionDelegate {
    public static final String COMMIT_COMMAND_NAME = "Git commit";

    private final DialogFactory           dialogFactory;
    private final AppContext              appContext;
    private final CommitView              view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;
    private final DateTimeFormatter       dateTimeFormatter;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;

    private       Project                 project;

    @Inject
    public CommitPresenter(CommitView view,
                           GitServiceClient service,
                           GitLocalizationConstant constant,
                           NotificationManager notificationManager,
                           DialogFactory dialogFactory,
                           AppContext appContext,
                           DateTimeFormatter dateTimeFormatter,
                           GitOutputConsoleFactory gitOutputConsoleFactory,
                           ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.notificationManager = notificationManager;
    }

    public void showDialog(Project project) {
        this.project = project;

        view.setAmend(false);
        view.setAllFilesInclude(false);
        view.setIncludeSelection(false);
        view.setOnlySelection(false);
        view.setEnableCommitButton(!view.getMessage().isEmpty());
        view.showDialog();
        view.focusInMessageField();
    }

    /** {@inheritDoc} */
    @Override
    public void onCommitClicked() {
        final String message = view.getMessage();
        final boolean all = view.isAllFilesInclued();
        final boolean amend = view.isAmend();
        final boolean selectionFlag = this.view.isIncludeSelection();
        final boolean onlySelectionFlag = this.view.isOnlySelection();

        if (selectionFlag) {
            addAndCommitSelection(message, amend);
        } else if (onlySelectionFlag) {
            commitSelection(message, amend);
        } else {
            doCommit(message, all, amend);
        }
    }

    protected void addAndCommitSelection(final String message, final boolean amend) {
        final Resource[] resources = appContext.getResources();

        if (isNullOrEmpty(resources)) {
            doCommit(message, false, amend);
        } else {
            service.add(appContext.getDevMachine(), project.getLocation(), false, toRelativePaths(resources))
                   .then(new Operation<Void>() {
                       @Override
                       public void apply(Void ignored) throws OperationException {
                           doCommit(message, false, amend);
                       }
                   });
        }
    }

    protected Path[] toRelativePaths(Resource[] resources) {
        final Path[] paths = new Path[resources.length];

        for (int i = 0; i < resources.length; i++) {
            checkState(project.getLocation().isPrefixOf(resources[i].getLocation()));

            final Path tmpPath = resources[i].getLocation().removeFirstSegments(project.getLocation().segmentCount());

            paths[i] = tmpPath.isEmpty() ? tmpPath.append(".") : tmpPath;
        }

        return paths;
    }

    protected void commitSelection(final String message, final boolean amend) {
        final Resource[] resources = appContext.getResources();

        checkState(!isNullOrEmpty(resources));

        service.commit(appContext.getDevMachine(), project.getLocation(), message, toRelativePaths(resources), amend)
               .then(new Operation<Revision>() {
                   @Override
                   public void apply(Revision revision) throws OperationException {
                       onCommitSuccess(revision);
                       view.close();
                   }
               });
    }

    protected void doCommit(final String message, final boolean all, final boolean amend) {
        service.commit(appContext.getDevMachine(), project.getLocation(), message, all, amend)
               .then(new Operation<Revision>() {
                   @Override
                   public void apply(Revision revision) throws OperationException {
                       onCommitSuccess(revision);
                       view.close();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       handleError(error.getCause());
                       view.close();
                   }
               });
    }

    protected void onCommitSuccess(@NotNull final Revision revision) {
        String date = dateTimeFormatter.getFormattedDate(revision.getCommitTime());
        String message = constant.commitMessage(revision.getId(), date);

        if ((revision.getCommitter() != null && revision.getCommitter().getName() != null &&
             !revision.getCommitter().getName().isEmpty())) {
            message += " " + constant.commitUser(revision.getCommitter().getName());
        }
        GitOutputConsole console = gitOutputConsoleFactory.create(COMMIT_COMMAND_NAME);
        console.print(message);
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
        notificationManager.notify(message);
        view.setMessage("");
    }

    protected void handleError(@NotNull Throwable exception) {
        if (exception instanceof ServerException &&
            ((ServerException)exception).getErrorCode() == ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED) {
            dialogFactory.createMessageDialog(constant.commitTitle(), constant.committerIdentityInfoEmpty(), null).show();
            return;
        }
        String exceptionMessage = exception.getMessage();
        String errorMessage = (exceptionMessage != null && !exceptionMessage.isEmpty()) ? exceptionMessage : constant.commitFailed();
        GitOutputConsole console = gitOutputConsoleFactory.create(COMMIT_COMMAND_NAME);
        console.printError(errorMessage);
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
        notificationManager.notify(constant.commitFailed(), errorMessage, FAIL, FLOAT_MODE);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        String message = view.getMessage();
        view.setEnableCommitButton(!message.isEmpty());
    }

    /** {@inheritDoc} */
    @Override
    public void setAmendCommitMessage() {
        service.log(appContext.getDevMachine(), project.getLocation(), null, false)
               .then(new Operation<LogResponse>() {
                   @Override
                   public void apply(LogResponse log) throws OperationException {
                       final List<Revision> commits = log.getCommits();
                       String message = "";
                       if (commits != null && (!commits.isEmpty())) {
                           final Revision tip = commits.get(0);
                           if (tip != null) {
                               message = tip.getMessage();
                           }
                       }
                       CommitPresenter.this.view.setMessage(message);
                       CommitPresenter.this.view.setEnableCommitButton(!message.isEmpty());
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                           dialogFactory.createMessageDialog(constant.commitTitle(),
                                                             constant.initCommitWasNotPerformed(),
                                                             null).show();
                       } else {
                           CommitPresenter.this.view.setMessage("");
                           notificationManager.notify(constant.logFailed(), FAIL, NOT_EMERGE_MODE);
                       }
                   }
               });
    }
}
