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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.WsAgentStateController;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.commons.exception.UnmarshallerException;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.SubscriptionHandler;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Subscribes on import project notifications.
 * It can be produced by {@code ImportProjectNotificationSubscriberFactory}
 *
 * @author Anton Korneta
 */
@Singleton
public class ProjectNotificationSubscriberImpl implements ProjectNotificationSubscriber {

    private final Operation<PromiseError>  logErrorHandler;
    private final CoreLocalizationConstant locale;
    private final NotificationManager      notificationManager;
    private final String                   workspaceId;
    private       WsAgentStateController   wsAgentStateController;
    private       Promise<MessageBus>      messageBusPromise;

    private String                      wsChannel;
    private String                      projectName;
    private StatusNotification          notification;
    private SubscriptionHandler<String> subscriptionHandler;

    @Inject
    public ProjectNotificationSubscriberImpl(CoreLocalizationConstant locale,
                                             AppContext appContext,
                                             NotificationManager notificationManager,
                                             final WsAgentStateController wsAgentStateController,
                                             EventBus eventBus) {
        this.locale = locale;
        this.notificationManager = notificationManager;
        this.wsAgentStateController = wsAgentStateController;
        this.workspaceId = appContext.getWorkspace().getId();


        this.logErrorHandler = new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.error(ProjectNotificationSubscriberImpl.class, error);
            }
        };

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(final WsAgentStateEvent event) {
                messageBusPromise = wsAgentStateController.getMessageBus();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {

            }
        });
    }

    @Override
    public void subscribe(final String projectName) {
        notification = notificationManager.notify(locale.importingProject(), PROGRESS, true);
        subscribe(projectName, notification);
    }

    @Override
    public void subscribe(final String projectName, final StatusNotification existingNotification) {
        if (messageBusPromise == null) {
            messageBusPromise = wsAgentStateController.getMessageBus();
        }
        this.projectName = projectName;
        this.wsChannel = "importProject:output:" + workspaceId + ":" + projectName;
        this.notification = existingNotification;
        this.subscriptionHandler = new SubscriptionHandler<String>(new LineUnmarshaller()) {
            @Override
            protected void onMessageReceived(String result) {
                notification.setContent(result);
            }

            @Override
            protected void onErrorReceived(final Throwable throwable) {
                messageBusPromise.then(new Operation<MessageBus>() {
                    @Override
                    public void apply(MessageBus messageBus) throws OperationException {
                        try {
                            messageBus.unsubscribe(wsChannel, subscriptionHandler);
                        } catch (WebSocketException e) {
                            Log.error(getClass(), e);
                        }
                        notification.setTitle(locale.importProjectMessageFailure(projectName));
                        notification.setContent("");
                        notification.setStatus(FAIL);
                        Log.error(getClass(), throwable);
                    }
                }).catchError(logErrorHandler);
            }
        };

        messageBusPromise.then(new Operation<MessageBus>() {
            @Override
            public void apply(final MessageBus messageBus) throws OperationException {
                try {
                    messageBus.subscribe(wsChannel, subscriptionHandler);
                } catch (WebSocketException wsEx) {
                    Log.error(ProjectNotificationSubscriberImpl.class, wsEx);
                }
            }
        }).catchError(logErrorHandler);
    }

    @Override
    public void onSuccess() {
        messageBusPromise.then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus messageBus) throws OperationException {
                try {
                    messageBus.unsubscribe(wsChannel, subscriptionHandler);
                } catch (WebSocketException e) {
                    Log.error(getClass(), e);
                }
                notification.setStatus(SUCCESS);
                notification.setTitle(locale.importProjectMessageSuccess(projectName));
                notification.setContent("");
            }
        }).catchError(logErrorHandler);
    }

    @Override
    public void onFailure(final String errorMessage) {
        messageBusPromise.then(new Operation<MessageBus>() {
            @Override
            public void apply(MessageBus messageBus) throws OperationException {
                try {
                    messageBus.unsubscribe(wsChannel, subscriptionHandler);
                } catch (WebSocketException e) {
                    Log.error(getClass(), e);
                }
                notification.setStatus(FAIL);
                notification.setContent(errorMessage);
            }
        }).catchError(logErrorHandler);
    }

    static class LineUnmarshaller implements org.eclipse.che.ide.websocket.rest.Unmarshallable<String> {
        private String line;

        @Override
        public void unmarshal(Message response) throws UnmarshallerException {
            JSONObject jsonObject = JSONParser.parseStrict(response.getBody()).isObject();
            if (jsonObject == null) {
                return;
            }
            if (jsonObject.containsKey("line")) {
                line = jsonObject.get("line").isString().stringValue();
            }
        }

        @Override
        public String getPayload() {
            return line;
        }
    }

}
