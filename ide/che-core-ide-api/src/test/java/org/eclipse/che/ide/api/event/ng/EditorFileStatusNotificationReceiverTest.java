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
package org.eclipse.che.ide.api.event.ng;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.VfsFileStatusUpdateDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EditorFileStatusNotificationReceiver}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class EditorFileStatusNotificationReceiverTest {
    private static final String FILE_PATH = "/folder/file";

    @Mock
    private DtoFactory                           dtoFactory;
    @Mock
    private EventBus                             eventBus;
    @Mock
    private AppContext                           appContext;
    @InjectMocks
    private EditorFileStatusNotificationReceiver receiver;

    @Mock
    private NotificationManager notificationManager;

    @Mock
    private VfsFileStatusUpdateDto dto;
    @Mock
    private JsonRpcRequest         request;
    @Mock
    private Container              container;

    @Before
    public void setUp() throws Exception {
        receiver.inject(notificationManager);

        when(dtoFactory.createDtoFromJson(any(), eq(VfsFileStatusUpdateDto.class))).thenReturn(dto);

        when(dto.getPath()).thenReturn(FILE_PATH);
        when(dto.getType()).thenReturn(MODIFIED);

        when(appContext.getWorkspaceRoot()).thenReturn(container);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldRunDtoFactory() {
        receiver.receive(request);

        verify(dtoFactory).createDtoFromJson(any(), eq(VfsFileStatusUpdateDto.class));
    }

    @Test
    public void shouldNotifyAboutUpdate() {
        when(dto.getType()).thenReturn(MODIFIED);


        receiver.receive(request);

        ArgumentCaptor<FileContentUpdateEvent> captor = ArgumentCaptor.forClass(FileContentUpdateEvent.class);

        verify(eventBus).fireEvent(captor.capture());
        assertEquals(captor.getValue().getFilePath(), FILE_PATH);

        verify(appContext, never()).getWorkspaceRoot();
        verify(container, never()).synchronize(any());
    }

    @Test
    public void shouldNotifyAboutRemove() {
        when(dto.getType()).thenReturn(DELETED);


        receiver.receive(request);

        verify(eventBus, never()).fireEvent(any());

        verify(appContext).getWorkspaceRoot();
        verify(container).synchronize(any());

        verify(notificationManager).notify(eq("External operation"), eq("File '" + "file" + "' is removed"), eq(SUCCESS), eq(EMERGE_MODE));
    }
}
