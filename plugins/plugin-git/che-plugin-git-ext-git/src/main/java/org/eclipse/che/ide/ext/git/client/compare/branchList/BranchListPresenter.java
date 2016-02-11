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
package org.eclipse.che.ide.ext.git.client.compare.branchList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changedList.ChangedListPresenter;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_ALL;
import static org.eclipse.che.api.git.shared.DiffRequest.DiffType.NAME_STATUS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for displaying list of branches for comparing selected with local changes.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BranchListPresenter implements BranchListView.ActionDelegate {
    public static final String BRANCH_LIST_COMMAND_NAME = "Git list of branches";

    private final ComparePresenter         comparePresenter;
    private final ChangedListPresenter     changedListPresenter;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;
    private final BranchListView           view;
    private final DialogFactory            dialogFactory;
    private final ProjectExplorerPresenter projectExplorer;
    private final GitServiceClient         gitService;
    private final GitLocalizationConstant  locale;
    private final AppContext               appContext;
    private final NotificationManager      notificationManager;
    private final String                   workspaceId;

    private CurrentProject project;
    private Branch         selectedBranch;

    @Inject
    public BranchListPresenter(BranchListView view,
                               ComparePresenter comparePresenter,
                               ChangedListPresenter changedListPresenter,
                               GitServiceClient service,
                               GitLocalizationConstant locale,
                               AppContext appContext,
                               NotificationManager notificationManager,
                               DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               DialogFactory dialogFactory,
                               ProjectExplorerPresenter projectExplorer,
                               GitOutputConsoleFactory gitOutputConsoleFactory,
                               ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.comparePresenter = comparePresenter;
        this.changedListPresenter = changedListPresenter;
        this.dialogFactory = dialogFactory;
        this.projectExplorer = projectExplorer;
        this.gitService = service;
        this.locale = locale;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.workspaceId = appContext.getWorkspaceId();

        this.view.setDelegate(this);
    }

    /** Open dialog and shows branches to compare. */
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
    public void onCompareClicked() {
        ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        String pattern;
        String path;

        Selection<ResourceBasedNode<?>> selection = getExplorerSelection();

        if (selection == null || selection.getHeadElement() == null) {
            path = project.getPath();
        } else {
            path = ((HasStorablePath)selection.getHeadElement()).getStorablePath();
        }

        pattern = path.replaceFirst(project.getPath(), "");
        pattern = (pattern.startsWith("/")) ? pattern.replaceFirst("/", "") : pattern;

        gitService.diff(workspaceId, project, Collections.singletonList(pattern), NAME_STATUS, false, 0, selectedBranch.getName(), false,
                        new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                            @Override
                            protected void onSuccess(String result) {
                                if (result.isEmpty()) {
                                    dialogFactory.createMessageDialog(locale.compareMessageIdenticalContentTitle(),
                                                                      locale.compareMessageIdenticalContentText(), new ConfirmCallback() {
                                                @Override
                                                public void accepted() {
                                                    //Do nothing
                                                }
                                            }).show();
                                } else {
                                    String[] changedFiles = result.split("\n");
                                    if (changedFiles.length == 1) {
                                        comparePresenter.show(changedFiles[0].substring(2), changedFiles[0].substring(0, 1),
                                                              selectedBranch.getName());
                                    } else {
                                        Map<String, String> items = new HashMap<>();
                                        for (String item : changedFiles) {
                                            items.put(item.substring(2, item.length()), item.substring(0, 1));
                                        }
                                        changedListPresenter.show(items, selectedBranch.getName());
                                    }
                                }
                            }

                            @Override
                            protected void onFailure(Throwable exception) {
                                notificationManager.notify(locale.diffFailed(), FAIL, false);
                            }
                        });
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onBranchUnselected() {
        selectedBranch = null;
        view.setEnableCompareButton(false);
    }

    /** {@inheritDoc} */
    @Override
    public void onBranchSelected(@NotNull Branch branch) {
        selectedBranch = branch;

        view.setEnableCompareButton(true);
    }

    /** Get list of branches from selected project. */
    private void getBranches() {
        gitService.branchList(workspaceId, project.getRootProject(), LIST_ALL,
                              new AsyncRequestCallback<List<Branch>>(dtoUnmarshallerFactory.newListUnmarshaller(Branch.class)) {
                                  @Override
                                  protected void onSuccess(List<Branch> result) {
                                      view.setBranches(result);
                                      view.showDialog();
                                  }

                                  @Override
                                  protected void onFailure(Throwable exception) {
                                      final String errorMessage =
                                              (exception.getMessage() != null) ? exception.getMessage() : locale.branchesListFailed();
                                      GitOutputConsole console = gitOutputConsoleFactory.create(BRANCH_LIST_COMMAND_NAME);
                                      console.printError(errorMessage);
                                      consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
                                      notificationManager.notify(locale.branchesListFailed(), FAIL, false);
                                  }
                              }
                             );
    }

    private Selection<ResourceBasedNode<?>> getExplorerSelection() {
        final Selection<ResourceBasedNode<?>> selection = (Selection<ResourceBasedNode<?>>)projectExplorer.getSelection();
        if (selection == null || selection.isEmpty() || selection.getHeadElement() instanceof HasStorablePath) {
            return selection;
        } else {
            return null;
        }
    }
}
