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
package org.eclipse.che.ide.core;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Handler that receives requests by websocket channel to notify the user about low disk space.
 *
 * @author Vlad Zhukovskyi
 * @since 6.9.0
 */
@Singleton
public class FreeDiskSpaceNotifier {
  private static final String JSON_RPC_METHOD_NAME = "workspace/lowDiskSpace";

  private NotificationManager notificationManager;
  private CoreLocalizationConstant constant;
  private boolean notified = false;

  @Inject
  public FreeDiskSpaceNotifier(
      NotificationManager notificationManager, CoreLocalizationConstant constant) {
    this.notificationManager = notificationManager;
    this.constant = constant;
  }

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(JSON_RPC_METHOD_NAME)
        .noParams()
        .noResult()
        .withConsumer(this::showNotification);
  }

  private void showNotification(String endpointId) {
    if (notified) {
      Log.info(getClass(), "Low disk space. User has already notified.");
      return;
    }

    notificationManager.notify(
        constant.lowDiskSpace(), constant.lowDiskSpaceDescription(), WARNING, EMERGE_MODE);
    notified = true;
  }

  public boolean isNotified() {
    return notified;
  }
}
