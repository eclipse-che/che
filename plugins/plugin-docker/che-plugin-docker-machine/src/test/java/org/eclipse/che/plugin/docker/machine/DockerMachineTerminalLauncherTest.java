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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 * @author Alexander Garagatyi
 */
@Listeners(value = {MockitoTestNGListener.class})
public class DockerMachineTerminalLauncherTest {
    private static final String LAUNCH_COMMAND = "launch terminal";
    private static final String CONTAINER      = "test container";
    private static final String EXEC_ID        = "testExecId";

    @Mock
    private DockerConnector docker;
    @Mock
    private Instance        testMachineInstance;
    @Mock
    private DockerInstance  dockerInstance;
    @Mock
    private Exec            exec;

    private DockerMachineImplTerminalLauncher launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        launcher = new DockerMachineImplTerminalLauncher(docker, LAUNCH_COMMAND);

        when(dockerInstance.getContainer()).thenReturn(CONTAINER);
        when(docker.createExec(CreateExecParams.create(CONTAINER,
                                                       new String[] {"/bin/bash",
                                                                     "-c",
                                                                     LAUNCH_COMMAND})
                                               .withDetach(true)))
                .thenReturn(exec);
        when(exec.getId()).thenReturn(EXEC_ID);
    }

    @Test
    public void shouldReturnDockerMachineType() throws Exception {
        assertEquals(launcher.getMachineType(), "docker");
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "Docker terminal launcher was used to launch terminal in non-docker machine.")
    public void shouldThrowExcIfNonDockerInstanceWasPassedAsArgument() throws Exception {
        launcher.launchTerminal(testMachineInstance);
    }

    @Test
    public void shouldCreateDetachedExecWithTerminalCommandInBash() throws Exception {
        launcher.launchTerminal(dockerInstance);

        verify(docker).createExec(CreateExecParams.create(CONTAINER, new String[] {"/bin/bash", "-c", LAUNCH_COMMAND})
                                                  .withDetach(true));
    }

    @Test
    public void shouldStartCreatedExec() throws Exception {
        launcher.launchTerminal(dockerInstance);

        verify(docker).startExec(eq(StartExecParams.create(EXEC_ID)), any());
    }

    @Test(expectedExceptions = MachineException.class,
          expectedExceptionsMessageRegExp = "test error")
    public void shouldThrowMachineExceptionIfIOExceptionWasThrownByDocker() throws Exception {
        when(docker.createExec(any(CreateExecParams.class))).thenThrow(new IOException("test error"));

        launcher.launchTerminal(dockerInstance);
    }
}
