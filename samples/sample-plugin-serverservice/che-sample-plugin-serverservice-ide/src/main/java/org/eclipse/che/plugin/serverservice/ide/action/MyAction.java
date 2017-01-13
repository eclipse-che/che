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
package org.eclipse.che.plugin.serverservice.ide.action;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.plugin.serverservice.ide.MyServiceClient;

/**
 * Actions that triggers the sample server service call.
 *
 * @author Edgar Mueller
 */
public class MyAction extends Action {

    private final NotificationManager notificationManager;
    private final MyServiceClient     serviceClient;

    /**
     * Constructor.
     *
     * @param notificationManager the notification manager
     * @param serviceClient the client that is used to create requests
     */
    @Inject
    public MyAction(final NotificationManager notificationManager,
                    final MyServiceClient serviceClient) {
        super("My Action", "My Action Description");
        this.notificationManager = notificationManager;
        this.serviceClient = serviceClient;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // This calls the service in the workspace.
        // This method is in our org.eclipse.che.plugin.serverservice.ide.MyServiceClient class
        // This is a Promise, so the .then() method is invoked after the response is made
        serviceClient.getHello("CheTheAllPowerful!").then(new Operation<String>() {
            @Override
            public void apply(String response) throws OperationException {
                // This passes the response String to the notification manager.
                notificationManager.notify(response, StatusNotification.Status.SUCCESS, StatusNotification.DisplayMode.FLOAT_MODE);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                notificationManager.notify("Fail", StatusNotification.Status.FAIL, StatusNotification.DisplayMode.FLOAT_MODE);
            }
        });
    }
}
