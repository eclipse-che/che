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
package org.eclipse.che.plugin.svn.ide.askcredentials;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.askcredentials.AskCredentialsView.AskCredentialsDelegate;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.inject.Inject;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

public class AskCredentialsPresenter implements AskCredentialsDelegate {

    private final AskCredentialsView                       view;
    private final NotificationManager                      notificationManager;
    private final SubversionExtensionLocalizationConstants constants;
    private final SubversionClientService                  clientService;
    private       String                                   repositoryUrl;

    @Inject
    public AskCredentialsPresenter(final AskCredentialsView view,
                                   final NotificationManager notificationManager,
                                   final SubversionExtensionLocalizationConstants constants,
                                   final SubversionClientService clientService) {
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view = view;
        this.view.setDelegate(this);
        this.clientService = clientService;
    }

    @Override
    public void onSaveClicked() {
        saveCredentials(this.view.getUsername(), this.view.getPassword());
        this.view.clearUsername();
        this.view.clearPassword();
        this.view.close();
    }

    @Override
    public void onCancelClicked() {
        this.view.clearUsername();
        this.view.clearPassword();
        this.view.close();
    }

    public void askCredentials(final String repositoryUrl) {
        this.view.clearUsername();
        this.view.clearPassword();
        this.view.setRepositoryUrl(repositoryUrl);
        this.repositoryUrl = repositoryUrl;
        this.view.showDialog();
    }

    private void saveCredentials(final String username, final String password) {
        final StatusNotification notification =
                new StatusNotification(constants.notificationSavingCredentials(repositoryUrl), PROGRESS, true);
        this.notificationManager.notify(notification);
        this.clientService.saveCredentials(this.repositoryUrl, username, password, new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(final Void notUsed) {
                notification.setTitle(constants.notificationCredentialsSaved(repositoryUrl));
                notification.setStatus(SUCCESS);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                notification.setTitle(constants.notificationCredentialsFailed(repositoryUrl));
                notification.setStatus(FAIL);
            }
        });
    }
}
