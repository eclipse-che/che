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
package org.eclipse.che.ide.notification;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationListener;
import org.eclipse.che.ide.api.notification.ReadState;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.part.PartStackPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link NotificationManagerImpl} functionality
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class NotificationManagerImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Resources resources;

    @Mock
    private NotificationManagerView view;
    @Mock
    private NotificationContainer   notificationContainer;
    @Mock
    private NotificationPopupStack  notificationMessageStack;

    private NotificationManagerImpl manager;

    @Mock
    PartStackPresenter partStack;

    @Before
    public void disarm() {
        manager = new NotificationManagerImpl(view, notificationContainer, notificationMessageStack, resources);
        manager.setPartStack(partStack);
        when(partStack.getActivePart()).thenReturn(manager);
        reset(view);
    }

    @Test
    public void testOnValueChanged() throws Exception {
        Notification notification = new Notification("Title");
        manager.notify(notification);
        reset(view);

        manager.onValueChanged();

        reset(view);
        notification.setState(ReadState.READ);
    }

    @Test
    public void testShowSimpleNotification() throws Exception {
        Notification notification = new Notification("Title");
        manager.notify(notification);

        verify(notificationContainer).addNotification(eq(notification));
        verify(notificationMessageStack, never()).push(any(StatusNotification.class));
    }

    @Test
    public void testShowStatusNotification() throws Exception {
        StatusNotification notification = new StatusNotification("Title", "Message", SUCCESS, FLOAT_MODE, null, null);
        manager.notify(notification);

        verify(notificationContainer).addNotification(eq(notification));
        verify(notificationMessageStack).push(eq(notification));
    }

    @Test
    public void testShowStatusNotificationOnlyInEventsPanel() throws Exception {
        StatusNotification notification = new StatusNotification("Title", "Message", SUCCESS, NOT_EMERGE_MODE, null, null);
        manager.notify(notification);

        verify(notificationContainer).addNotification(eq(notification));
        verify(notificationMessageStack, never()).push(any(StatusNotification.class));
    }

    @Test
    public void testRemoveNotification() throws Exception {
        StatusNotification notification = new StatusNotification("Title", "Message", SUCCESS, NOT_EMERGE_MODE, null, null);
        manager.removeNotification(notification);

        verify(notificationContainer).removeNotification(eq(notification));
    }

    @Test
    public void testOnMessageClicked() throws Exception {
        NotificationListener listener = mock(NotificationListener.class);
        StatusNotification notification = new StatusNotification("Title", "Message", SUCCESS, NOT_EMERGE_MODE, null, listener);

        manager.onClick(notification);
        verify(listener).onClick(eq(notification));
        verify(listener, never()).onClose(eq(notification));
        verify(listener, never()).onDoubleClick(eq(notification));
    }

    @Test
    public void testOnMessageDoubleClicked() throws Exception {
        NotificationListener listener = mock(NotificationListener.class);
        StatusNotification notification = new StatusNotification("Title", "Message", SUCCESS, NOT_EMERGE_MODE, null, listener);

        manager.onDoubleClick(notification);
        verify(listener, never()).onClick(eq(notification));
        verify(listener, never()).onClose(eq(notification));
        verify(listener).onDoubleClick(eq(notification));
    }

    @Test
    public void testOnCloseMessageClicked() throws Exception {
        NotificationListener listener = mock(NotificationListener.class);
        StatusNotification notification = new StatusNotification("Title", "Message", SUCCESS, NOT_EMERGE_MODE, null, listener);

        manager.onClose(notification);
        verify(listener, never()).onClick(eq(notification));
        verify(listener).onClose(eq(notification));
        verify(listener, never()).onDoubleClick(eq(notification));
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        manager.go(container);

        verify(container).setWidget(eq(view));
    }
}
