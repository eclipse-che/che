/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.projectimport.wizard;

import static com.google.common.base.Strings.nullToEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.*;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;

/**
 * Json RPC based implementation of the {@link ProjectNotificationSubscriber} which notifies user
 * about output events via popup notification.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
public class ProjectImportOutputJsonRpcNotifier implements ProjectNotificationSubscriber {

  private final NotificationManager notificationManager;
  private final CoreLocalizationConstant locale;
  private final ImportProgressJsonRpcHandler importProgressJsonRpcHandler;

  private StatusNotification singletonNotification;
  private String projectName;

  @Inject
  public ProjectImportOutputJsonRpcNotifier(
      NotificationManager notificationManager,
      CoreLocalizationConstant locale,
      EventBus eventBus,
      ImportProgressJsonRpcHandler importProgressJsonRpcHandler) {
    this.notificationManager = notificationManager;
    this.locale = locale;
    this.importProgressJsonRpcHandler = importProgressJsonRpcHandler;

    eventBus.addHandler(
        WsAgentServerStoppedEvent.TYPE,
        e -> {
          importProgressJsonRpcHandler.unsetConsumer();

          if (singletonNotification != null) {
            singletonNotification.setStatus(FAIL);
            singletonNotification.setContent("");
          }
        });
  }

  @Override
  public void subscribe(String projectName, StatusNotification notification) {
    this.projectName = projectName;
    this.singletonNotification = notification;

    String title = locale.importingProject(projectName);
    importProgressJsonRpcHandler.setConsumer(
        progressRecord -> {
          String content = nullToEmpty(progressRecord.getLine());
          singletonNotification.setTitle(title);
          singletonNotification.setContent(content);
        });
  }

  @Override
  public void subscribe(String projectName) {
    String title = locale.importingProject(projectName);
    singletonNotification = notificationManager.notify(title, PROGRESS, FLOAT_MODE);
    subscribe(projectName, singletonNotification);
  }

  @Override
  public void onSuccess() {
    importProgressJsonRpcHandler.unsetConsumer();

    singletonNotification.setStatus(SUCCESS);
    singletonNotification.setTitle(locale.importProjectMessageSuccess(projectName));
    singletonNotification.setContent("");
  }

  @Override
  public void onFailure(String errorMessage) {
    importProgressJsonRpcHandler.unsetConsumer();

    singletonNotification.setStatus(FAIL);
    singletonNotification.setContent(errorMessage);
  }
}
