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
package org.eclipse.che.plugin.debugger.ide.actions;

import com.google.common.base.Optional;

import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

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

        action.actionPerformed(null);

        verify(debugConfigurationsManager).apply(debugConfiguration);
    }
}
