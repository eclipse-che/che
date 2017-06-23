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
package org.eclipse.che.ide.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;

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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WorkspaceSnapshotNotifier}.
 *
 * @author Yevhenii Voevodin
 */
@RunWith(GwtMockitoTestRunner.class)
public class WorkspaceSnapshotNotifierTest {

    @Mock
    private NotificationManager           notificationManager;
    @Mock
    private Provider<NotificationManager> notificationManagerProvider;

    @Mock
    private StatusNotification notification;

    @Mock
    private Promise<Void> createSnapshotPromise;

    @Mock
    private PromiseError promiseError;

    @Mock
    private CoreLocalizationConstant locale;

    @Mock
    private StatusNotification statusNotification;

    @Captor
    private ArgumentCaptor<Operation<PromiseError>> errorCaptor;

    @InjectMocks
    private WorkspaceSnapshotNotifier snapshotCreator;

    @Before
    public void setup () throws Exception {
        when(notificationManagerProvider.get()).thenReturn(notificationManager);
        when(notificationManager.notify(anyString(), any(StatusNotification.Status.class), (DisplayMode)anyObject()))
                .thenReturn(notification);
        when(notification.getStatus()).thenReturn(StatusNotification.Status.PROGRESS);
    }

    @Test
    public void shouldShowNotificationWhenCreatingSnapshot() {
        snapshotCreator.creationStarted();

        verify(notificationManager).notify(anyString(), eq(StatusNotification.Status.PROGRESS), eq(FLOAT_MODE));
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
