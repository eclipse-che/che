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
package org.eclipse.che.ide.ext.debugger.client.actions;

import com.google.common.base.Optional;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class DebugActionTest {

    @Mock
    private DebugConfigurationsManager   debugConfigurationsManager;
    @Mock
    private DebuggerLocalizationConstant localizationConstant;
    @Mock
    private DebuggerResources            javaRuntimeResources;
    @Mock
    private DebuggerManager              debuggerManager;
    @Mock
    private DialogFactory                dialogFactory;

    @InjectMocks
    private DebugAction action;

    @Test
    public void verifyActionConstruction() {
        verify(localizationConstant).debugActionTitle();
        verify(localizationConstant).debugActionDescription();
        verify(javaRuntimeResources).debug();
    }

    @Test
    public void actionShouldBePerformed() {
        DebugConfiguration debugConfiguration = mock(DebugConfiguration.class);
        when(debugConfiguration.getType()).thenReturn(mock(DebugConfigurationType.class));
        when(debugConfiguration.getHost()).thenReturn("localhost");
        when(debugConfiguration.getPort()).thenReturn(8000);
        Map<String, String> connectionProperties = new HashMap<>();
        connectionProperties.put("prop1", "val1");
        connectionProperties.put("prop2", "val2");
        when(debugConfiguration.getConnectionProperties()).thenReturn(connectionProperties);
        Optional configurationOptional = mock(Optional.class);
        when(configurationOptional.isPresent()).thenReturn(Boolean.TRUE);
        when(configurationOptional.get()).thenReturn(debugConfiguration);
        when(debugConfigurationsManager.getCurrentDebugConfiguration()).thenReturn(configurationOptional);

        Debugger debugger = mock(Debugger.class);
        when(debugger.attachDebugger(anyMap())).thenReturn(mock(Promise.class));
        when(debuggerManager.getDebugger(anyString())).thenReturn(debugger);

        action.actionPerformed(null);

        ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(debugger).attachDebugger(mapArgumentCaptor.capture());

        Map actualConnectionProperties = mapArgumentCaptor.getValue();
        assertEquals("localhost", actualConnectionProperties.get("HOST"));
        assertEquals("8000", actualConnectionProperties.get("PORT"));
        assertEquals("val1", actualConnectionProperties.get("prop1"));
        assertEquals("val2", actualConnectionProperties.get("prop2"));
    }
}
