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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.ImportProgressRecord;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.function.Consumer;

import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProjectImportOutputJsonRpcNotifier}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectImportOutputJsonRpcNotifierTest {

    @Mock
    NotificationManager                  notificationManager;
    @Mock
    ProjectImportOutputJsonRpcSubscriber subscriber;
    @Mock
    CoreLocalizationConstant             constant;
    @Mock
    EventBus                             eventBus;

    private ProjectImportOutputJsonRpcNotifier notifier;

    @Before
    public void setUp() throws Exception {
        notifier = new ProjectImportOutputJsonRpcNotifier(notificationManager, subscriber, constant, eventBus);
    }

    @Test
    public void testShouldSubscribeForDisplayingNotification() throws Exception {
        //given
        final ImportProgressRecord dto = new ImportProgressRecord() {
            @Override
            public int getNum() {
                return 1;
            }

            @Override
            public String getLine() {
                return "message";
            }

            @Override
            public String getProjectName() {
                return "project";
            }
        };

        final ArgumentCaptor<Consumer> argumentCaptor = ArgumentCaptor.forClass(Consumer.class);
        final StatusNotification statusNotification = mock(StatusNotification.class);
        when(notificationManager.notify(anyString(), any(Status.class), any(DisplayMode.class))).thenReturn(statusNotification);
        when(constant.importingProject(anyString())).thenReturn("message");

        //when
        notifier.subscribe("project");

        //then
        verify(constant).importingProject(eq("project"));
        verify(subscriber).subscribeForImportOutputEvents(argumentCaptor.capture());
        argumentCaptor.getValue().accept(dto);
        verify(statusNotification).setTitle(eq("message"));
        verify(statusNotification).setContent(eq(dto.getLine()));
    }

    @Test
    public void testShouldUnSubscribeFromDisplayingNotification() throws Exception {
        //given
        when(constant.importProjectMessageSuccess(anyString())).thenReturn("message");
        final StatusNotification statusNotification = mock(StatusNotification.class);
        when(notificationManager.notify(anyString(), any(Status.class), any(DisplayMode.class))).thenReturn(statusNotification);

        //when
        notifier.subscribe("project");
        notifier.onSuccess();

        //then
        verify(subscriber).unSubscribeForImportOutputEvents();
        verify(statusNotification).setStatus(eq(SUCCESS));
        verify(statusNotification).setTitle(eq("message"));
        verify(statusNotification).setContent(eq(""));
    }

    @Test
    public void testShouldUnSubscribeFromDisplayingNotificationIfExceptionOccurred() throws Exception {

        //given
        final StatusNotification statusNotification = mock(StatusNotification.class);
        when(notificationManager.notify(anyString(), any(Status.class), any(DisplayMode.class))).thenReturn(statusNotification);

        //when
        notifier.subscribe("project");
        notifier.onFailure("message");

        //then
        verify(subscriber).unSubscribeForImportOutputEvents();
        verify(statusNotification).setStatus(eq(FAIL));
        verify(statusNotification).setContent(eq("message"));

    }
}
