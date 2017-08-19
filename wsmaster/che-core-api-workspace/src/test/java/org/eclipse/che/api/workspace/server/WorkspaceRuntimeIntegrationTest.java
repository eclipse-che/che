/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceRuntimeIntegrationTest {
  // FIXME: spi
  //    private WorkspaceRuntimes runtimes;

  //    @BeforeMethod
  //    public void setUp() throws Exception {
  //        CheEnvironmentEngine environmentEngine = new CheEnvironmentEngine(snapshotDao,
  //                                                                          machineInstanceProviders,
  //                                                                          "/tmp",
  //                                                                          2000,
  //                                                                          eventService,
  //                                                                          environmentParser,
  //                                                                          new DefaultServicesStartStrategy(),
  //                                                                          instanceProvider,
  //                                                                          infrastructureProvisioner,
  //                                                                          "http://localhost:8080/api",
  //                                                                          recipeDownloader,
  //                                                                          containerNameGenerator,
  //                                                                          agentRegistry);
  //
  ////        runtimes = new WorkspaceRuntimes(eventService,
  ////                                         environmentEngine,
  ////                                         agentSorter,
  ////                                         launcherFactory,
  ////                                         agentRegistry,
  ////                                         snapshotDao,
  ////                                         sharedPool);
  //
  //        executor = Executors.newFixedThreadPool(
  //                1, new ThreadFactoryBuilder().setNameFormat(this.getClass().toString() + "-%d").build());
  //
  //        EnvironmentContext.getCurrent().setSubject(new SubjectImpl("name", "id", "token", false));
  //    }
  //
  //    @AfterMethod
  //    public void tearDown() throws Exception {
  //        executor.shutdownNow();
  //
  //        EnvironmentContext.reset();
  //    }
  //
  //    // Check for https://github.com/codenvy/codenvy/issues/593
  //    @Test(expectedExceptions = NotFoundException.class,
  //          expectedExceptionsMessageRegExp = "Workspace with id '" + WORKSPACE_ID + "' is not running.")
  //    public void environmentEngineShouldDestroyAllMachinesBeforeRemovalOfEnvironmentRecord() throws Exception {
  //        // given
  //        EnvironmentDto environment = newDto(EnvironmentDto.class);
  //        environment.withMachines(singletonMap("service1", newDto(ExtendedMachineDto.class)
  //                .withInstallers(singletonList("org.eclipse.che.ws-agent"))));
  //        WorkspaceConfigDto config = newDto(WorkspaceConfigDto.class)
  //                .withDefaultEnv(ENV_NAME)
  //                .withName("ws1")
  //                .withEnvironments(singletonMap(ENV_NAME, environment));
  //        WorkspaceDto workspace = newDto(WorkspaceDto.class).withId(WORKSPACE_ID)
  //                                                           .withNamespace("namespace")
  //                                                           .withConfig(config);
  //        Instance instance = mock(Instance.class);
  //        OldMachineConfigImpl machineConfig = new OldMachineConfigImpl();
  //        machineConfig.setDev(true);
  //        machineConfig.setName("service1");
  //        when(instance.getWorkspaceId()).thenReturn(WORKSPACE_ID);
  //        when(instance.getId()).thenReturn("machineId");
  //        when(instance.getConfig()).thenReturn(machineConfig);
  //
  //        CheServicesEnvironmentImpl internalRuntimeConfig = new CheServicesEnvironmentImpl();
  //        internalRuntimeConfig.getServices().put("service1", new CheServiceImpl().withId("machineId"));
  //
  //        when(environmentParser.parse(any(Environment.class))).thenReturn(internalRuntimeConfig);
  //        when(instanceProvider.startService(anyString(),
  //                                           anyString(),
  //                                           anyString(),
  //                                           anyString(),
  //                                           anyBoolean(),
  //                                           anyString(),
  //                                           any(CheServiceImpl.class),
  //                                           any(LineConsumer.class)))
  //                .thenReturn(instance);
  //
  //        runtimes.start(workspace, ENV_NAME, false);
  //
  //        WaitingAnswer<Void> waitingAnswer = new WaitingAnswer<>();
  //        doAnswer(waitingAnswer).when(instance).destroy();
  //
  //        // when
  //        executor.execute(() -> {
  //            try {
  //                runtimes.stop(WORKSPACE_ID);
  //            } catch (ApiException e) {
  //                LOG.error(e.getLocalizedMessage(), e);
  //            }
  //        });
  //
  //        waitingAnswer.waitAnswerCall(1, TimeUnit.SECONDS);
  //
  //        // then
  //        // no exception - environment and workspace are still running
  //        runtimes.get(WORKSPACE_ID);
  //        // let instance removal proceed
  //        waitingAnswer.completeAnswer();
  //        // verify destroying was called
  //        verify(instance, timeout(1000)).destroy();
  //        verify(instanceProvider, timeout(1000)).destroyNetwork(anyString());
  //        // wait to ensure that removal of runtime is finished
  //        Thread.sleep(500);
  //        // runtime is removed - now getting of it should throw an exception
  //        runtimes.get(WORKSPACE_ID);
  //    }
}
