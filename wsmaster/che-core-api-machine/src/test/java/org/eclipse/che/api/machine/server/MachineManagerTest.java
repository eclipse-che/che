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
import org.eclipse.che.api.core.model.machine.Limits;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.machine.server.dao.SnapshotDao;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProvider;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.machine.server.wsagent.WsAgentLauncher;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.user.UserImpl;
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

    @Mock
    private MachineInstanceProviders machineInstanceProviders;
    @Mock
    private RecipeDownloader         recipeDownloader;
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

    private MachineManager manager;

    @BeforeMethod
    public void setUp() throws Exception {
        final SnapshotDao snapshotDao = mock(SnapshotDao.class);
        final EventService eventService = mock(EventService.class);
        final String machineLogsDir = targetDir().resolve("logs-dir").toString();
        IoUtil.deleteRecursive(new File(machineLogsDir));
        manager = spy(new MachineManager(snapshotDao,
                                         machineRegistry,
                                         machineInstanceProviders,
                                         machineLogsDir,
                                         eventService,
                                         DEFAULT_MACHINE_MEMORY_SIZE_MB,
                                         wsAgentLauncher,
                                         recipeDownloader));

        EnvironmentContext envCont = new EnvironmentContext();
        envCont.setUser(new UserImpl(null, USER_ID, null, null, false));
        EnvironmentContext.setCurrent(envCont);

        RecipeImpl recipe = new RecipeImpl().withScript("script").withType("Dockerfile");
//        doNothing().when(manager).createMachineLogsDir(anyString());
        doReturn(MACHINE_ID).when(manager).generateMachineId();
        when(recipeDownloader.getRecipe(any(MachineConfig.class))).thenReturn(recipe);
        when(machineInstanceProviders.getProvider(anyString())).thenReturn(instanceProvider);
        HashSet<String> recipeTypes = new HashSet<>();
        recipeTypes.add("test type 1");
        recipeTypes.add("dockerfile");
        when(instanceProvider.getRecipeTypes()).thenReturn(recipeTypes);
        when(instanceProvider.createInstance(eq(recipe), any(Machine.class), any(LineConsumer.class))).thenReturn(instance);
        when(machineRegistry.getInstance(anyString())).thenReturn(instance);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Invalid machine name @name!")
    public void shouldThrowExceptionOnMachineCreationIfMachineNameIsInvalid() throws Exception {
        when(recipeDownloader.getRecipe(any(MachineConfig.class))).thenReturn(new RecipeImpl().withScript("script")
                                                                                              .withType("Dockerfile"));

        MachineConfig machineConfig = new MachineConfigImpl(false,
                                                            "@name!",
                                                            "machineType",
                                                            new MachineSourceImpl("Dockerfile", "location"),
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

        manager.createMachineSync(machineConfig, workspaceId, environmentName);
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

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME);

        verify(machineRegistry).addMachine(eq(expectedMachine));
    }

    @Test
    public void shouldCallWsAgentLauncherAfterDevMachineStart() throws Exception {
        final MachineConfigImpl machineConfig = MachineConfigImpl.builder()
                                                                 .fromConfig(createMachineConfig())
                                                                 .setDev(true)
                                                                 .build();

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME);

        verify(wsAgentLauncher).startWsAgent(WS_ID);
    }

    @Test
    public void shouldNotCallWsAgentLauncherAfterNonDevMachineStart() throws Exception {
        final MachineConfigImpl machineConfig = createMachineConfig();

        manager.createMachineSync(machineConfig, WS_ID, ENVIRONMENT_NAME);

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

    private static Path targetDir() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        return Paths.get(url.toURI()).getParent();
    }

    private MachineConfigImpl createMachineConfig() {
        return new MachineConfigImpl(false,
                                     "MachineName",
                                     "docker",
                                     new MachineSourceImpl("Dockerfile", "location"),
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
}
