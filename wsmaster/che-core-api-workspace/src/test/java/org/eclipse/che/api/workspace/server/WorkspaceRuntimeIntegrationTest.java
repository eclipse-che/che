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
package org.eclipse.che.api.workspace.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.agent.server.launcher.AgentLauncherFactory;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.environment.server.CheEnvironmentEngine;
import org.eclipse.che.api.environment.server.ContainerNameGenerator;
import org.eclipse.che.api.environment.server.DefaultServicesStartStrategy;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.InfrastructureProvisioner;
import org.eclipse.che.api.environment.server.MachineInstanceProvider;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.SnapshotDao;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.test.mockito.answer.WaitingAnswer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRuntimeIntegrationTest {
    private static final Logger LOG          = getLogger(WorkspaceRuntimeIntegrationTest.class);
    private static final String WORKSPACE_ID = "workspace123";
    private static final String ENV_NAME     = "default-env";

    @Mock
    private EventService              eventService;
    @Mock
    private SnapshotDao               snapshotDao;
    @Mock
    private MachineInstanceProviders  machineInstanceProviders;
    @Mock
    private EnvironmentParser         environmentParser;
    @Mock
    private MachineInstanceProvider   instanceProvider;
    @Mock
    private InfrastructureProvisioner infrastructureProvisioner;
    @Mock
    private RecipeDownloader          recipeDownloader;
    @Mock
    private ContainerNameGenerator    containerNameGenerator;
    @Mock
    private AgentRegistry             agentRegistry;
    @Mock
    private AgentSorter               agentSorter;
    @Mock
    private AgentLauncherFactory      launcherFactory;

    private ExecutorService      executor;
    private WorkspaceRuntimes    runtimes;

    @BeforeMethod
    public void setUp() throws Exception {
        CheEnvironmentEngine environmentEngine = new CheEnvironmentEngine(snapshotDao,
                                                                          machineInstanceProviders,
                                                                          "/tmp",
                                                                          2000,
                                                                          eventService,
                                                                          environmentParser,
                                                                          new DefaultServicesStartStrategy(),
                                                                          instanceProvider,
                                                                          infrastructureProvisioner,
                                                                          "http://localhost:8080/api",
                                                                          recipeDownloader,
                                                                          containerNameGenerator,
                                                                          agentRegistry);

        runtimes = new WorkspaceRuntimes(eventService,
                                         environmentEngine,
                                         agentSorter,
                                         launcherFactory,
                                         agentRegistry);

        executor = Executors.newFixedThreadPool(
                1, new ThreadFactoryBuilder().setNameFormat(this.getClass().toString() + "-%d").build());

        EnvironmentContext.getCurrent().setSubject(new SubjectImpl("name", "id", "token", false));
    }

    @AfterMethod
    public void tearDown() throws Exception {
        executor.shutdownNow();

        EnvironmentContext.reset();
    }

    // Check for https://github.com/codenvy/codenvy/issues/593
    @Test(expectedExceptions = NotFoundException.class,
          expectedExceptionsMessageRegExp = "Workspace with id '" + WORKSPACE_ID + "' is not running.")
    public void environmentEngineShouldDestroyAllMachinesBeforeRemovalOfEnvironmentRecord() throws Exception {
        // given
        EnvironmentDto environment = newDto(EnvironmentDto.class);
        environment.withMachines(singletonMap("service1", newDto(ExtendedMachineDto.class)
                .withAgents(singletonList("org.eclipse.che.ws-agent"))));
        WorkspaceConfigDto config = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv(ENV_NAME)
                .withName("ws1")
                .withEnvironments(singletonMap(ENV_NAME, environment));
        WorkspaceDto workspace = newDto(WorkspaceDto.class).withId(WORKSPACE_ID)
                                                           .withNamespace("namespace")
                                                           .withConfig(config);
        Instance instance = mock(Instance.class);
        MachineConfigImpl machineConfig = new MachineConfigImpl();
        machineConfig.setDev(true);
        machineConfig.setName("service1");
        when(instance.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(instance.getId()).thenReturn("machineId");
        when(instance.getConfig()).thenReturn(machineConfig);

        CheServicesEnvironmentImpl internalEnv = new CheServicesEnvironmentImpl();
        internalEnv.getServices().put("service1", new CheServiceImpl().withId("machineId"));

        when(environmentParser.parse(any(Environment.class))).thenReturn(internalEnv);
        when(instanceProvider.startService(anyString(),
                                           anyString(),
                                           anyString(),
                                           anyString(),
                                           anyBoolean(),
                                           anyString(),
                                           any(CheServiceImpl.class),
                                           any(LineConsumer.class)))
                .thenReturn(instance);

        runtimes.start(workspace, ENV_NAME, false);

        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
        doAnswer(waitingAnswer).when(instance).destroy();

        // when
        executor.execute(() -> {
            try {
                runtimes.stop(WORKSPACE_ID);
            } catch (ApiException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        });

        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);

        // then
        // no exception - environment and workspace are still running
        runtimes.get(WORKSPACE_ID);
        // let instance removal proceed
        waitingAnswer.completeAnswer();
        // verify destroying was called
        verify(instance, timeout(1000)).destroy();
        verify(instanceProvider, timeout(1000)).destroyNetwork(anyString());
        // wait to ensure that removal of runtime is finished
        Thread.sleep(500);
        // runtime is removed - now getting of it should throw an exception
        runtimes.get(WORKSPACE_ID);
    }
}
