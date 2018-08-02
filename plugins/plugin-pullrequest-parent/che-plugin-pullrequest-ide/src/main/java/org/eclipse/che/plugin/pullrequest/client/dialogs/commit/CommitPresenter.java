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
package org.eclipse.che.plugin.pullrequest.client.dialogs.commit;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.git.client.GitUtil.isUnderGit;
import static org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CANCEL;
import static org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.CONTINUE;
import static org.eclipse.che.plugin.pullrequest.client.dialogs.commit.CommitPresenter.CommitActionHandler.CommitAction.OK;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.plugin.pullrequest.client.vcs.VcsServiceProvider;

/**
 * This presenter provides base functionality to commit project changes or not before cloning or
 * generating a factory url.
 *
 * @author Kevin Pollet
 */
public class CommitPresenter implements CommitView.ActionDelegate {

  private final CommitView view;
  private final AppContext appContext;
  private final VcsServiceProvider vcsServiceProvider;
  private final NotificationManager notificationManager;
  private CommitActionHandler handler;

  @Inject
  public CommitPresenter(
      @NotNull final CommitView view,
      @NotNull final AppContext appContext,
      @NotNull final VcsServiceProvider vcsServiceProvider,
      @NotNull final NotificationManager notificationManager) {
    this.view = view;
    this.appContext = appContext;
    this.vcsServiceProvider = vcsServiceProvider;
    this.notificationManager = notificationManager;

    this.view.setDelegate(this);
  }

  /**
   * Opens the {@link CommitView}.
   *
   * @param commitDescription the default commit description.
   */
  public void showView(@NotNull String commitDescription) {
    view.show(commitDescription);
  }

  /**
   * Sets the {@link CommitPresenter.CommitActionHandler} called after the ok or continue action is
   * executed.
   *
   * @param handler the handler to set.
   */
  public void setCommitActionHandler(final CommitActionHandler handler) {
    this.handler = handler;
  }

  /** Returns if the current project has uncommitted changes. */
  public void hasUncommittedChanges(final AsyncCallback<Boolean> callback) {
    final Project project = appContext.getRootProject();
    if (project == null) {
      callback.onFailure(new IllegalStateException("No project opened"));

    } else if (!isUnderGit(project)) {
      callback.onFailure(new IllegalStateException("Opened project is not has no Git repository"));

    } else {
      vcsServiceProvider.getVcsService(project).hasUncommittedChanges(project, callback);
    }
  }

  @Override
  public void onOk() {
    final Project project = appContext.getRootProject();
    if (project != null) {
      vcsServiceProvider
          .getVcsService(project)
          .commit(
              project,
              view.isIncludeUntracked(),
              view.getCommitDescription(),
              new AsyncCallback<Void>() {
                @Override
                public void onFailure(final Throwable exception) {
                  notificationManager.notify(exception.getMessage(), FAIL, FLOAT_MODE);
                }

                @Override
                public void onSuccess(final Void result) {
                  view.close();

                  if (handler != null) {
                    handler.onCommitAction(OK);
                  }
                }
              });
    }
  }

  @Override
  public void onContinue() {
    view.close();

    if (handler != null) {
      handler.onCommitAction(CONTINUE);
    }
  }

  @Override
  public void onCancel() {
    view.close();

    if (handler != null) {
      handler.onCommitAction(CANCEL);
    }
  }

  @Override
  public void onCommitDescriptionChanged() {
    view.setOkButtonEnabled(!view.getCommitDescription().isEmpty());
  }

  public interface CommitActionHandler {
    /**
     * Called when a commit actions is done on the commit view.
     *
     * @param action the action.
     */
    void onCommitAction(CommitAction action);

    enum CommitAction {
      OK,
      CONTINUE,
      CANCEL
    }
  }
}
