/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.client.update;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.selection.SelectionManager;
import org.eclipse.che.ide.ext.tutorials.client.TutorialsLocalizationConstant;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.inject.Inject;

import static org.eclipse.che.ide.api.notification.Notification.Status.FINISHED;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

/**
 * @author Vitaly Parfonov
 * @author Valeriy Svydenko
 */
public class ExtensionUpdater implements Notification.OpenNotificationHandler {

    private UpdateServiceClient           updateServiceClient;
    private NotificationManager           notificationManager;
    private AppContext                    appContext;
    private TutorialsLocalizationConstant localizationConstant;
    private SelectionManager              selectionManager;

    @Inject
    public ExtensionUpdater(UpdateServiceClient updateServiceClient,
                            NotificationManager notificationManager,
                            AppContext appContext,
                            SelectionManager selectionManager,
                            TutorialsLocalizationConstant localizationConstant) {
        this.updateServiceClient = updateServiceClient;
        this.notificationManager = notificationManager;
        this.selectionManager = selectionManager;
        this.appContext = appContext;
        this.localizationConstant = localizationConstant;
    }

    /** Updates launched Codenvy Extension. */
    public void updateExtension() {
        final CurrentProject currentProject = appContext.getCurrentProject();
        Runner selectionRunner = selectionManager.getRunner();
        if (selectionRunner == null) {
            return;
        }
        final Notification notification =
                new Notification(localizationConstant.applicationUpdating(currentProject.getProjectDescription().getName()), PROGRESS,
                                 ExtensionUpdater.this);
        notificationManager.showNotification(notification);
        try {
            updateServiceClient.update(selectionRunner, new RequestCallback<Void>() {
                @Override
                protected void onSuccess(Void result) {
                    notification.setStatus(FINISHED);
                    notification.setMessage(localizationConstant.applicationUpdated(currentProject.getProjectDescription().getName()));
                }

                @Override
                protected void onFailure(Throwable exception) {
                    notification.setStatus(FINISHED);
                    notification.setType(ERROR);

                    String message;
                    if (exception != null && exception.getMessage() != null) {
                        message = exception.getMessage();
                    } else {
                        message = localizationConstant.updateApplicationFailed(currentProject.getProjectDescription().getName());
                    }
                    notification.setMessage(message);
                }
            });
        } catch (WebSocketException e) {
            notification.setStatus(FINISHED);
            notification.setType(ERROR);
            notification.setMessage(localizationConstant.updateApplicationFailed(currentProject.getProjectDescription().getName()));
        }
    }

    @Override
    public void onOpenClicked() {
    }
}