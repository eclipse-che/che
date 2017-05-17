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
package org.eclipse.che.ide.ext.git.client.commit;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.ext.git.client.DateTimeFormatter;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.ext.git.client.compare.changedpanel.ChangedPanelPresenter;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.eclipse.che.api.git.shared.BranchListMode.LIST_REMOTE;
import static org.eclipse.che.api.git.shared.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.defineStatus;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for commit changes on git.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 * @author Igor Vinokur
 */
@Singleton
public class CommitPresenter implements CommitView.ActionDelegate {
    private static final String COMMIT_COMMAND_NAME = "Git commit";

    private final ChangedPanelPresenter   changedPanelPresenter;
    private final DialogFactory           dialogFactory;
    private final AppContext              appContext;
    private final CommitView              view;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;
    private final DateTimeFormatter       dateTimeFormatter;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;

    private Project      project;
    private Set<String>  allFiles;
    private List<String> filesToCommit;

    @Inject
    public CommitPresenter(final CommitView view,
                           GitServiceClient service,
                           ChangedPanelPresenter changedPanelPresenter,
                           GitLocalizationConstant constant,
                           NotificationManager notificationManager,
                           DialogFactory dialogFactory,
                           AppContext appContext,
                           DateTimeFormatter dateTimeFormatter,
                           GitOutputConsoleFactory gitOutputConsoleFactory,
                           ProcessesPanelPresenter processesPanelPresenter) {
        this.view = view;
        this.changedPanelPresenter = changedPanelPresenter;
        this.dialogFactory = dialogFactory;
        this.appContext = appContext;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.notificationManager = notificationManager;

        this.filesToCommit = new ArrayList<>();
        this.view.setChangedPanelView(changedPanelPresenter.getView());
    }

    public void showDialog(Project project) {
        this.project = project;

        checkState(project != null, "Null project occurred");
        checkState(project.getLocation().isPrefixOf(appContext.getResource().getLocation()),
                   "Given selected item is not descendant of given project");

        view.setValueToAmendCheckBox(false);
        view.setValueToPushAfterCommitCheckBox(false);
        view.setEnableAmendCheckBox(true);
        view.setEnablePushAfterCommitCheckBox(true);
        view.setEnableRemoteBranchesDropDownLis(false);
        service.diff(appContext.getDevMachine(), project.getLocation(), null, NAME_STATUS, false, 0, "HEAD", false)
               .then(diff -> {
                   service.log(appContext.getDevMachine(), project.getLocation(), null, -1, -1, false)
                          .then(arg -> {
                              if (diff.isEmpty()) {
                                  dialogFactory.createConfirmDialog(constant.commitTitle(),
                                                                    constant.commitNothingToCommitMessageText(),
                                                                    constant.buttonYes(),
                                                                    constant.buttonNo(),
                                                                    () -> {
                                                                        view.setValueToAmendCheckBox(true);
                                                                        view.setEnablePushAfterCommitCheckBox(false);
                                                                        setAmendCommitMessage();
                                                                        show(null);
                                                                    },
                                                                    null)
                                               .show();
                              } else {
                                  show(diff);
                              }
                          })
                          .catchError(error -> {
                              if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                                  service.getStatus(appContext.getDevMachine(), project.getLocation()).then(
                                          status -> {
                                              view.setEnableAmendCheckBox(false);
                                              view.setEnablePushAfterCommitCheckBox(false);
                                              List<String> newFiles = new ArrayList<>();
                                              newFiles.addAll(status.getAdded());
                                              newFiles.addAll(status.getUntracked());
                                              show(newFiles.stream().collect(joining("\nA ", "A ", "")));
                                          });
                              }
                          });
               })
               .catchError(arg -> {
                   notificationManager.notify(constant.diffFailed(), FAIL, FLOAT_MODE);
               });

        service.branchList(appContext.getDevMachine(), project.getLocation(), LIST_REMOTE)
               .then(view::setRemoteBranchesList)
               .catchError(error -> {
                   notificationManager.notify(constant.branchesListFailed(), FAIL, FLOAT_MODE);
               });
    }

    private void show(@Nullable String diff) {
        Map<String, Status> items = new HashMap<>();
        if (!isNullOrEmpty(diff)) {
            stream(diff.split("\n")).forEach(item -> items.put(item.substring(2, item.length()), defineStatus(item.substring(0, 1))));
        }
        filesToCommit.clear();
        allFiles = items.keySet();

        view.setEnableCommitButton(!view.getMessage().isEmpty());
        view.focusInMessageField();
        view.showDialog();
        changedPanelPresenter.show(items, null);
        view.checkCheckBoxes(stream(appContext.getResources()).map(resource -> resource.getLocation().removeFirstSegments(1))
                                                              .collect(Collectors.toSet()));
    }

    @Override
    public void onCommitClicked() {
        DevMachine devMachine = appContext.getDevMachine();
        Path location = project.getLocation();
        Path[] filesToCommitArray = new Path[filesToCommit.size()];
        filesToCommit.forEach(file -> filesToCommitArray[filesToCommit.indexOf(file)] = Path.valueOf(file));

        service.add(devMachine, location, false, filesToCommitArray)
               .then(arg -> {
                   service.commit(devMachine,
                                  location,
                                  view.getMessage(),
                                  false,
                                  filesToCommitArray,
                                  view.isAmend())
                          .then(revision -> {
                              onCommitSuccess(revision);
                              if (view.isPushAfterCommit()) {
                                  String remoteBranch = view.getRemoteBranch();
                                  String remote = remoteBranch.split("/")[0];
                                  String branch = remoteBranch.split("/")[1];
                                  service.push(devMachine,
                                               location,
                                               singletonList(branch),
                                               remote,
                                               false)
                                         .then(result -> {
                                             notificationManager.notify(constant.pushSuccess(remote), SUCCESS, FLOAT_MODE);
                                         })
                                         .catchError(error -> {
                                             notificationManager.notify(constant.pushFail(), FAIL, FLOAT_MODE);
                                         });
                              }
                              view.close();
                          })
                          .catchError(error -> {
                              handleError(error.getCause());
                          });
               })
               .catchError(error -> {
                   notificationManager.notify(constant.addFailed(), FAIL, FLOAT_MODE);
               });
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onValueChanged() {
        view.setEnableCommitButton(!view.getMessage().isEmpty() && (!filesToCommit.isEmpty() || view.isAmend()));
    }

    @Override
    public void setAmendCommitMessage() {
        service.log(appContext.getDevMachine(), project.getLocation(), null, -1, -1, false)
               .then(log -> {
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
               })
               .catchError(error -> {
                   if (getErrorCode(error.getCause()) == ErrorCodes.INIT_COMMIT_WAS_NOT_PERFORMED) {
                       dialogFactory.createMessageDialog(constant.commitTitle(),
                                                         constant.initCommitWasNotPerformed(),
                                                         null).show();
                   } else {
                       CommitPresenter.this.view.setMessage("");
                       notificationManager.notify(constant.logFailed(), FAIL, NOT_EMERGE_MODE);
                   }
               });
    }

    @Override
    public void onFileNodeCheckBoxValueChanged(Path path, boolean newCheckBoxValue) {
        if (newCheckBoxValue) {
            filesToCommit.add(path.toString());
        } else {
            filesToCommit.remove(path.toString());
        }
    }

    @Override
    public Set<String> getChangedFiles() {
        return allFiles;
    }

    private void onCommitSuccess(@NotNull final Revision revision) {
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

    private void handleError(@NotNull Throwable exception) {
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
}
