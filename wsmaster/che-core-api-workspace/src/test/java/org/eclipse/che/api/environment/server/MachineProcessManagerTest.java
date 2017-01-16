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
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link MachineProcessManager}
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class MachineProcessManagerTest {

    private static final String USER_ID          = "userId";
    private static final String MACHINE_ID       = "machineId";
    private static final String WORKSPACE_ID     = "testWorkspaceId";

    private static final SubjectImpl CREATOR = new SubjectImpl("name", USER_ID, "token", false);

    @Mock
    Instance         instance;
    @Mock
    Command          command;
    @Mock
    InstanceProcess  instanceProcess;
    @Mock
    LineConsumer     logConsumer;
    @Mock
    CheEnvironmentEngine environmentEngine;

    private MachineProcessManager manager;

    @BeforeMethod
    public void setUp() throws Exception {
        final EventService eventService = mock(EventService.class);
        final String machineLogsDir = targetDir().resolve("logs-dir").toString();
        IoUtil.deleteRecursive(new File(machineLogsDir));
        manager = spy(new MachineProcessManager(machineLogsDir,
                                                eventService,
                                                environmentEngine));

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setSubject(CREATOR);
        EnvironmentContext.setCurrent(envCont);

        doReturn(logConsumer).when(manager).getProcessLogger(MACHINE_ID, 111, "outputChannel");
        when(command.getCommandLine()).thenReturn("CommandLine");
        when(command.getName()).thenReturn("CommandName");
        when(command.getType()).thenReturn("CommandType");
        when(instance.createProcess(command, "outputChannel")).thenReturn(instanceProcess);
        when(instanceProcess.getPid()).thenReturn(111);
        when(environmentEngine.getMachine(WORKSPACE_ID, MACHINE_ID)).thenReturn(instance);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldCloseProcessLoggerIfExecIsSuccess() throws Exception {
        //when
        manager.exec(WORKSPACE_ID, MACHINE_ID, command, "outputChannel");
        waitForExecutorIsCompletedTask();

        //then
        verify(logConsumer).close();
    }

    @Test
    public void shouldCloseProcessLoggerIfExecFails() throws Exception {
        //given
        doThrow(Exception.class).when(instanceProcess).start();

        //when
        manager.exec(WORKSPACE_ID, MACHINE_ID, command, "outputChannel");
        waitForExecutorIsCompletedTask();

        //then
        verify(logConsumer).close();
    }

    private void waitForExecutorIsCompletedTask() throws Exception {
        for (int i = 0; ((ThreadPoolExecutor)manager.executor).getCompletedTaskCount() == 0 && i < 10; i++) {
            Thread.sleep(300);
        }
    }

    private static Path targetDir() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }
}
