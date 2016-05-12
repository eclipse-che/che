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
package org.eclipse.che.ide.ext.git.client.remote.add;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;

/**
 * Presenter for adding remote repository.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 */
@Singleton
public class AddRemoteRepositoryPresenter implements AddRemoteRepositoryView.ActionDelegate {
    private AddRemoteRepositoryView view;
    private GitServiceClient        service;
    private AppContext              appContext;
    private AsyncCallback<Void>     callback;

    /**
     * Create presenter.
     *
     * @param view
     * @param service
     * @param appContext
     */
    @Inject
    public AddRemoteRepositoryPresenter(AddRemoteRepositoryView view, GitServiceClient service, AppContext appContext) {
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
        String name = view.getName();
        String url = view.getUrl();
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();

        service.remoteAdd(appContext.getDevMachine(), project, name, url, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(String result) {
                callback.onSuccess(null);
                view.close();
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
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
