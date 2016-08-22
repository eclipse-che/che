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
package org.eclipse.che.ide.ext.git.client.push;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.BranchFilterByRemote;
import org.eclipse.che.ide.ext.git.client.BranchSearcher;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_LOCAL;
import static org.eclipse.che.api.git.shared.BranchListRequest.LIST_REMOTE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.eclipse.che.ide.ext.git.client.compare.branchList.BranchListPresenter.BRANCH_LIST_COMMAND_NAME;
import static org.eclipse.che.ide.ext.git.client.remote.RemotePresenter.REMOTE_REPO_COMMAND_NAME;

/**
 * Presenter for pushing changes to remote repository.
 *
 * @author Ann Zhuleva
 * @author Sergii Leschenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class PushToRemotePresenter implements PushToRemoteView.ActionDelegate {
    public static final String PUSH_COMMAND_NAME   = "Git push";
    public static final String CONFIG_COMMAND_NAME = "Git config";

    private final GitOutputConsoleFactory gitOutputConsoleFactory;
    private final ProcessesPanelPresenter processesPanelPresenter;
    private final DtoFactory              dtoFactory;
    private final BranchSearcher          branchSearcher;
    private final PushToRemoteView        view;
    private final GitServiceClient        service;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;
    private       Project                 project;

    @Inject
    public PushToRemotePresenter(DtoFactory dtoFactory,
                                 PushToRemoteView view,
                                 GitServiceClient service,
                                 AppContext appContext,
                                 GitLocalizationConstant constant,
                                 NotificationManager notificationManager,
                                 BranchSearcher branchSearcher,
                                 GitOutputConsoleFactory gitOutputConsoleFactory,
                                 ProcessesPanelPresenter processesPanelPresenter) {
        this.dtoFactory = dtoFactory;
        this.branchSearcher = branchSearcher;
        this.view = view;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.processesPanelPresenter = processesPanelPresenter;
    }

    public void showDialog(Project project) {
        this.project = project;

        updateRemotes();
    }

    /**
     * Get the list of remote repositories for local one.
     * If remote repositories are found, then get the list of branches (remote and local).
     */
    void updateRemotes() {
        service.remoteList(appContext.getDevMachine(), project.getLocation(), null, true).then(new Operation<List<Remote>>() {
            @Override
            public void apply(List<Remote> remotes) throws OperationException {
                updateLocalBranches();
                view.setRepositories(remotes);
                view.setEnablePushButton(!remotes.isEmpty());
                view.showDialog();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                String errorMessage = error.getMessage() != null ? error.getMessage() : constant.remoteListFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(REMOTE_REPO_COMMAND_NAME);
                console.printError(errorMessage);
                processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.remoteListFailed(), FAIL, FLOAT_MODE);
                view.setEnablePushButton(false);
            }
        });
    }

    /**
     * Update list of local and remote branches on view.
     */
    void updateLocalBranches() {
        //getting local branches
        getBranchesForCurrentProject(LIST_LOCAL, new AsyncCallback<List<Branch>>() {
            @Override
            public void onSuccess(List<Branch> result) {
                List<String> localBranches = branchSearcher.getLocalBranchesToDisplay(result);
                view.setLocalBranches(localBranches);

                for (Branch branch : result) {
                    if (branch.isActive()) {
                        view.selectLocalBranch(branch.getDisplayName());
                        break;
                    }
                }

                //getting remote branch only after selecting current local branch
                updateRemoteBranches();
            }

            @Override
            public void onFailure(Throwable exception) {
                String errorMessage = exception.getMessage() != null ? exception.getMessage() : constant.localBranchesListFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(BRANCH_LIST_COMMAND_NAME);
                console.printError(errorMessage);
                processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.localBranchesListFailed(), FAIL, FLOAT_MODE);
                view.setEnablePushButton(false);
            }
        });
    }

    /**
     * Update list of remote branches on view.
     */
    void updateRemoteBranches() {
        getBranchesForCurrentProject(LIST_REMOTE, new AsyncCallback<List<Branch>>() {
            @Override
            public void onSuccess(final List<Branch> result) {
                // Need to add the upstream of local branch in the list of remote branches
                // to be able to push changes to the remote upstream branch
                getUpstreamBranch(new AsyncCallback<Branch>() {
                    @Override
                    public void onSuccess(Branch upstream) {
                        BranchFilterByRemote remoteRefsHandler = new BranchFilterByRemote(view.getRepository());

                        final List<String> remoteBranches = branchSearcher.getRemoteBranchesToDisplay(remoteRefsHandler, result);

                        String selectedRemoteBranch = null;
                        if (upstream != null && upstream.isRemote() && remoteRefsHandler.isLinkedTo(upstream)) {
                            String simpleUpstreamName = remoteRefsHandler.getBranchNameWithoutRefs(upstream);
                            if (!remoteBranches.contains(simpleUpstreamName)) {
                                remoteBranches.add(simpleUpstreamName);
                            }
                            selectedRemoteBranch = simpleUpstreamName;
                        }

                        // Need to add the current local branch in the list of remote branches
                        // to be able to push changes to the remote branch  with same name
                        final String currentBranch = view.getLocalBranch();
                        if (!remoteBranches.contains(currentBranch)) {
                            remoteBranches.add(currentBranch);
                        }
                        if (selectedRemoteBranch == null) {
                            selectedRemoteBranch = currentBranch;
                        }

                        view.setRemoteBranches(remoteBranches);
                        view.selectRemoteBranch(selectedRemoteBranch);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        GitOutputConsole console = gitOutputConsoleFactory.create(CONFIG_COMMAND_NAME);
                        console.printError(constant.failedGettingConfig());
                        processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                        notificationManager.notify(constant.failedGettingConfig(), FAIL, FLOAT_MODE);
                    }
                });
            }

            @Override
            public void onFailure(Throwable exception) {
                String errorMessage = exception.getMessage() != null ? exception.getMessage() : constant.remoteBranchesListFailed();
                GitOutputConsole console = gitOutputConsoleFactory.create(BRANCH_LIST_COMMAND_NAME);
                console.printError(errorMessage);
                processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notificationManager.notify(constant.remoteBranchesListFailed(), FAIL, FLOAT_MODE);
                view.setEnablePushButton(false);
            }

        });
    }


    /**
     * Get upstream branch for selected local branch. Can invoke {@code onSuccess(null)} if upstream branch isn't set
     */
    private void getUpstreamBranch(final AsyncCallback<Branch> result) {

        final String configBranchRemote = "branch." + view.getLocalBranch() + ".remote";
        final String configUpstreamBranch = "branch." + view.getLocalBranch() + ".merge";
        service.config(appContext.getDevMachine(), project.getLocation(), Arrays.asList(configUpstreamBranch, configBranchRemote), false)
               .then(new Operation<Map<String, String>>() {
                   @Override
                   public void apply(Map<String, String> configs) throws OperationException {
                       if (configs.containsKey(configBranchRemote) && configs.containsKey(configUpstreamBranch)) {
                           String displayName = configs.get(configBranchRemote) + "/" + configs.get(configUpstreamBranch);
                           Branch upstream = dtoFactory.createDto(Branch.class)
                                                       .withActive(false)
                                                       .withRemote(true)
                                                       .withDisplayName(displayName)
                                                       .withName("refs/remotes/" + displayName);

                           result.onSuccess(upstream);
                       } else {
                           result.onSuccess(null);
                       }
                   }
               }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                result.onFailure(error.getCause());
            }
        });
    }

    /**
     * Get the list of branches.
     *
     * @param remoteMode
     *         is a remote mode
     */
    void getBranchesForCurrentProject(@NotNull final String remoteMode, final AsyncCallback<List<Branch>> asyncResult) {
        service.branchList(appContext.getDevMachine(), project.getLocation(), remoteMode).then(new Operation<List<Branch>>() {
            @Override
            public void apply(List<Branch> branches) throws OperationException {
                asyncResult.onSuccess(branches);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                asyncResult.onFailure(error.getCause());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onPushClicked() {
        final StatusNotification notification =
                notificationManager.notify(constant.pushProcess(), PROGRESS, FLOAT_MODE);

        final String repository = view.getRepository();
        final GitOutputConsole console = gitOutputConsoleFactory.create(PUSH_COMMAND_NAME);
        service.push(appContext.getDevMachine(), project.getLocation(), getRefs(), repository, false).then(new Operation<PushResponse>() {
            @Override
            public void apply(PushResponse response) throws OperationException {
                console.print(response.getCommandOutput());
                processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                notification.setStatus(SUCCESS);
                if (response.getCommandOutput().contains("Everything up-to-date")) {
                    notification.setTitle(constant.pushUpToDate());
                } else {
                    notification.setTitle(constant.pushSuccess(repository));
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                handleError(error.getCause(), notification, console);
                processesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
            }
        });
        view.close();
    }

    /** @return list of refs to push */
    @NotNull
    private List<String> getRefs() {
        String localBranch = view.getLocalBranch();
        String remoteBranch = view.getRemoteBranch();
        return singletonList(localBranch + ":" + remoteBranch);
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onLocalBranchChanged() {
        view.addRemoteBranch(view.getLocalBranch());
        view.selectRemoteBranch(view.getLocalBranch());
    }

    @Override
    public void onRepositoryChanged() {
        updateRemoteBranches();
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param throwable
     *         exception what happened
     */
    void handleError(@NotNull Throwable throwable, StatusNotification notification, GitOutputConsole console) {
        notification.setStatus(FAIL);
        if (throwable instanceof UnauthorizedException) {
            console.printError(constant.messagesNotAuthorized());
            notification.setTitle(constant.messagesNotAuthorized());
            return;
        }

        String errorMessage = throwable.getMessage();
        if (errorMessage == null) {
            console.printError(constant.pushFail());
            notification.setTitle(constant.pushFail());
            return;
        }

        try {
            errorMessage = dtoFactory.createDtoFromJson(errorMessage, ServiceError.class).getMessage();
            if (errorMessage.equals("Unable get private ssh key")) {
                console.printError(constant.messagesUnableGetSshKey());
                notification.setTitle(constant.messagesUnableGetSshKey());
                return;
            }
            console.printError(errorMessage);
            notification.setTitle(errorMessage);
        } catch (Exception e) {
            console.printError(errorMessage);
            notification.setTitle(errorMessage);
        }
    }
}
