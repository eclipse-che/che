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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.BRANCH;
import static org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto.Type.REVISION;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GitCheckoutStatusNotificationReceiver}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class GitCheckoutStatusNotificationReceiverTest {
    private static final String NAME = "name";

    @Mock
    private DtoFactory                            dtoFactory;
    @InjectMocks
    private GitCheckoutStatusNotificationReceiver receiver;

    @Mock
    private NotificationManager notificationManager;

    @Mock
    private GitCheckoutEventDto dto;
    @Mock
    private JsonRpcRequest      request;

    @Before
    public void setUp() throws Exception {
        receiver.inject(notificationManager);

        when(dtoFactory.createDtoFromJson(any(), eq(GitCheckoutEventDto.class))).thenReturn(dto);

        when(dto.getName()).thenReturn(NAME);
        when(dto.getType()).thenReturn(BRANCH);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldRunDtoFactory() {
        receiver.receive(request);

        verify(dtoFactory).createDtoFromJson(any(), eq(GitCheckoutEventDto.class));
    }

    @Test
    public void shouldNotifyAboutBranchCheckout() {
        when(dto.getType()).thenReturn(BRANCH);


        receiver.receive(request);

        verify(notificationManager)
                .notify(eq("External operation"), eq("Branch '" + NAME + "' is checked out"), eq(SUCCESS), eq(EMERGE_MODE));
    }

    @Test
    public void shouldNotifyAboutRevisionCheckout() {
        when(dto.getType()).thenReturn(REVISION);

        receiver.receive(request);

        verify(notificationManager)
                .notify(eq("External operation"), eq("Revision '" + NAME + "' is checked out"), eq(SUCCESS), eq(EMERGE_MODE));
    }
}
