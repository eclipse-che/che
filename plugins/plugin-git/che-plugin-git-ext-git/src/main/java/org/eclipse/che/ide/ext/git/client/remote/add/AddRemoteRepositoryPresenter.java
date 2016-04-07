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

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;

/**
 * Presenter for adding remote repository.
 *
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 */
@Singleton
public class AddRemoteRepositoryPresenter implements AddRemoteRepositoryView.ActionDelegate {

    // An alternative scp-like syntax: [user@]host.xz:path/to/repo.git/
    private static final RegExp SCP_LIKE_SYNTAX = RegExp.compile("([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+:");
    // the transport protocol
    private static final RegExp PROTOCOL        = RegExp.compile("((http|https|git|ssh|ftp|ftps)://)");
    // the address of the remote server between // and /
    private static final RegExp HOST1           = RegExp.compile("//([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+/");
    // the address of the remote server between @ and : or /
    private static final RegExp HOST2           = RegExp.compile("@([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+[:/]");
    // the repository name
    private static final RegExp REPO_NAME       = RegExp.compile("/[A-Za-z0-9_.\\-]+$");
    // start with white space
    private static final RegExp WHITE_SPACE     = RegExp.compile("^\\s");
    // remote name
    private static final RegExp REMOTE_NAME     = RegExp.compile("^[A-Za-z0-9_\\.]+$");

    private final AddRemoteRepositoryView view;
    private final GitLocalizationConstant locale;
    private final GitServiceClient        service;
    private final AppContext              appContext;

    private AsyncCallback<Void> callback;
    private boolean             isRemoteNameCorrect;
    private boolean             isRemoteURLCorrect;

    @Inject
    public AddRemoteRepositoryPresenter(AddRemoteRepositoryView view,
                                        GitServiceClient service,
                                        GitLocalizationConstant locale,
                                        AppContext appContext) {
        this.view = view;
        this.locale = locale;
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

        service.remoteAdd(appContext.getWorkspaceId(), project, name, url, new AsyncRequestCallback<String>() {
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

    @Override
    public void onRemoteNameChanged() {
        isRemoteNameCorrect = REMOTE_NAME.test(view.getName());
        if (isRemoteNameCorrect) {
            view.markNameValid();
        } else {
            view.markNameInvalid();
        }
        view.setEnableOkButton(isRemoteNameCorrect && isRemoteURLCorrect);
    }

    @Override
    public void onRemoteURLChanged() {
        isRemoteURLCorrect = isRemoteUrlCorrect(view.getUrl());
        view.setEnableOkButton(isRemoteNameCorrect && isRemoteURLCorrect);
    }

    /**
     * Validate url
     *
     * @param url
     *         url for validate
     * @return <code>true</code> if url is correct
     */
    private boolean isRemoteUrlCorrect(@NotNull String url) {
        if (WHITE_SPACE.test(url)) {
            view.markURLInvalid();
            view.setURLErrorMessage(locale.gitUrlStartWithWhiteSpaceMessage());
            return false;
        }

        if (SCP_LIKE_SYNTAX.test(url)) {
            view.markURLValid();
            view.setURLErrorMessage(null);
            return true;
        }

        if (!PROTOCOL.test(url)) {
            view.markURLInvalid();
            view.setURLErrorMessage(locale.gitUrlProtocolIncorrectMessage());
            return false;
        }

        if (!(HOST1.test(url) || HOST2.test(url))) {
            view.markURLInvalid();
            view.setURLErrorMessage(locale.gitUrlHostIncorrectMessage());
            return false;
        }

        if (!(REPO_NAME.test(url))) {
            view.markURLInvalid();
            view.setURLErrorMessage(locale.gitUrlNameRepoIncorrectMessage());
            return false;
        }

        view.markURLValid();
        view.setURLErrorMessage(null);
        return true;
    }
}