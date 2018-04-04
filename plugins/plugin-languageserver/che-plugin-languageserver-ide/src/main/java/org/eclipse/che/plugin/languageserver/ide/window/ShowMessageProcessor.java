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
package org.eclipse.che.plugin.languageserver.ide.window;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.lsp4j.MessageParams;

/**
 * A processor for incoming <code>window/showMessage</code> notifications sent by a language server.
 *
 * @author xcoulon
 */
@Singleton
public class ShowMessageProcessor {

  private final NotificationManager notificationManager;

  @Inject
  public ShowMessageProcessor(final NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }

  public void processNotification(final MessageParams messageParams) {
    Log.debug(getClass(), "Received a 'ShowMessage' message: " + messageParams.getMessage());
    switch (messageParams.getType()) {
      case Error:
        this.notificationManager.notify(
            messageParams.getMessage(), StatusNotification.Status.FAIL, FLOAT_MODE);
        break;
      case Warning:
        this.notificationManager.notify(
            messageParams.getMessage(), StatusNotification.Status.WARNING, FLOAT_MODE);
        break;
      case Info:
      case Log:
      default:
        this.notificationManager.notify(
            messageParams.getMessage(), StatusNotification.Status.SUCCESS, FLOAT_MODE);
        break;
    }
  }
}
