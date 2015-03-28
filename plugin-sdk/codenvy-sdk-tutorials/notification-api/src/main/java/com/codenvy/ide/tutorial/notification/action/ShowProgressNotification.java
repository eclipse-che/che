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
package com.codenvy.ide.tutorial.notification.action;

import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import static org.eclipse.che.ide.api.notification.Notification.Status.FINISHED;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

/**
 * The action for showing PROGRESS notification.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
public class ShowProgressNotification extends Action
        implements Notification.OpenNotificationHandler, Notification.CloseNotificationHandler {
    private NotificationManager notificationManager;
    private Notification        notification;
    private Timer timer = new Timer() {
        @Override
        public void run() {
            boolean isSuccessful = Window.confirm("Close notification as successful? Otherwise it will be failed.");
            if (isSuccessful) {
                notification.setStatus(FINISHED);
                notification.setMessage("I've finished progress...");
            } else {
                notification.setStatus(FINISHED);
                notification.setType(ERROR);
                notification.setMessage("Some error is happened...");
            }
            notification = null;
        }
    };

    @Inject
    public ShowProgressNotification(NotificationManager notificationManager) {
        super("Show progress notification", "This action shows progress notification");
        this.notificationManager = notificationManager;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (notification == null) {
            notification = new Notification("I'm doing something...", PROGRESS, this, this);
            notificationManager.showNotification(notification);
            timer.schedule(10000);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        timer.cancel();
        notification.setStatus(FINISHED);
        notification.setMessage("The process was stopped...");
        notification = null;
    }

    /** {@inheritDoc} */
    @Override
    public void onOpenClicked() {
        Window.alert("You've opened notification!");
    }
}