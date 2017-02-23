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
package org.eclipse.che.plugin.pullrequest.client.vcs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Git backed implementation for {@link VcsService}.
 */
@Singleton
public class GitVcsService implements VcsService {
    private static final String BRANCH_UP_TO_DATE_ERROR_MESSAGE = "Everything up-to-date";

    private final GitServiceClient       service;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AppContext             appContext;

    @Inject
    public GitVcsService(final DtoFactory dtoFactory,
                         final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         final GitServiceClient service,
                         final AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.service = service;
        this.appContext = appContext;
    }

    @Override
    public void addRemote(@NotNull final ProjectConfig project, @NotNull final String remote, @NotNull final String remoteUrl,
                          @NotNull final AsyncCallback<Void> callback) {

        service.remoteAdd(appContext.getDevMachine(), project, remote, remoteUrl, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void checkoutBranch(@NotNull final ProjectConfig project, @NotNull final String name,
                               final boolean createNew, @NotNull final AsyncCallback<String> callback) {

        service.checkout(appContext.getDevMachine(),
                         project,
                         dtoFactory.createDto(CheckoutRequest.class)
                                   .withName(name)
                                   .withCreateNew(createNew),
                         new AsyncRequestCallback<String>() {
                             @Override
                             protected void onSuccess(final String branchName) {
                                 callback.onSuccess(branchName);
                             }

                             @Override
                             protected void onFailure(final Throwable exception) {
                                 callback.onFailure(exception);
                             }
                         });
    }

    @Override
    public void commit(@NotNull final ProjectConfig project, final boolean includeUntracked, @NotNull final String commitMessage,
                       @NotNull final AsyncCallback<Void> callback) {
        try {

            service.add(appContext.getDevMachine(), project, !includeUntracked, null, new RequestCallback<Void>() {
                @Override
                protected void onSuccess(Void aVoid) {

                    service.commit(appContext.getDevMachine(), project, commitMessage, true, false, new AsyncRequestCallback<Revision>() {
                        @Override
                        protected void onSuccess(final Revision revision) {
                            callback.onSuccess(null);
                        }

                        @Override
                        protected void onFailure(final Throwable exception) {
                            callback.onFailure(exception);
                        }
                    });
                }

                @Override
                protected void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }
            });

        } catch (final WebSocketException exception) {
            callback.onFailure(exception);
        }
    }

    @Override
    public void deleteRemote(@NotNull final ProjectConfig project, @NotNull final String remote,
                             @NotNull final AsyncCallback<Void> callback) {
        service.remoteDelete(appContext.getDevMachine(), project, remote, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public Promise<String> getBranchName(ProjectConfig project) {
        return service.getStatus(appContext.getDevMachine(), Path.valueOf(project.getPath()))
                      .then(new Function<Status, String>() {
                          @Override
                          public String apply(Status status) throws FunctionException {
                              return status.getBranchName();
                          }
                      });
    }

    @Override
    public void hasUncommittedChanges(@NotNull final ProjectConfig project, @NotNull final AsyncCallback<Boolean> callback) {
        service.getStatus(appContext.getDevMachine(), Path.valueOf(project.getPath()))
               .then(new Operation<Status>() {
                   @Override
                   public void apply(Status status) throws OperationException {
                       callback.onSuccess(!status.isClean());
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError err) throws OperationException {
                       callback.onFailure(err.getCause());
                   }
               });
    }

    @Override
    public void isLocalBranchWithName(@NotNull final ProjectConfig project, @NotNull final String branchName,
                                      @NotNull final AsyncCallback<Boolean> callback) {

        listLocalBranches(project, new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                for (final Branch oneBranch : branches) {
                    if (oneBranch.getDisplayName().equals(branchName)) {
                        callback.onSuccess(true);
                        return;
                    }
                }
                callback.onSuccess(false);
            }
        });
    }

    @Override
    public void listLocalBranches(@NotNull final ProjectConfig project, @NotNull final AsyncCallback<List<Branch>> callback) {
        listBranches(project, null, callback);
    }

    @Override
    public Promise<List<Remote>> listRemotes(ProjectConfig project) {
        return service.remoteList(appContext.getDevMachine(), project, null, false);
    }

    @Override
    public Promise<PushResponse> pushBranch(final ProjectConfig project, final String remote, final String localBranchName) {
        return service.push(appContext.getDevMachine(), project, Collections.singletonList(localBranchName), remote, true)
                      .catchErrorPromise(new Function<PromiseError, Promise<PushResponse>>() {
                          @Override
                          public Promise<PushResponse> apply(PromiseError error) throws FunctionException {
                              if (BRANCH_UP_TO_DATE_ERROR_MESSAGE.equalsIgnoreCase(error.getMessage())) {
                                  return Promises.reject(JsPromiseError.create(new BranchUpToDateException(localBranchName)));
                              } else {
                                  return Promises.reject(error);
                              }
                          }
                      });
    }

    /**
     * List branches of a given type.
     *
     * @param project
     *         the project descriptor.
     * @param listMode
     *         null -> list local branches; "r" -> list remote branches; "a" -> list all branches.
     * @param callback
     *         callback when the operation is done.
     */
    private void listBranches(final ProjectConfig project, final BranchListMode listMode, final AsyncCallback<List<Branch>> callback) {
        final Unmarshallable<List<Branch>> unMarshaller =
                dtoUnmarshallerFactory.newListUnmarshaller(Branch.class);
        service.branchList(appContext.getDevMachine(), project, listMode,
                           new AsyncRequestCallback<List<Branch>>(unMarshaller) {
                               @Override
                               protected void onSuccess(final List<Branch> branches) {
                                   final List<Branch> result = new ArrayList<>();
                                   for (final Branch branch : branches) {
                                       result.add(fromGitBranch(branch));
                                   }
                                   callback.onSuccess(result);
                               }

                               @Override
                               protected void onFailure(final Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    /**
     * Converts a git branch DTO to an abstracted {@link org.eclipse.che.api.git.shared.Branch} object.
     *
     * @param gitBranch
     *         the object to convert.
     * @return the converted object.
     */
    private Branch fromGitBranch(final Branch gitBranch) {
        final Branch branch = GitVcsService.this.dtoFactory.createDto(Branch.class);
        branch.withActive(gitBranch.isActive()).withRemote(gitBranch.isRemote())
              .withName(gitBranch.getName()).withDisplayName(gitBranch.getDisplayName());
        return branch;
    }

    /**
     * Converts a git remote DTO to an abstracted {@link org.eclipse.che.api.git.shared.Remote} object.
     *
     * @param gitRemote
     *         the object to convert.
     * @return the converted object.
     */
    private Remote fromGitRemote(final Remote gitRemote) {
        final Remote remote = GitVcsService.this.dtoFactory.createDto(Remote.class);
        remote.withName(gitRemote.getName()).withUrl(gitRemote.getUrl());
        return remote;
    }
}
