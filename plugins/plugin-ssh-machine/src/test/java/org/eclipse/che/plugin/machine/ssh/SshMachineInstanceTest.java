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
package org.eclipse.che.plugin.machine.ssh;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineRuntimeInfo;
import org.eclipse.che.api.core.util.LineConsumer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for{@link SshMachineInstance}
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class SshMachineInstanceTest {
    @Mock
    private Machine      machine;
    @Mock
    private SshClient    sshClient;
    @Mock
    private LineConsumer outputConsumer;

    private SshMachineInstance sshMachineInstance;

    @BeforeMethod
    public void setUp() {
        when(machine.getConfig()).thenReturn(mock(MachineConfig.class));
        when(machine.getEnvName()).thenReturn("EnvName");
        when(machine.getId()).thenReturn("Id");
        when(machine.getOwner()).thenReturn("Owner");
        when(machine.getRuntime()).thenReturn(mock(MachineRuntimeInfo.class));
        when(machine.getWorkspaceId()).thenReturn("WorkspaceId");

        sshMachineInstance = new SshMachineInstance(machine,
                                                    sshClient,
                                                    outputConsumer,
                                                    mock(SshMachineFactory.class),
                                                    new HashSet<>());
    }

    @Test
    public void shouldCloseOutputConsumerAndStopClientOnDestroy() throws Exception {
        sshMachineInstance.destroy();

        verify(outputConsumer).close();
        verify(sshClient).stop();
    }

}
