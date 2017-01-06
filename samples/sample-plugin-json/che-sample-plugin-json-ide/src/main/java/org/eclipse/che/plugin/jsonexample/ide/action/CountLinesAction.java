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


import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.plugin.jsonexample.ide.JsonExampleResources;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Action for counting lines of code of all JSON files within the current project.
 * Line counting is implemented by consuming a RESTful service.
 */
@Singleton
public class CountLinesAction extends JsonExampleProjectAction {

    private final AppContext            appContext;
    private final StringMapUnmarshaller unmarshaller;
    private final AsyncRequestFactory   asyncRequestFactory;
    private final NotificationManager   notificationManager;

    /**
     * Constructor
     *
     * @param appContext
     *         the IDE application context
     * @param resources
     *         the JSON Example resources that contain the action icon
     * @param asyncRequestFactory
     *         asynchronous request factory for creating the server request
     * @param notificationManager
     *         the notification manager used to display the lines of code per file
     */
    @Inject
    public CountLinesAction(AppContext appContext,
                            JsonExampleResources resources,
                            AsyncRequestFactory asyncRequestFactory,
                            NotificationManager notificationManager) {

        super(appContext,
              "Count JSON Lines of Code",
              "Counts lines of code for all JSON Files in the project",
              resources.icon());

        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.notificationManager = notificationManager;
        this.unmarshaller = new StringMapUnmarshaller();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/json-example/" +
                     appContext.getWorkspaceId() +
                     appContext.getRootProject().getLocation();

        asyncRequestFactory.createGetRequest(url, false).send(
                new AsyncRequestCallback<Map<String, String>>(unmarshaller) {

                    @Override
                    protected void onSuccess(Map<String, String> linesPerFile) {
                        for (Map.Entry<String, String> entry : linesPerFile.entrySet()) {
                            String fileName = entry.getKey();
                            String loc = entry.getValue();
                            notificationManager.notify(
                                    "File " + fileName + " has " + loc + " lines.",
                                    StatusNotification.Status.SUCCESS,
                                    StatusNotification.DisplayMode.FLOAT_MODE);
                        }
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.notify(
                                exception.getMessage(),
                                StatusNotification.Status.FAIL,
                                StatusNotification.DisplayMode.FLOAT_MODE);
                    }
                });
    }
}
