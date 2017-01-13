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
package org.eclipse.che.plugin.debugger.ide.configuration;

import junit.framework.TestCase;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathMacro;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Anatoliy Bazko
 */
@RunWith(MockitoJUnitRunner.class)
public class DebugConfigurationsManagerImplTest extends TestCase {

    @Mock
    private LocalStorageProvider           localStorageProvider;
    @Mock
    private DtoFactory                     dtoFactory;
    @Mock
    private DebuggerManager                debuggerManager;
    @Mock
    private DebugConfigurationTypeRegistry debugConfigurationTypeRegistry;
    @Mock
    private DialogFactory                  dialogFactory;
    @Mock
    private DebuggerLocalizationConstant   localizationConstants;
    @Mock
    private DebugConfiguration             debugConfiguration;
    @Mock
    private DebugConfigurationType         debugConfigurationType;
    @Mock
    private CurrentProjectPathMacro        currentProjectPathMacro;
    @Mock
    private Debugger                       debugger;

    @InjectMocks
    private DebugConfigurationsManagerImpl debugConfigurationsManager;

    @Test
    public void testShouldNotApplyConfigurationIfActiveDebuggerExists() throws Exception {
        when(debuggerManager.getActiveDebugger()).thenReturn(mock(Debugger.class));
        when(dialogFactory.createMessageDialog(anyString(), anyString(), (ConfirmCallback)isNull())).thenReturn(mock(MessageDialog.class));

        debugConfigurationsManager.apply(debugConfiguration);

        verify(debuggerManager, never()).setActiveDebugger(any(Debugger.class));
    }

    @Test
    public void testShouldApplyNewConfiguration() throws Exception {
        final String debugId = "debug";
        final String host = "localhost";
        final int port = 9000;

        when(debugConfiguration.getConnectionProperties()).thenReturn(ImmutableMap.of("prop", "value"));
        when(debugConfigurationType.getId()).thenReturn(debugId);
        when(debugConfiguration.getHost()).thenReturn(host);
        when(debugConfiguration.getPort()).thenReturn(port);
        when(debugConfiguration.getType()).thenReturn(debugConfigurationType);
        when(debuggerManager.getDebugger(debugId)).thenReturn(debugger);
        when(debugger.connect(anyMap())).thenReturn(mock(Promise.class));
        when(currentProjectPathMacro.expand()).thenReturn(mock(Promise.class));
        when(currentProjectPathMacro.getName()).thenReturn("key");

        debugConfigurationsManager.apply(debugConfiguration);

        ArgumentCaptor<Operation> operationArgumentCaptor = ArgumentCaptor.forClass(Operation.class);
        verify(currentProjectPathMacro.expand()).then(operationArgumentCaptor.capture());
        operationArgumentCaptor.getValue().apply("project path");

        verify(debuggerManager).setActiveDebugger(debugger);

        ArgumentCaptor<Map> properties = ArgumentCaptor.forClass(Map.class);
        verify(debugger).connect(properties.capture());

        Map<String, String> m = properties.getValue();
        assertEquals(m.get("HOST"), host);
        assertEquals(m.get("PORT"), String.valueOf(port));
        assertEquals(m.get("prop"), "value");
    }
}
