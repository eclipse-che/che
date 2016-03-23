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
package org.eclipse.che.ide.extension.machine.client.actions;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/** @author Max Shaposhnik */
@RunWith(MockitoJUnitRunner.class)
public class RunCommandActionTest {
    
    private static final String NAME_PROPERTY = "name";
    
    //constructors mocks
    @Mock
    SelectCommandComboBoxReady          selectCommandAction;
    @Mock
    private CommandManager              commandManager;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private ActionEvent                 event;
    @Mock
    private CommandConfiguration        command;
    
    
    @InjectMocks
    private RunCommandAction action;
    
    
    @Before
    public void setUp() throws Exception {
        when(selectCommandAction.getCommandByName(anyString())).thenReturn(command);
    }
    
    @Test
    public void commandNameShouldBePresent() {
        when(event.getParameters()).thenReturn(Collections.singletonMap("otherParam", "MCI")); 
        action.actionPerformed(event);

        verify(commandManager, never()).execute(any(CommandConfiguration.class));
    }
    
    @Test
    public void actionShouldBePerformed() {
        when(event.getParameters()).thenReturn(Collections.singletonMap(NAME_PROPERTY, "MCI")); 
        action.actionPerformed(event);

        verify(commandManager).execute(eq(command));
    }
}
