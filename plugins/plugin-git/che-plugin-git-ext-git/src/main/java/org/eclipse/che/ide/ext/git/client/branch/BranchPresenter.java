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
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.event.project.ProjectUpdatedEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.InputCallback;

import javax.validation.constraints.NotNull;

import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_ALL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter for displaying and work with branches.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class BranchPresenter implements BranchView.ActionDelegate {
    public static final String BRANCH_RENAME_COMMAND_NAME   = "Git rename branch";
    public static final String BRANCH_DELETE_COMMAND_NAME   = "Git delete branch";
    public static final String BRANCH_CHECKOUT_COMMAND_NAME = "Git checkout branch";
    public static final String BRANCH_CREATE_COMMAND_NAME   = "Git create branch";
    public static final String BRANCH_LIST_COMMAND_NAME     = "Git list of branches";

    private final DtoFactory              dtoFactory;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final BranchView              view;
    private final ProjectServiceClient    projectService;
    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ConsolesPanelPresenter  consolesPanelPresenter;
    private final DialogFactory           dialogFactory;
    private final EventBus                eventBus;
    private final GitServiceClient        service;
    private final GitLocalizationConstant constant;
    private final EditorAgent             editorAgent;
    private final AppContext              appContext;
    private final NotificationManager     notificationManager;
    private final String                  workspaceId;

    private CurrentProject      project;
    private Branch              selectedBranch;

    /** Create presenter. */
    @Inject
    public BranchPresenter(BranchView view,
                           DtoFactory dtoFactory,
                           EditorAgent editorAgent,
                           GitServiceClient service,
                           ProjectServiceClient projectServiceClient,
                           GitLocalizationConstant constant,
                           AppContext appContext,
                           NotificationManager notificationManager,
                           DtoUnmarshallerFactory dtoUnmarshallerFactory,
                           GitOutputConsoleFactory gitOutputConsoleFactory,
                           ConsolesPanelPresenter consolesPanelPresenter,
                           DialogFactory dialogFactory,
                           EventBus eventBus) {
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.projectService = projectServiceClient;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.dialogFactory = dialogFactory;
        this.eventBus = eventBus;
        this.view.setDelegate(this);
        this.editorAgent = editorAgent;
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.workspaceId = appContext.getWorkspaceId();
    }

    /** Open dialog if closed and shows branches. */
    public void showBranches() {
        project = appContext.getCurrentProject();
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
            dialogFactory.createConfirmDialog(constant.branchConfirmRenameTitle(), constant.branchConfirmRenameMessage(),
                                              getConfirmRenameBranchCallback(), null).show();
        } else {
            renameBranch();
        }
    }

    private ConfirmCallback getConfirmRenameBranchCallback() {
        return new ConfirmCallback() {
            @Override
            public void accepted() {
                renameBranch();
            }
        };
    }

    private void renameBranch() {
        final String selectedBranchName = getSelectedBranchName();
        dialogFactory.createInputDialog(constant.branchTitleRename(), constant.branchTypeRename(), selectedBranchName,
                                        0, selectedBranchName.length(), getNewBranchNameCallback(), null).show();
    }

    private InputCallback getNewBranchNameCallback() {
        return new InputCallback() {
            @Override
            public void accepted(String newBranchName) {
                renameBranch(newBranchName);
            }
        };
    }

    private void renameBranch(String newName) {
        service.branchRename(workspaceId, project.getRootProject(), selectedBranch.getDisplayName(), newName,
                             new AsyncRequestCallback<String>() {
                                 @Override
                                 protected void onSuccess(String result) {
                                     getBranches();
                                 }

                                 @Override
                                 protected void onFailure(Throwable exception) {
                                     handleError(exception, BRANCH_RENAME_COMMAND_NAME);
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
        final String name = selectedBranch.getName();

        service.branchDelete(workspaceId, project.getRootProject(), name, true, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(String result) {
                getBranches();
            }

            @Override
            protected void onFailure(Throwable exception) {
                handleError(exception, BRANCH_DELETE_COMMAND_NAME);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onCheckoutClicked() {
        String name = selectedBranch.getDisplayName();

        if (name == null) {
            return;
        }

        final CheckoutRequest checkoutRequest = dtoFactory.createDto(CheckoutRequest.class);
        if (selectedBranch.isRemote()) {
            checkoutRequest.setTrackBranch(selectedBranch.getDisplayName());
        } else {
            checkoutRequest.setName(selectedBranch.getDisplayName());
        }

        final ProjectConfigDto root = project.getRootProject();
        final String path = root.getPath();
        final String projectType = root.getType();

        service.checkout(workspaceId, root, checkoutRequest, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(String result) {
                getBranches();
                //In this case we can have unconfigured state of the project,
                //so we must repeat the logic which is performed when we open a project
                projectService.getProject(workspaceId, path,
                                          new AsyncRequestCallback<ProjectConfigDto>(
                                                  dtoUnmarshallerFactory.newUnmarshaller(ProjectConfigDto.class)) {
                                              @Override
                                              protected void onSuccess(ProjectConfigDto result) {
                                                  result.setType(projectType);
                                                  updateProject(result);
                                              }

                                              @Override
                                              protected void onFailure(Throwable exception) {
                                                  notificationManager
                                                          .notify(exception.getLocalizedMessage(), FAIL, true, project.getProjectConfig());
                                              }
                                          });
            }

            @Override
            protected void onFailure(Throwable exception) {
                handleError(exception, BRANCH_CHECKOUT_COMMAND_NAME);
            }
        });
    }

    private void updateProject(final ProjectConfigDto projectToUpdate) {
        Promise<ProjectConfigDto> updateProjectPromise = projectService.updateProject(workspaceId, projectToUpdate.getPath(), projectToUpdate);
        updateProjectPromise.then(new Operation<ProjectConfigDto>() {
            @Override
            public void apply(ProjectConfigDto arg) throws OperationException {
                updateOpenedFiles();
                eventBus.fireEvent(new ProjectUpdatedEvent(arg.getPath(), arg));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(arg.getMessage(), FAIL, true, projectToUpdate);
            }
        });
    }

    private void updateOpenedFiles() {
        for (EditorPartPresenter editorPartPresenter : editorAgent.getOpenedEditors()) {
            final VirtualFile file = editorPartPresenter.getEditorInput().getFile();
            final String filePath = file.getPath();
            Unmarshallable<ItemReference> unmarshaller = dtoUnmarshallerFactory.newUnmarshaller(ItemReference.class);

            projectService.getItem(workspaceId, filePath,
                                   new AsyncRequestCallback<org.eclipse.che.api.project.shared.dto.ItemReference>(unmarshaller) {
                                       @Override
                                       protected void onSuccess(ItemReference itemReference) {
                                           eventBus.fireEvent(new FileContentUpdateEvent(filePath));
                                       }

                                       @Override
                                       protected void onFailure(Throwable throwable) {
                                           eventBus.fireEvent(new FileEvent(file, FileEvent.FileOperation.CLOSE));
                                       }
                                   });

        }
    }

    /** Get the list of branches. */
    private void getBranches() {
        service.branchList(workspaceId, project.getRootProject(), LIST_ALL,
                           new AsyncRequestCallback<List<Branch>>(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class)) {
                               @Override
                               protected void onSuccess(List<Branch> result) {
                                   if (result.isEmpty()) {
                                       dialogFactory.createMessageDialog(constant.branchTitle(),
                                                                         constant.initCommitWasNotPerformed(),
                                                                         null).show();
                                   } else {
                                       view.setBranches(result);
                                       view.showDialogIfClosed();
                                   }
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   handleError(exception, BRANCH_LIST_COMMAND_NAME);
                               }
                           }
                          );
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateClicked() {
        dialogFactory.createInputDialog(constant.branchCreateNew(), constant.branchTypeNew(), new InputCallback() {
            @Override
            public void accepted(String value) {
                if (!value.isEmpty()) {
                    service.branchCreate(workspaceId, project.getRootProject(), value, null,
                                         new AsyncRequestCallback<Branch>(dtoUnmarshallerFactory.newUnmarshaller(Branch.class)) {
                                             @Override
                                             protected void onSuccess(Branch result) {
                                                 getBranches();
                                             }

                                             @Override
                                             protected void onFailure(Throwable exception) {
                                                 handleError(exception, BRANCH_CREATE_COMMAND_NAME);
                                             }

                                         });
                }

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
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
        notificationManager.notify(errorMessage, FAIL, true, project.getRootProject());
    }

    private void printGitMessage(String messageText, GitOutputConsole console) {
        console.print("");
        String[] lines = messageText.split("\n");
        for (String line : lines) {
            console.printError(line);
        }
    }

}
