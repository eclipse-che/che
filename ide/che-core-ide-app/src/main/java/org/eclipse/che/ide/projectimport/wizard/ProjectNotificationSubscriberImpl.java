/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.project.wizard.ProjectNotificationSubscriber;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Subscribes on import project notifications. It can be produced by {@code
 * ImportProjectNotificationSubscriberFactory}
 *
 * @author Anton Korneta
 * @deprecated this class is going to be removed soon
 */
@Deprecated
@Singleton
public class ProjectNotificationSubscriberImpl implements ProjectNotificationSubscriber {

  private final Operation<PromiseError> logErrorHandler;
  private final CoreLocalizationConstant locale;
  private final NotificationManager notificationManager;

  private String wsChannel;
  private String projectName;
  private StatusNotification notification;
  //    private SubscriptionHandler<String> subscriptionHandler;

  @Inject
  public ProjectNotificationSubscriberImpl(
      CoreLocalizationConstant locale, NotificationManager notificationManager) {
    this.locale = locale;
    this.notificationManager = notificationManager;

    this.logErrorHandler =
        new Operation<PromiseError>() {
          @Override
          public void apply(PromiseError error) throws OperationException {
            Log.error(ProjectNotificationSubscriberImpl.class, error);
          }
        };
  }

  @Override
  public void subscribe(final String projectName) {
    notification =
        notificationManager.notify(locale.importingProject(projectName), PROGRESS, FLOAT_MODE);
    subscribe(projectName, notification);
  }

  @Override
  public void subscribe(final String name, final StatusNotification existingNotification) {
    this.projectName = name;
    this.wsChannel = "importProject:output";
    this.notification = existingNotification;

    // TODO (spi ide): should be removed after reworking Dashboard since it depends on this class somehow
    //        this.subscriptionHandler = new SubscriptionHandler<String>(new StringUnmarshallerWS()) {
    //            @Override
    //            protected void onMessageReceived(String result) {
    //                JsonObject jsonObject = Json.parse(result);
    //
    //                if (jsonObject == null) {
    //                    return;
    //                }
    //
    //                if (jsonObject.hasKey("project")) {
    //                    projectName = jsonObject.getString("project");
    //                    notification.setTitle(locale.importingProject(projectName));
    //                }
    //
    //                if (jsonObject.hasKey("line")) {
    //                    notification.setContent(jsonObject.getString("line"));
    //                }
    //            }
    //
    //            @Override
    //            protected void onErrorReceived(final Throwable throwable) {
    //                wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
    //                    @Override
    //                    public void apply(MessageBus messageBus) throws OperationException {
    //                        try {
    //                            messageBus.unsubscribe(wsChannel, subscriptionHandler);
    //                        } catch (WebSocketException e) {
    //                            Log.error(getClass(), e);
    //                        }
    //                        notification.setTitle(locale.importProjectMessageFailure(projectName));
    //                        notification.setContent("");
    //                        notification.setStatus(FAIL);
    //                        Log.error(getClass(), throwable);
    //                    }
    //                }).catchError(logErrorHandler);
    //            }
    //        };

    //        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
    //            @Override
    //            public void apply(final MessageBus messageBus) throws OperationException {
    //                try {
    //                    messageBus.subscribe(wsChannel, subscriptionHandler);
    //                } catch (WebSocketException wsEx) {
    //                    Log.error(ProjectNotificationSubscriberImpl.class, wsEx);
    //                }
    //            }
    //        }).catchError(logErrorHandler);
  }

  @Override
  public void onSuccess() {
    //        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
    //            @Override
    //            public void apply(MessageBus messageBus) throws OperationException {
    //                try {
    //                    messageBus.unsubscribe(wsChannel, subscriptionHandler);
    //                } catch (WebSocketException e) {
    //                    Log.error(getClass(), e);
    //                }
    //                notification.setStatus(SUCCESS);
    //                notification.setTitle(locale.importProjectMessageSuccess(projectName));
    //                notification.setContent("");
    //            }
    //        }).catchError(logErrorHandler);
  }

  @Override
  public void onFailure(final String errorMessage) {
    //        wsAgentStateController.getMessageBus().then(new Operation<MessageBus>() {
    //            @Override
    //            public void apply(MessageBus messageBus) throws OperationException {
    //                try {
    //                    messageBus.unsubscribe(wsChannel, subscriptionHandler);
    //                } catch (WebSocketException e) {
    //                    Log.error(getClass(), e);
    //                }
    //                notification.setStatus(FAIL);
    //                notification.setContent(errorMessage);
    //            }
    //        }).catchError(logErrorHandler);
  }
}
