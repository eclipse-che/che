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
package org.eclipse.che.api.machine.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.exception.SourceNotFoundException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link MachineManager}
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class MachineManagerTest {

    private static final int    DEFAULT_MACHINE_MEMORY_SIZE_MB = 1000;
    private static final String WS_ID                          = "testWsId";
    private static final String ENVIRONMENT_NAME               = "testEnvName";
    private static final String USER_ID                        = "userId";
    private static final String MACHINE_ID                     = "machineId";
    private static final String NAMESPACE                      = "namespace";

    private static final SubjectImpl CREATOR = new SubjectImpl("name", USER_ID, "token", false);

    @Mock
    private MachineInstanceProviders machineInstanceProviders;
    @Mock
    private InstanceProvider         instanceProvider;
    @Mock
    private MachineRegistry          machineRegistry;
    @Mock
    private WsAgentLauncher          wsAgentLauncher;
    @Mock
    private Instance                 instance;
    @Mock
    private Limits                   limits;
    @Mock
    private Command                  command;
    @Mock
    private InstanceProcess          instanceProcess;
    @Mock
    private LineConsumer             logConsumer;
    @Mock
    private SnapshotDao              snapshotDao;

    private MachineManager manager;

    @BeforeMethod
    public void setUp() throws Exception {
        final EventService eventService = mock(EventService.class);
        final String machineLogsDir = targetDir().resolve("logs-dir").toString();
        IoUtil.deleteRecursive(new File(machineLogsDir));
        manager = spy(new MachineManager(snapshotDao,
                                         machineRegistry,
                                         machineInstanceProviders,
                                         machineLogsDir,
                                         eventService,
                                         DEFAULT_MACHINE_MEMORY_SIZE_MB,
                                         wsAgentLauncher));

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setSubject(CREATOR);
        EnvironmentContext.setCurrent(envCont);

        RecipeImpl recipe = new RecipeImpl().withScript("script").withType("Dockerfile");
//        doNothing().when(manager).createMachineLogsDir(anyString());
        doReturn(MACHINE_ID).when(manager).generateMachineId();
        doReturn(logConsumer).when(manager).getProcessLogger(MACHINE_ID, 111, "outputChannel");
        when(machineInstanceProviders.getProvider(anyString())).thenReturn(instanceProvider);
        HashSet<String> recipeTypes = new HashSet<>();
        recipeTypes.add("test type 1");
        recipeTypes.add("snapshot");
        recipeTypes.add("dockerfile");
        when(instanceProvider.getRecipeTypes()).thenReturn(recipeTypes);
        when(instanceProvider.createInstance(any(Machine.class), any(LineConsumer.class))).thenReturn(instance);
        when(machineRegistry.getInstance(anyString())).thenReturn(instance);
        when(command.getCommandLine()).thenReturn("CommandLine");
        when(command.getName()).thenReturn("CommandName");
        when(command.getType()).thenReturn("CommandType");
        when(machineRegistry.getInstance(MACHINE_ID)).thenReturn(instance);
        when(instance.createProcess(command, "outputChannel")).thenReturn(instanceProcess);
        when(instanceProcess.getPid()).thenReturn(111);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Invalid machine name @name!")
    public void shouldThrowExceptionOnMachineCreationIfMachineNameIsInvalid() throws Exception {
        MachineConfig machineConfig = new MachineConfigImpl(false,
                                                            "@name!",
                                                            "machineType",
                                                            new MachineSourceImpl("Dockerfile").setLocation("location"),
                                                            new LimitsImpl(1024),
                                                            Arrays.asList(new ServerConfImpl("ref1",
                                                                                             "8080",
                                                                                             "https",
                                                                                             "some/path"),
                                                                          new ServerConfImpl("ref2",
                                                                                             "9090/udp",
                                                                                             "someprotocol",
                                                                                             "/some/path")),
                                                            Collections.singletonMap("key1", "value1"));
        String workspaceId = "wsId";
        String environmentName = "env1";

        manager.createMachineSync(machineConfig, workspaceId, environmentName, logConsumer);
    }

    @Test
    public void shouldBeAbleToCreateMachineWithValidName() throws Exception {
        String expectedName = "validMachineName";
        final MachineConfigImpl machineConfig = MachineConfigImpl.builder()
                                                                 .fromConfig(createMachineConfig())
                                                                 .setName(expectedName)
                                                                 .build();
        MachineImpl expectedMachine = new MachineImpl(machineConfig,
                                                      MACHINE_ID,
                                                      WS_ID,
                                                      ENVIRONMENT_NAME,
                                                      USER_ID,
                                                      MachineStatus.CREATING,
                                                      null);

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME, logConsumer);

        verify(machineRegistry).addMachine(eq(expectedMachine));
    }

    @Test
    public void shouldCallWsAgentLauncherAfterDevMachineStart() throws Exception {
        final MachineConfigImpl machineConfig = MachineConfigImpl.builder()
                                                                 .fromConfig(createMachineConfig())
                                                                 .setDev(true)
                                                                 .build();

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME, logConsumer);

        verify(wsAgentLauncher).startWsAgent(WS_ID);
    }

    @Test
    public void shouldNotCallWsAgentLauncherAfterNonDevMachineStart() throws Exception {
        final MachineConfigImpl machineConfig = createMachineConfig();

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME, logConsumer);

        verify(wsAgentLauncher, never()).startWsAgent(WS_ID);
    }

    @Test
    public void shouldRemoveMachineFromRegistryIfInstanceDestroyingFailsOnDestroy() throws Exception {
        final MachineConfigImpl machineConfig = createMachineConfig();
        when(instance.getConfig()).thenReturn(machineConfig);
        when(instance.getWorkspaceId()).thenReturn(WS_ID);
        doThrow(new MachineException("test")).when(instance).destroy();

        try {
            manager.destroy(MACHINE_ID, false);
        } catch (Exception e) {
            verify(machineRegistry).remove(MACHINE_ID);
        }
    }

    @Test
    public void shouldCloseProcessLoggerIfExecIsSuccess() throws Exception {
        //when
        manager.exec(MACHINE_ID, command, "outputChannel");
        waitForExecutorIsCompletedTask();

        //then
        verify(logConsumer).close();
    }

    @Test
    public void shouldCloseProcessLoggerIfExecFails() throws Exception {
        //given
        doThrow(Exception.class).when(instanceProcess).start();

        //when
        manager.exec(MACHINE_ID, command, "outputChannel");
        waitForExecutorIsCompletedTask();

        //then
        verify(logConsumer).close();
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldCloseMachineLoggerIfMachineCreationFails() throws Exception {
        //given
        MachineConfig machineConfig = mock(MachineConfig.class);
        MachineSource machineSource = mock(MachineSource.class);
        LineConsumer machineLogger = mock(LineConsumer.class);
        doReturn(machineLogger).when(manager).getMachineLogger(eq(MACHINE_ID), any(LineConsumer.class));
        when(machineConfig.getSource()).thenReturn(machineSource);
        when(machineConfig.getName()).thenReturn("Name");
        when(machineSource.getType()).thenReturn("dockerfile");
        doThrow(ConflictException.class).when(machineRegistry).addMachine(any());

        //when
        manager.createMachineSync(machineConfig, "workspaceId", "environmentName", logConsumer);

        //then
        verify(machineLogger).close();
    }

    @Test
    public void shouldCreateMachineFromOriginalSourceWhenMachineRecoverFails() throws Exception {
        final SnapshotImpl snapshot = createSnapshot();
        final MachineConfigImpl config = createMachineConfig();
        when(manager.generateMachineId()).thenReturn(MACHINE_ID);
        final MachineImpl machine = new MachineImpl(config,
                                                    MACHINE_ID,
                                                    WS_ID,
                                                    ENVIRONMENT_NAME,
                                                    CREATOR.getUserId(),
                                                    MachineStatus.CREATING,
                                                    null);
        machine.getConfig().setSource(snapshot.getMachineSource());
        when(snapshotDao.getSnapshot(WS_ID, ENVIRONMENT_NAME, config.getName())).thenReturn(snapshot);
        when(instanceProvider.createInstance(eq(machine), any(LineConsumer.class))).thenThrow(new SourceNotFoundException(""));
        when(machineRegistry.getMachine(MACHINE_ID)).thenReturn(machine);

        final MachineImpl result = manager.recoverMachine(config, WS_ID, ENVIRONMENT_NAME, logConsumer);

        machine.getConfig().setSource(config.getSource());
        assertEquals(result, machine);
    }

    @Test(expectedExceptions = MachineException.class)
    public void shouldThrowExceptionWhenMachineCreationFromOriginSourceFailed() throws Exception {
        final MachineConfigImpl config = createMachineConfig();
        when(manager.generateMachineId()).thenReturn(MACHINE_ID);

        when(instanceProvider.createInstance(any(MachineImpl.class),
                                             any(LineConsumer.class))).thenThrow(new MachineException(""));

        manager.recoverMachine(config, WS_ID, ENVIRONMENT_NAME, logConsumer);
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

    private MachineConfigImpl createMachineConfig() {
        return new MachineConfigImpl(false,
                                     "MachineName",
                                     "docker",
                                     new MachineSourceImpl("Dockerfile").setLocation("location"),
                                     new LimitsImpl(1024),
                                     Arrays.asList(new ServerConfImpl("ref1",
                                                                      "8080",
                                                                      "https",
                                                                      "some/path"),
                                                   new ServerConfImpl("ref2",
                                                                      "9090/udp",
                                                                      "someprotocol",
                                                                      "/some/path")),
                                     Collections.singletonMap("key1", "value1"));
    }

    private SnapshotImpl createSnapshot() {
        return SnapshotImpl.builder()
                           .setId("snapshot12")
                           .fromConfig(createMachineConfig())
                           .setWorkspaceId(WS_ID)
                           .setEnvName(ENVIRONMENT_NAME)
                           .setNamespace(NAMESPACE)
                           .setMachineSource(new MachineSourceImpl("snapshot").setLocation("location"))
                           .setDev(true)
                           .build();
    }
}
