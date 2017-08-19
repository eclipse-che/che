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
package org.eclipse.che.ide.actions;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;

/**
 * Shows notifications about workspace snapshotting progress. Each call to {@link
 * #creationStarted()} must be eventually followed by either call to {@link #creationError(String)}
 * or {@link #successfullyCreated()}.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class WorkspaceSnapshotNotifier {

  private final NotificationManager notificationManager;
  private final CoreLocalizationConstant locale;

  private StatusNotification notification;

  @Inject
  public WorkspaceSnapshotNotifier(
      NotificationManager notificationManager, CoreLocalizationConstant locale) {
    this.notificationManager = notificationManager;
    this.locale = locale;
  }

  /**
   * Starts showing snapshotting notification. The notification is shown until either {@link
   * #creationError(String)} or {@link #successfullyCreated()} is called.
   */
  public void creationStarted() {
    notification =
        notificationManager.notify(locale.createSnapshotProgress(), PROGRESS, FLOAT_MODE);
  }

  /** Changes notification state to finished with an error. */
  public void creationError(String message) {
    if (notification != null) {
      notification.setTitle(locale.createSnapshotFailed());
      notification.setContent(message);
      notification.setStatus(FAIL);
    }
  }

  /** Changes notification state to successfully finished. */
  public void successfullyCreated() {
    if (notification != null) {
      notification.setStatus(SUCCESS);
      notification.setTitle(locale.createSnapshotSuccess());
    }
  }
}
