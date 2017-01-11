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
package org.eclipse.che.plugin.debugger.ide;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.dto.DebugSessionDto;
import org.eclipse.che.api.debug.shared.dto.DebuggerInfoDto;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.MessageBus;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Base test for java debugger extension.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTest {
    public static final String  HOST           = "some.host";
    public static final int     PORT           = 8000;
    public static final String  NAME           = "vm.name";
    public static final String  VERSION        = "vm.version";
    public static final String  DEBUGGER_ID    = "debugger_id";
    public static final String  DEBUGGER_TYPE  = "id";
    public static final boolean DISABLE_BUTTON = false;
    @Mock
    protected DebuggerServiceClient        service;
    @Mock
    protected DebuggerInfoDto              debuggerInfoDto;
    @Mock
    protected DebugSessionDto              debugSessionDto;
    @Mock
    protected DebuggerLocalizationConstant constants;
    @Mock
    protected NotificationManager          notificationManager;
    @Mock
    protected EventBus                     eventBus;
    @Mock
    protected MessageBus                   messageBus;
    @Mock
    protected DtoFactory                   dtoFactory;
    @Mock
    protected DtoUnmarshallerFactory       dtoUnmarshallerFactory;
    @Mock
    protected WorkspaceAgent               workspaceAgent;
    @Mock
    protected PartStack                    partStack;

    @Before
    public void setUp() {
        when(debugSessionDto.getDebuggerInfo()).thenReturn(debuggerInfoDto);
        when(debugSessionDto.getId()).thenReturn(DEBUGGER_ID);
        when(debugSessionDto.getType()).thenReturn(DEBUGGER_TYPE);
        when(debuggerInfoDto.getHost()).thenReturn(HOST);
        when(debuggerInfoDto.getPort()).thenReturn(PORT);
        when(debuggerInfoDto.getName()).thenReturn(NAME);
        when(debuggerInfoDto.getVersion()).thenReturn(VERSION);
    }
}
