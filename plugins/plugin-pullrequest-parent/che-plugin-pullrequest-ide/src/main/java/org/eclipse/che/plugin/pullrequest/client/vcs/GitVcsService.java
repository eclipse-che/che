/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.pullrequest.client.vcs;

import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.ErrorCodes.UNAUTHORIZED_GIT_OPERATION;
import static org.eclipse.che.api.git.shared.ProviderInfo.PROVIDER_NAME;
import static org.eclipse.che.ide.util.ExceptionUtils.getAttributes;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.BranchListMode;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.auth.Credentials;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.StringUtils;

/** Git backed implementation for {@link VcsService}. */
@Singleton
public class GitVcsService implements VcsService {
  private static final String BRANCH_UP_TO_DATE_ERROR_MESSAGE = "Everything up-to-date";

  private final GitServiceClient service;
  private final DtoFactory dtoFactory;
  private final AppContext appContext;
  private final OAuthServiceClient oAuthServiceClient;

  @Inject
  public GitVcsService(
      final DtoFactory dtoFactory,
      final DtoUnmarshallerFactory dtoUnmarshallerFactory,
      final GitServiceClient service,
      final AppContext appContext,
      final OAuthServiceClient oAuthServiceClient) {
    this.dtoFactory = dtoFactory;
    this.service = service;
    this.appContext = appContext;
    this.oAuthServiceClient = oAuthServiceClient;
  }

