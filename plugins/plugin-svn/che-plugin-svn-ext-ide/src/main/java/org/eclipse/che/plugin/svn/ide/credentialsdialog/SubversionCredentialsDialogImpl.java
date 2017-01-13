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
package org.eclipse.che.plugin.svn.ide.credentialsdialog;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;

import static org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.createFromAsyncRequest;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * Implementation of {@link SubversionCredentialsDialog}.
 *
 * @author Igor Vinokur
 */
public class SubversionCredentialsDialogImpl implements SubversionCredentialsDialog, SubversionCredentialsDialogViewImpl.ActionDelegate {

    private final SubversionCredentialsDialogView view;

    private AsyncCallback<Credentials> callback;

    @Inject
    public SubversionCredentialsDialogImpl(SubversionCredentialsDialogView view) {
        this.view = view;
        this.view.setDelegate(this);
    }

    @Override
    public Promise<Credentials> askCredentials() {
        view.cleanCredentials();
        view.showDialog();
        return createFromAsyncRequest(new RequestCall<Credentials>() {
            @Override
            public void makeCall(final AsyncCallback<Credentials> callback) {
                SubversionCredentialsDialogImpl.this.callback = callback;
            }
        });
    }

    @Override
    public void onCancelClicked() {
        callback.onFailure(new Exception("Authorization request rejected by user."));
        view.closeDialog();
    }

    @Override
    public void onAuthenticateClicked() {
        callback.onSuccess(new Credentials(view.getUsername(), view.getPassword()));
        view.closeDialog();
    }

    @Override
    public void onCredentialsChanged() {
        view.setEnabledAuthenticateButton(!isNullOrEmpty(view.getUsername()) && !isNullOrEmpty(view.getPassword()));
    }
}
