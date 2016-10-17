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
package org.eclipse.che.plugin.sampleactions.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

/**
 * Action for showing a string via the {@link NotificationManager}.
 */
@Singleton
public class HelloWorldAction extends Action {

    private NotificationManager notificationManager;

    @Inject
    public HelloWorldAction(NotificationManager notificationManager) {
        super("Say Hello World", "Say Hello World Action");
        this.notificationManager = notificationManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.notificationManager.notify(
                "Hello World",
                StatusNotification.Status.SUCCESS,
                StatusNotification.DisplayMode.FLOAT_MODE);
    }
}
