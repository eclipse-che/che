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
package org.eclipse.che.ide.command.toolbar.commands.button;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tests for {@link MachineItem}. */
@RunWith(MockitoJUnitRunner.class)
public class MachineItemTest {

    private static final String MACHINE_NAME = "dev-machine";

    @Mock
    private CommandImpl command;
    @Mock
    private Machine     machine;

    private MachineItem item;

    @Before
    public void setUp() throws Exception {
        MachineConfig machineConfig = mock(MachineConfig.class);
        when(machineConfig.getName()).thenReturn(MACHINE_NAME);
        when(machine.getConfig()).thenReturn(machineConfig);

        item = new MachineItem(command, machine);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(MACHINE_NAME, item.getName());
    }

    @Test
    public void testGetMachine() throws Exception {
        assertEquals(machine, item.getMachine());
    }
}