  @Override
  public void addRemote(
      @NotNull final ProjectConfig project,
      @NotNull final String remote,
      @NotNull final String remoteUrl,
      @NotNull final AsyncCallback<Void> callback) {
    service
        .remoteAdd(appContext.getRootProject().getLocation(), remote, remoteUrl)
        .then(
            arg -> {
              callback.onSuccess(null);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  @Override
  public void checkoutBranch(
      @NotNull final ProjectConfig project,
      @NotNull final String name,
      final boolean createNew,
      @NotNull final AsyncCallback<String> callback) {

    service
        .checkout(
            appContext.getRootProject().getLocation(),
            dtoFactory.createDto(CheckoutRequest.class).withName(name).withCreateNew(createNew))
        .then(callback::onSuccess)
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  @Override
  public void commit(
      @NotNull final ProjectConfig project,
      final boolean includeUntracked,
      @NotNull final String commitMessage,
      @NotNull final AsyncCallback<Void> callback) {
    service
        .add(appContext.getRootProject().getLocation(), !includeUntracked, null)
        .then(
            arg -> {
              service
                  .commit(appContext.getRootProject().getLocation(), commitMessage, true, false)
                  .then(
                      revision -> {
                        callback.onSuccess(null);
                      })
                  .catchError(
                      error -> {
                        callback.onFailure(error.getCause());
                      });
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  @Override
  public void deleteRemote(
      @NotNull final ProjectConfig project,
      @NotNull final String remote,
      @NotNull final AsyncCallback<Void> callback) {
    service
        .remoteDelete(appContext.getRootProject().getLocation(), remote)
        .then(
            arg -> {
              callback.onSuccess(null);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  @Override
  public Promise<String> getBranchName(ProjectConfig project) {
    return service
        .getStatus(Path.valueOf(project.getPath()), emptyList())
        .then((Function<Status, String>) status -> status.getRefName());
  }

  @Override
  public void hasUncommittedChanges(
      @NotNull final ProjectConfig project, @NotNull final AsyncCallback<Boolean> callback) {
    service
        .getStatus(Path.valueOf(project.getPath()), emptyList())
        .then(
            status -> {
              callback.onSuccess(!status.isClean());
            })
        .catchError(
            err -> {
              callback.onFailure(err.getCause());
            });
  }

  @Override
  public void isLocalBranchWithName(
      @NotNull final ProjectConfig project,
      @NotNull final String branchName,
      @NotNull final AsyncCallback<Boolean> callback) {

    listLocalBranches(
        project,
        new AsyncCallback<List<Branch>>() {
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
  public void listLocalBranches(
      @NotNull final ProjectConfig project, @NotNull final AsyncCallback<List<Branch>> callback) {
    listBranches(project, null, callback);
  }

  @Override
  public Promise<List<Remote>> listRemotes(ProjectConfig project) {
    return service.remoteList(appContext.getRootProject().getLocation(), null, false);
  }

  @Override
  public Promise<PushResponse> pushBranch(
      final ProjectConfig project, final String remote, final String localBranchName) {
    return service
        .push(
            appContext.getRootProject().getLocation(),
            Collections.singletonList(localBranchName),
            remote,
            true,
            null)
        .catchErrorPromise(
            error -> {
              if (getErrorCode(error.getCause()) != UNAUTHORIZED_GIT_OPERATION) {
                return null;
              }
              Map<String, String> attributes = getAttributes(error.getCause());
              String providerName = attributes.get(PROVIDER_NAME);
              if (!StringUtils.isNullOrEmpty(providerName)) {
                return pushBranchAuthenticated(remote, localBranchName, providerName);
              } else if (BRANCH_UP_TO_DATE_ERROR_MESSAGE.equalsIgnoreCase(error.getMessage())) {
                return Promises.reject(
                    JsPromiseError.create(new BranchUpToDateException(localBranchName)));
              } else {
                return Promises.reject(error);
              }
            });
  }

  public Promise<PushResponse> pushBranchAuthenticated(
      final String remote, final String localBranchName, final String providerName) {
    return oAuthServiceClient
        .getToken(providerName)
        .thenPromise(
            token ->
                service.push(
                    appContext.getRootProject().getLocation(),
                    Collections.singletonList(localBranchName),
                    remote,
                    true,
                    new Credentials(token.getToken(), token.getToken())))
        .catchErrorPromise(
            error -> {
              if (BRANCH_UP_TO_DATE_ERROR_MESSAGE.equalsIgnoreCase(error.getMessage())) {
                return Promises.reject(
                    JsPromiseError.create(new BranchUpToDateException(localBranchName)));
              } else {
                return Promises.reject(error);
              }
            });
  }

  /**
   * List branches of a given type.
   *
   * @param project the project descriptor.
   * @param listMode null -> list local branches; "r" -> list remote branches; "a" -> list all
   *     branches.
   * @param callback callback when the operation is done.
   */
  private void listBranches(
      final ProjectConfig project,
      final BranchListMode listMode,
      final AsyncCallback<List<Branch>> callback) {
    service
        .branchList(Path.valueOf(project.getPath()), listMode)
        .then(
            branches -> {
              final List<Branch> result =
                  branches.stream().map(this::fromGitBranch).collect(Collectors.toList());
              callback.onSuccess(result);
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  /**
   * Converts a git branch DTO to an abstracted {@link org.eclipse.che.api.git.shared.Branch}
   * object.
   *
   * @param gitBranch the object to convert.
   * @return the converted object.
   */
  private Branch fromGitBranch(final Branch gitBranch) {
    final Branch branch = GitVcsService.this.dtoFactory.createDto(Branch.class);
    branch
        .withActive(gitBranch.isActive())
        .withRemote(gitBranch.isRemote())
        .withName(gitBranch.getName())
        .withDisplayName(gitBranch.getDisplayName());
    return branch;
  }

  /**
   * Converts a git remote DTO to an abstracted {@link org.eclipse.che.api.git.shared.Remote}
   * object.
   *
   * @param gitRemote the object to convert.
   * @return the converted object.
   */
  private Remote fromGitRemote(final Remote gitRemote) {
    final Remote remote = GitVcsService.this.dtoFactory.createDto(Remote.class);
    remote.withName(gitRemote.getName()).withUrl(gitRemote.getUrl());
    return remote;
  }
}
