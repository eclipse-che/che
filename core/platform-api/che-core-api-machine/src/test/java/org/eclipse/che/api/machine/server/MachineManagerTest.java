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
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.Channels;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineMetadata;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.core.model.machine.MachineState;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.impl.AbstractInstance;
import org.eclipse.che.api.machine.server.model.impl.ChannelsImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.eclipse.che.api.machine.server.spi.InstanceNode;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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
    private MachineConfig            machineConfig;
    @Mock
    private Limits                   limits;

    private MachineManager manager;

    @BeforeMethod
    public void setUp() throws Exception {
        final SnapshotDao snapshotDao = mock(SnapshotDao.class);
        final EventService eventService = mock(EventService.class);
        final String machineLogsDir = targetDir().resolve("logs-dir").toString();
        manager = spy(new MachineManager(snapshotDao,
                                         machineRegistry,
                                         machineInstanceProviders,
                                         machineLogsDir,
                                         eventService,
                                         DEFAULT_MACHINE_MEMORY_SIZE_MB,
                                         "apiEndpoint",
                                         wsAgentLauncher));

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setUser(new UserImpl("user", null, null, null, false));
        EnvironmentContext.setCurrent(envCont);

        RecipeImpl recipe = new RecipeImpl().withScript("script").withType("Dockerfile");
        when(machineConfig.isDev()).thenReturn(false);
        when(machineConfig.getName()).thenReturn("MachineName");
        when(machineConfig.getType()).thenReturn("docker");
        when(machineConfig.getSource()).thenReturn(new MachineSourceImpl("Recipe", "location"));
        when(machineConfig.getLimits()).thenReturn(new LimitsImpl(1024));
        when(instance.getId()).thenReturn("machineId");
        when(instance.getChannels()).thenReturn(new ChannelsImpl("chan1", "chan2"));
        when(instance.getEnvName()).thenReturn("env1");
        when(instance.getLimits()).thenAnswer(invocation -> machineConfig.getLimits());
        when(instance.isDev()).thenAnswer(invocation -> machineConfig.isDev());
        when(instance.getName()).thenAnswer(invocation -> machineConfig.getName());
        when(instance.getOwner()).thenReturn("owner");
        when(instance.getSource()).thenAnswer(invocation -> machineConfig.getSource());
        when(instance.getStatus()).thenReturn(MachineStatus.CREATING);
        when(instance.getType()).thenAnswer(invocation -> machineConfig.getType());
        when(instance.getWorkspaceId()).thenReturn(WS_ID);
        doReturn(recipe).when(manager).getRecipeByLocation(any(MachineConfig.class));
        when(machineInstanceProviders.getProvider(anyString())).thenReturn(instanceProvider);
        when(instanceProvider.createInstance(eq(recipe), any(MachineState.class), any(LineConsumer.class))).thenReturn(instance);
        when(machineRegistry.get(anyString())).thenReturn(instance);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Invalid machine name @name!")
    public void shouldThrowExceptionOnMachineCreationIfMachineNameIsInvalid() throws Exception {
        doReturn(new RecipeImpl().withScript("script").withType("Dockerfile"))
                .when(manager).getRecipeByLocation(any(MachineConfig.class));

        MachineConfig machineConfig = new MachineConfigImpl(false,
                                                            "@name!",
                                                            "machineType",
                                                            new MachineSourceImpl("Recipe", "location"),
                                                            new LimitsImpl(1024));
        String workspaceId = "wsId";
        String environmentName = "env1";

        manager.createMachineSync(machineConfig, workspaceId, environmentName);
    }

    @Test
    public void shouldBeAbleToCreateMachineWithValidName() throws Exception {
        String expectedName = "validMachineName";
        when(machineConfig.getName()).thenReturn(expectedName);

        final MachineImpl machine = manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME);

        assertEquals(machine.getName(), expectedName);
    }

    @Test
    public void shouldCallWsAgentLauncherAfterDevMachineStart() throws Exception {
        when(machineConfig.isDev()).thenReturn(true);

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME);

        verify(wsAgentLauncher).startWsAgent(WS_ID);
    }

    @Test
    public void shouldNotCallWsAgentLauncherAfterNonDevMachineStart() throws Exception {
        when(machineConfig.isDev()).thenReturn(false);

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME);

        verify(wsAgentLauncher, never()).startWsAgent(WS_ID);
    }

    private static Path targetDir() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }

    private static class NoOpInstanceImpl extends AbstractInstance {

        public NoOpInstanceImpl(String id,
                                String type,
                                String workspaceId,
                                String owner,
                                boolean isDev,
                                String displayName,
                                Channels channels,
                                Limits limits,
                                MachineSource source,
                                MachineStatus machineStatus,
                                String envName) {
            super(id, type, workspaceId, owner, isDev, displayName, channels, limits, source, machineStatus, envName);
        }

        @Override
        public LineConsumer getLogger() {
            return null;
        }

        @Override
        public InstanceProcess getProcess(int pid) throws NotFoundException, MachineException {
            return null;
        }

        @Override
        public List<InstanceProcess> getProcesses() throws MachineException {
            return null;
        }

        @Override
        public InstanceProcess createProcess(Command command, String outputChannel)
                throws MachineException {
            return null;
        }

        @Override
        public InstanceKey saveToSnapshot(String owner) throws MachineException {
            return null;
        }

        @Override
        public void destroy() throws MachineException {

        }

        @Override
        public InstanceNode getNode() {
            return null;
        }

        @Override
        public String readFileContent(String filePath, int startFrom, int limit) throws MachineException {
            return null;
        }

        @Override
        public void copy(Instance sourceMachine, String sourcePath, String targetPath, boolean overwrite) throws MachineException {

        }

        @Override
        public MachineMetadata getMetadata() {
            return null;
        }
    }
}
