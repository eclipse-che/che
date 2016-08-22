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
package org.eclipse.che.ide.ext.git.client.branch;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_ALL;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for displaying and work with branches.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class BranchPresenter implements BranchView.ActionDelegate {
    public static final String BRANCH_RENAME_COMMAND_NAME   = "Git rename branch";
    public static final String BRANCH_DELETE_COMMAND_NAME   = "Git delete branch";
    public static final String BRANCH_CHECKOUT_COMMAND_NAME = "Git checkout branch";
    public static final String BRANCH_CREATE_COMMAND_NAME   = "Git create branch";
    public static final String BRANCH_LIST_COMMAND_NAME     = "Git list of branches";

    private final DtoFactory              dtoFactory;
    private final BranchView              view;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter consolesPanelPresenter;
    private final DialogFactory           dialogFactory;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;

    private Branch  selectedBranch;
    private Project project;

    /** Create presenter. */
    @Inject
    public BranchPresenter(BranchView view,
                           DtoFactory dtoFactory,
                           GitServiceClient service,
                           GitLocalizationConstant constant,
                           AppContext appContext,
                           NotificationManager notificationManager,
                           GitOutputConsoleFactory gitOutputConsoleFactory,
                           ProcessesPanelPresenter processesPanelPresenter,
                           DialogFactory dialogFactory) {
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.dialogFactory = dialogFactory;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
    }

    /** Open dialog if closed and shows branches. */
    public void showBranches(Project project) {
        this.project = project;
        getBranches();
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onRenameClicked() {
        if (selectedBranch.isRemote()) {
            dialogFactory.createConfirmDialog(constant.branchConfirmRenameTitle(),
                                              constant.branchConfirmRenameMessage(),
                                              new ConfirmCallback() {
                                                  @Override
                                                  public void accepted() {
                                                      renameBranch();
                                                  }
                                              },
                                              null).show();
        } else {
            renameBranch();
        }
    }

    private void renameBranch() {
        final String selectedBranchName = getSelectedBranchName();
        dialogFactory.createInputDialog(constant.branchTitleRename(),
                                        constant.branchTypeRename(),
                                        selectedBranchName,
                                        0,
                                        selectedBranchName.length(),
                                        new InputCallback() {
                                            @Override
                                            public void accepted(String newBranchName) {
                                                renameBranch(newBranchName);
                                            }
                                        },
                                        null).show();
    }

    private void renameBranch(String newName) {
        service.branchRename(appContext.getDevMachine(), project.getLocation(), selectedBranch.getDisplayName(), newName)
               .then(new Operation<Void>() {
                   @Override
                   public void apply(Void ignored) throws OperationException {
                       getBranches();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError error) throws OperationException {
                       handleError(error.getCause(), BRANCH_RENAME_COMMAND_NAME);
                       getBranches();//rename of remote branch occurs in three stages, so needs update list of branches on view
                   }
               });
    }

    /** @return name of branch, e.g. 'origin/master' -> 'master' */
    private String getSelectedBranchName() {
        String selectedBranchName = selectedBranch.getDisplayName();
        String[] tokens = selectedBranchName.split("/");
        return tokens.length > 0 ? tokens[tokens.length - 1] : selectedBranchName;
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteClicked() {

        service.branchDelete(appContext.getDevMachine(), project.getLocation(), selectedBranch.getName(), true).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                getBranches();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                handleError(error.getCause(), BRANCH_DELETE_COMMAND_NAME);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCheckoutClicked() {
        final CheckoutRequest checkoutRequest = dtoFactory.createDto(CheckoutRequest.class);
        if (selectedBranch.isRemote()) {
            checkoutRequest.setTrackBranch(selectedBranch.getDisplayName());
        } else {
            checkoutRequest.setName(selectedBranch.getDisplayName());
        }

        service.checkout(appContext.getDevMachine(), project.getLocation(), checkoutRequest).then(new Operation<Void>() {
            @Override
            public void apply(Void ignored) throws OperationException {
                getBranches();

                project.synchronize();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                handleError(error.getCause(), BRANCH_CHECKOUT_COMMAND_NAME);
            }
        });
    }

    /** Get the list of branches. */
    private void getBranches() {
        service.branchList(appContext.getDevMachine(), project.getLocation(), LIST_ALL).then(new Operation<List<Branch>>() {
            @Override
            public void apply(List<Branch> branches) throws OperationException {
                if (branches.isEmpty()) {
                    dialogFactory.createMessageDialog(constant.branchTitle(),
                                                      constant.initCommitWasNotPerformed(),
                                                      null).show();
                } else {
                    view.setBranches(branches);
                    view.showDialogIfClosed();
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                handleError(error.getCause(), BRANCH_LIST_COMMAND_NAME);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateClicked() {
        dialogFactory.createInputDialog(constant.branchCreateNew(), constant.branchTypeNew(), new InputCallback() {
            @Override
            public void accepted(String value) {
                if (value.isEmpty()) {
                    return;
                }

                service.branchCreate(appContext.getDevMachine(), project.getLocation(), value, null).then(new Operation<Branch>() {
                    @Override
                    public void apply(Branch branch) throws OperationException {
                        getBranches();
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError error) throws OperationException {
                        handleError(error.getCause(), BRANCH_CREATE_COMMAND_NAME);
                    }
                });
            }
        }, null).show();
    }

    @Override
    public void onBranchUnselected() {
        selectedBranch = null;

        view.setEnableCheckoutButton(false);
        view.setEnableRenameButton(false);
        view.setEnableDeleteButton(false);
    }

    /** {@inheritDoc} */
    @Override
    public void onBranchSelected(@NotNull Branch branch) {
        selectedBranch = branch;
        boolean isActive = selectedBranch.isActive();

        view.setEnableCheckoutButton(!isActive);
        view.setEnableDeleteButton(!isActive);
        view.setEnableRenameButton(true);
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param exception
     *         exception what happened
     * @param commandName
     *         name of the executed command
     */
    void handleError(@NotNull Throwable exception, String commandName) {
        if (getErrorCode(exception) == ErrorCodes.UNABLE_GET_PRIVATE_SSH_KEY) {
            dialogFactory.createMessageDialog(commandName, constant.messagesUnableGetSshKey(), null).show();
            return;
        }

        String errorMessage = exception.getMessage();
        if (errorMessage == null) {
            switch (commandName) {
                case BRANCH_CREATE_COMMAND_NAME:
                    errorMessage = constant.branchCreateFailed();
                    break;
                case BRANCH_DELETE_COMMAND_NAME:
                    errorMessage = constant.branchDeleteFailed();
                    break;
                case BRANCH_LIST_COMMAND_NAME:
                    errorMessage = constant.branchesListFailed();
                    break;
                case BRANCH_RENAME_COMMAND_NAME:
                    errorMessage = constant.branchRenameFailed();
                    break;
                case BRANCH_CHECKOUT_COMMAND_NAME:
                    errorMessage = constant.branchCheckoutFailed();
                    break;
            }
        }

        GitOutputConsole console = gitOutputConsoleFactory.create(commandName);
        printGitMessage(errorMessage, console);
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
        notificationManager.notify(errorMessage, FAIL, FLOAT_MODE);
    }

    private void printGitMessage(String messageText, GitOutputConsole console) {
        console.print("");
        String[] lines = messageText.split("\n");
        for (String line : lines) {
            console.printError(line);
        }
    }
}
