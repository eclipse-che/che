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
package org.eclipse.che.plugin.jsonexample.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

/**
 * Action for showing a string via the {@link NotificationManager}.
 */
@Singleton
public class HelloWorldAction extends JsonExampleProjectAction {

    private NotificationManager notificationManager;

    /**
     * Constructor.
     *
     * @param appContext
     *         the IDE application context
     * @param notificationManager
     *         the notification manager used to display 'Hello World'
     */
    @Inject
    public HelloWorldAction(AppContext appContext,
                            NotificationManager notificationManager) {
        super(appContext,
              "Say Hello World",
              "Say Hello World Action",
              null);
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
