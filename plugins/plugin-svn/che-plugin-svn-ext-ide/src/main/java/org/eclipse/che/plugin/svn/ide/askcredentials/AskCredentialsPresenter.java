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

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.plugin.svn.ide.SubversionClientService;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.askcredentials.AskCredentialsView.AskCredentialsDelegate;

import javax.inject.Inject;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
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
    public AskCredentialsPresenter(AskCredentialsView view,
                                   NotificationManager notificationManager,
                                   SubversionExtensionLocalizationConstants constants,
                                   SubversionClientService clientService) {
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.view = view;
        this.view.setDelegate(this);
        this.clientService = clientService;
    }

    @Override
    public void onSaveClicked() {
        saveCredentials(view.getUsername(), view.getPassword());
        view.clearUsername();
        view.clearPassword();
        view.close();
    }

    @Override
    public void onCancelClicked() {
        view.clearUsername();
        view.clearPassword();
        view.close();
    }

    public void askCredentials(String repositoryUrl) {
        view.clearUsername();
        view.clearPassword();
        view.setRepositoryUrl(repositoryUrl);
        this.repositoryUrl = repositoryUrl;
        view.showDialog();
    }

    private void saveCredentials(String username, String password) {
        final StatusNotification notification =
                new StatusNotification(constants.notificationSavingCredentials(repositoryUrl), PROGRESS, FLOAT_MODE);
        notificationManager.notify(notification);
        clientService.saveCredentials(repositoryUrl, username, password).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                notification.setTitle(constants.notificationCredentialsSaved(repositoryUrl));
                notification.setStatus(SUCCESS);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notification.setTitle(constants.notificationCredentialsFailed(repositoryUrl));
                notification.setStatus(FAIL);
            }
        });
    }
}
