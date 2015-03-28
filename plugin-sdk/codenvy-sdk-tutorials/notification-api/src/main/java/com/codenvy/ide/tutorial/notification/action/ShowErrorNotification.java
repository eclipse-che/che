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
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

/**
 * The action for showing ERROR notification.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@Singleton
public class ShowErrorNotification extends Action {
    private NotificationManager notificationManager;

    @Inject
    public ShowErrorNotification(NotificationManager notificationManager) {
        super("Show ERROR notification", "This action shows ERROR notification");
        this.notificationManager = notificationManager;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        Notification notification = new Notification("This is a error notification...", ERROR);
        notificationManager.showNotification(notification);
    }
}