/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.remote.add;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;

/**
 * Presenter for adding remote repository.
 *
 * @author Ann Zhuleva
 * @author Vlad Zhukovskyi
 */
@Singleton
public class AddRemoteRepositoryPresenter implements AddRemoteRepositoryView.ActionDelegate {
  private AddRemoteRepositoryView view;
  private GitServiceClient service;
  private AppContext appContext;
  private AsyncCallback<Void> callback;

  /**
   * Create presenter.
   *
   * @param view
   * @param service
   * @param appContext
   */
  @Inject
  public AddRemoteRepositoryPresenter(
      AddRemoteRepositoryView view, GitServiceClient service, AppContext appContext) {
    this.view = view;
    this.view.setDelegate(this);
    this.service = service;
    this.appContext = appContext;
  }

  /** Show dialog. */
  public void showDialog(@NotNull AsyncCallback<Void> callback) {
    this.callback = callback;
    view.setUrl("");
    view.setName("");
    view.setEnableOkButton(false);
    view.showDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void onOkClicked() {
    final String name = view.getName();
    final String url = view.getUrl().trim();
    final Project project = appContext.getRootProject();

    service
        .remoteAdd(project.getLocation(), name, url)
        .then(
            arg -> {
              callback.onSuccess(null);
              view.close();
            })
        .catchError(
            error -> {
              callback.onFailure(error.getCause());
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelClicked() {
    view.close();
  }

  /** {@inheritDoc} */
  @Override
  public void onValueChanged() {
    String name = view.getName();
    String url = view.getUrl();
    boolean isEnabled = !name.isEmpty() && !url.isEmpty();
    view.setEnableOkButton(isEnabled);
  }
}
