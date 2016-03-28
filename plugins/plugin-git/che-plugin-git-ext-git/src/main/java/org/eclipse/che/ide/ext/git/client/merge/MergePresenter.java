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
package org.eclipse.che.ide.ext.git.client.merge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.MergeResult;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_REMOTE;
import static org.eclipse.che.api.git.shared.MergeResult.MergeStatus.ALREADY_UP_TO_DATE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.merge.Reference.RefType.LOCAL_BRANCH;
import static org.eclipse.che.ide.ext.git.client.merge.Reference.RefType.REMOTE_BRANCH;

/**
 * Presenter to perform merge reference with current HEAD commit.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class MergePresenter implements MergeView.ActionDelegate {
    public static final String MERGE_COMMAND_NAME    = "Git merge";
    public static final String LOCAL_BRANCHES_TITLE  = "Local Branches";
    public static final String REMOTE_BRANCHES_TITLE = "Remote Branches";

    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final MergeView                view;
    private final DialogFactory            dialogFactory;
    private final ProjectExplorerPresenter projectExplorer;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;
    private final GitServiceClient         service;
    private final EventBus                 eventBus;
    private final GitLocalizationConstant  constant;
    private final EditorAgent              editorAgent;
    private final AppContext               appContext;
    private final NotificationManager      notificationManager;
    private final String                   workspaceId;

    private Reference selectedReference;

    @Inject
    public MergePresenter(MergeView view,
                          EventBus eventBus,
                          EditorAgent editorAgent,
                          GitServiceClient service,
                          GitLocalizationConstant constant,
                          AppContext appContext,
                          NotificationManager notificationManager,
                          DialogFactory dialogFactory,
                          DtoUnmarshallerFactory dtoUnmarshallerFactory,
                          ProjectExplorerPresenter projectExplorer,
                          GitOutputConsoleFactory gitOutputConsoleFactory,
                          ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.dialogFactory = dialogFactory;
        this.projectExplorer = projectExplorer;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.view.setDelegate(this);
        this.service = service;
        this.eventBus = eventBus;
        this.constant = constant;
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        this.workspaceId = appContext.getWorkspaceId();
    }

    /** Show dialog. */
    public void showDialog() {
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        final GitOutputConsole console = gitOutputConsoleFactory.create(MERGE_COMMAND_NAME);
        selectedReference = null;
        view.setEnableMergeButton(false);

        service.branchList(workspaceId, project, LIST_LOCAL,
                           new AsyncRequestCallback<List<Branch>>(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class)) {
                               @Override
                               protected void onSuccess(List<Branch> result) {
                                   List<Reference> references = new ArrayList<>();
                                   for (Branch branch : result) {
                                       if (!branch.isActive()) {
                                           Reference reference = new Reference(branch.getName(), branch.getDisplayName(), LOCAL_BRANCH);
                                           references.add(reference);
                                       }
                                   }
                                   view.setLocalBranches(references);
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   console.printError(exception.getMessage());
                                   consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                   notificationManager.notify(constant.branchesListFailed(), FAIL, true, project);
                               }
                           });

        service.branchList(workspaceId, project, LIST_REMOTE,
                           new AsyncRequestCallback<List<Branch>>(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class)) {
                               @Override
                               protected void onSuccess(List<Branch> result) {
                                   List<Reference> references = new ArrayList<>();
                                   for (Branch branch : result) {
                                       if (!branch.isActive()) {
                                           Reference reference =
                                                   new Reference(branch.getName(), branch.getDisplayName(), REMOTE_BRANCH);
                                           references.add(reference);
                                       }
                                   }
                                   view.setRemoteBranches(references);
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   console.printError(exception.getMessage());
                                   consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                   notificationManager.notify(constant.branchesListFailed(), FAIL, true, project);
                               }
                           });

        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onMergeClicked() {
        view.close();

        final GitOutputConsole console = gitOutputConsoleFactory.create(MERGE_COMMAND_NAME);
        service.merge(workspaceId, appContext.getCurrentProject().getRootProject(), selectedReference.getDisplayName(),
                      new AsyncRequestCallback<MergeResult>(dtoUnmarshallerFactory.newUnmarshaller(MergeResult.class)) {
                          @Override
                          protected void onSuccess(final MergeResult result) {
                              console.print(formMergeMessage(result));
                              consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                              notificationManager.notify(formMergeMessage(result), appContext.getCurrentProject().getRootProject());
                              refreshProject(editorAgent.getOpenedEditors());
                          }

                          @Override
                          protected void onFailure(Throwable exception) {
                              if (exception instanceof ServerException &&
                                  ((ServerException)exception).getErrorCode() == ErrorCodes.NO_COMMITTER_NAME_OR_EMAIL_DEFINED) {
                                  dialogFactory.createMessageDialog(constant.mergeTitle(), constant.committerIdentityInfoEmpty(),
                                                                    new ConfirmCallback() {
                                                                        @Override
                                                                        public void accepted() {
                                                                            //do nothing
                                                                        }
                                                                    }).show();
                                  return;
                              }
                              console.printError(exception.getMessage());
                              consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                              notificationManager
                                      .notify(constant.mergeFailed(), FAIL, true, appContext.getCurrentProject().getRootProject());
                          }
                      });
    }

    /**
     * Refresh project.
     *
     * @param openedEditors
     *         editors that corresponds to open files
     */
    private void refreshProject(final List<EditorPartPresenter> openedEditors) {
        projectExplorer.reloadChildren();
        for (EditorPartPresenter partPresenter : openedEditors) {
            final VirtualFile file = partPresenter.getEditorInput().getFile();
            eventBus.fireEvent(new FileContentUpdateEvent(file.getPath()));
        }
    }

    /**
     * Form the result message of the merge operation.
     *
     * @param mergeResult
     *         result of merge operation
     * @return {@link String} merge result message
     */
    @NotNull
    private String formMergeMessage(@NotNull MergeResult mergeResult) {
        if (mergeResult.getMergeStatus().equals(ALREADY_UP_TO_DATE)) {
            return mergeResult.getMergeStatus().getValue();
        }

        StringBuilder conflictMessage = new StringBuilder();
        List<String> conflicts = mergeResult.getConflicts();
        if (conflicts != null && conflicts.size() > 0) {
            for (String conflict : conflicts) {
                conflictMessage.append("- ").append(conflict);
            }
        }
        StringBuilder commitsMessage = new StringBuilder();
        List<String> commits = mergeResult.getMergedCommits();
        if (commits != null && commits.size() > 0) {
            for (String commit : commits) {
                commitsMessage.append("- ").append(commit);
            }
        }

        String message = "<b>" + mergeResult.getMergeStatus().getValue() + "</b>";
        String conflictText = conflictMessage.toString();
        message += (!conflictText.isEmpty()) ? constant.mergedConflicts() : "";


        String commitText = commitsMessage.toString();
        message += (!commitText.isEmpty()) ? " " + constant.mergedCommits(commitText) : "";
        message += (mergeResult.getNewHead() != null) ? " " + constant.mergedNewHead(mergeResult.getNewHead()) : "";
        return message;
    }

    /** {@inheritDoc} */
    @Override
    public void onReferenceSelected(@NotNull Reference reference) {
        selectedReference = reference;
        String displayName = selectedReference.getDisplayName();
        boolean isEnabled = !displayName.equals(LOCAL_BRANCHES_TITLE) && !displayName.equals(REMOTE_BRANCHES_TITLE);
        view.setEnableMergeButton(isEnabled);
    }
}
