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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Tests for {@link WorkspaceSnapshotNotifier}.
 *
 * @author Yevhenii Voevodin
 */
@RunWith(GwtMockitoTestRunner.class)
public class WorkspaceSnapshotNotifierTest {

  @Mock private NotificationManager notificationManager;

  @Mock private StatusNotification notification;

  @Mock private Promise<Void> createSnapshotPromise;

  @Mock private PromiseError promiseError;

  @Mock private CoreLocalizationConstant locale;

  @Mock private StatusNotification statusNotification;

  @Captor private ArgumentCaptor<Operation<PromiseError>> errorCaptor;

  @InjectMocks private WorkspaceSnapshotNotifier snapshotCreator;

  @Before
  public void setup() throws Exception {
    when(notificationManager.notify(
            nullable(String.class),
            nullable(StatusNotification.Status.class),
            nullable(DisplayMode.class)))
        .thenReturn(notification);
    when(notification.getStatus()).thenReturn(StatusNotification.Status.PROGRESS);
  }

  @Test
  public void shouldShowNotificationWhenCreatingSnapshot() {
    snapshotCreator.creationStarted();

    verify(notificationManager)
        .notify(nullable(String.class), eq(StatusNotification.Status.PROGRESS), eq(FLOAT_MODE));
  }

  @Test
  public void shouldChangeNotificationAfterCreationError() {
    when(locale.createSnapshotFailed()).thenReturn("Error");

    snapshotCreator.creationStarted();

    snapshotCreator.creationError("Error");

    verify(notification).setTitle("Error");
    verify(notification).setStatus(StatusNotification.Status.FAIL);
  }

  @Test
  public void shouldChangeNotificationAfterSuccessfullyCreated() {
    when(locale.createSnapshotSuccess()).thenReturn("Snapshot successfully created");
    snapshotCreator.creationStarted();

    snapshotCreator.successfullyCreated();

    verify(notification).setStatus(StatusNotification.Status.SUCCESS);
    verify(notification).setTitle("Snapshot successfully created");
  }
}
