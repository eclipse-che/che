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

/**
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class RuntimesTest {

  // FIXME: spi
  //
  //    private static final Logger LOG = LoggerFactory.getLogger(RuntimesTest.class);
  //
  //    private static final String WORKSPACE_ID = "workspace123";
  //    private static final String ENV_NAME     = "default-env";
  //
  //    @Mock
  //    private EventService         eventService;
  //    @Mock
  //    private CheEnvironmentEngine envEngine;
  //    @Mock
  //    private InstallerSorter          agentSorter;
  //    @Mock
  //    private AgentLauncherFactory launcherFactory;
  //    @Mock
  //    private InstallerRegistry        agentRegistry;
  //
  //    @Mock
  //    private WorkspaceSharedPool  sharedPool;
  //    @Mock
  //
  //    private RuntimeInfrastructure infra;
  //
  //    @Captor
  //    private ArgumentCaptor<WorkspaceStatusEvent>     eventCaptor;
  //    @Captor
  //    private ArgumentCaptor<Callable>                 taskCaptor;
  //
  //    private WorkspaceRuntimes runtimes;
  //
  //    private Set<RuntimeInfrastructure> infras = new HashSet<>();
  //
  //
  //
  //    @BeforeMethod
  //    public void setUp() throws Exception {
  //
  //        //sharedPool = new WorkspaceSharedPool();
  //        //when(infra.getRecipeTypes()).thenReturn(asList("test"));
  //        infras.add(new TestRuntimeInfra());
  //        runtimes = spy(new WorkspaceRuntimes(eventService,
  //                                             infras,
  //                                             sharedPool));
  //
  //    }
  //
  //
  //    @Test(expectedExceptions = NotFoundException.class,
  //          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not running.")
  //    public void shouldThrowNotFoundExceptionIfWorkspaceRuntimeDoesNotExist() throws Exception {
  //        runtimes.get(WORKSPACE_ID);
  //    }
  //
  //    @Test
  //    public void shouldAddRuntimeOnRuntimesStart() throws Exception {
  //
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        Runtime runtime = runtimes.start(workspace, workspace.getConfig().getDefaultEnv(),
  // null);
  //        assertNotNull(runtime);
  //        assertEquals(runtime, runtimes.get(WORKSPACE_ID));
  //
  //    }
  //
  //    @Test
  //    public void shouldAddRuntimeOnRuntimesStartAsync() throws Exception {
  //
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        Future<Runtime> future =  runtimes.startAsync(workspace,
  // workspace.getConfig().getDefaultEnv(), null);
  //        assertNotNull(future.get());
  //        assertEquals(future.get(), runtimes.get(WORKSPACE_ID));
  //
  //    }

  //    @Test(expectedExceptions = ServerException.class,
  //          expectedExceptionsMessageRegExp = "Dev machine is not found in active environment of
  // workspace 'workspace123'")
  //    public void shouldThrowExceptionOnGetRuntimesIfDevMachineIsMissingInTheEnvironment() throws
  // Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //        when(envEngine.getMachines(workspace.getId()))
  //                .thenReturn(asList(createMachine(false), createMachine(false)));
  //
  //        // when
  //        runtimes.get(workspace.getId());
  //    }
  //
  //    @Test
  //    public void shouldFetchMachinesFromEnvEngineOnGetRuntime() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        Instance devMachine = createMachine(true);
  //        List<Instance> machines = asList(devMachine, createMachine(false));
  //        when(envEngine.start(anyString(),
  //                             anyString(),
  //                             any(Environment.class),
  //                             anyBoolean(),
  //                             any()))
  //                .thenReturn(machines);
  //        when(envEngine.getMachines(WORKSPACE_ID)).thenReturn(machines);
  //
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //
  //        // when
  //        RuntimeDescriptor runtimeDescriptor = runtimes.get(workspace.getId());
  //
  //        // then
  //        RuntimeDescriptor expected = new RuntimeDescriptor(WorkspaceStatus.RUNNING,
  //                                                           new RuntimeImpl(workspace.getConfig()
  //
  // .getDefaultEnv(),
  //
  // devMachine.getRuntime()
  //
  // .projectsRoot(),
  //                                                                                    machines,
  //
  // devMachine));
  //        verify(envEngine, times(2)).getMachines(workspace.getId());
  //        assertEquals(runtimeDescriptor, expected);
  //    }
  //
  //    @Test(expectedExceptions = ServerException.class,
  //          expectedExceptionsMessageRegExp = "Could not perform operation because application
  // server is stopping")
  //    public void shouldNotStartTheWorkspaceIfPostConstructWasIsInvoked() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.cleanup();
  //
  //        // when
  //        runtimes.start(createWorkspace(), workspace.getConfig().getDefaultEnv(), false);
  //    }
  //
  //    @Test
  //    public void workspaceShouldNotHaveRuntimeIfEnvStartFails() throws Exception {
  //        // given
  //        WorkspaceImpl ws1 = createWorkspace();
  //
  //        assertFalse(runtimes.hasRuntime(ws1.getId()));
  //
  //        when(runtimes.start(ws1,
  //                            ws1.getConfig().getDefaultEnv(),
  //                            null))
  //                .thenThrow(new ServerException("Test env start error"));
  //
  ////        runtimes.start(ws1, ws1.getConfig().getDefaultEnv(), null);
  //
  ////        WorkspaceImpl workspaceMock = createWorkspace();
  //
  //        try {
  //            // when
  //            runtimes.start(ws1, ws1.getConfig().getDefaultEnv(), null);
  ////            runtimes.start(workspaceMock,
  ////                           workspaceMock.getConfig().getDefaultEnv(),
  ////                           null);
  //        } catch (Exception ex) {
  //
  //            //LOG.info(">>>>> "+runtimes.get(ws1.getId()) + ws1);
  //            // then
  //            //assertFalse(runtimes.hasRuntime(ws1.getId()));
  //        }
  //    }
  //
  //    @Test
  //    public void workspaceShouldContainAllMachinesAndBeInRunningStatusAfterSuccessfulStart()
  // throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        // when
  //        RuntimeDescriptor runningWorkspace = runtimes.start(workspace,
  //
  // workspace.getConfig().getDefaultEnv(),
  //                                                            false);
  //
  //        // then
  //        assertEquals(runningWorkspace.getRuntimeStatus(), RUNNING);
  //        assertNotNull(runningWorkspace.getRuntime().getDevMachine());
  //        assertEquals(runningWorkspace.getRuntime().getMachines().size(), 2);
  //    }
  //
  //    @Test(expectedExceptions = ConflictException.class,
  //          expectedExceptionsMessageRegExp = "Could not start workspace '.*' because its status
  // is 'RUNNING'")
  //    public void shouldNotStartWorkspaceIfItIsAlreadyRunning() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //        // when
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //    }
  //
  //    @Test
  //    public void testCleanup() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //
  //        runtimes.cleanup();
  //
  //        // when, then
  //        assertFalse(runtimes.hasRuntime(workspace.getId()));
  //    }
  //
  //    @Test
  //    public void shouldStopRunningWorkspace() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //        // when
  //        runtimes.stop(workspace.getId(), null);
  //
  //        // then
  //        assertFalse(runtimes.hasRuntime(workspace.getId()));
  //    }
  //
  //    @Test(expectedExceptions = NotFoundException.class,
  //          expectedExceptionsMessageRegExp = "Workspace with id 'workspace123' is not running.")
  //    public void shouldThrowNotFoundExceptionWhenStoppingWorkspaceWhichDoesNotHaveRuntime()
  // throws Exception {
  //        runtimes.stop(WORKSPACE_ID, null);
  //    }
  //
  //    @Test
  //    public void startedRuntimeShouldBeTheSameToRuntimeTakenFromGetMethod() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        // when
  //        Runtime descriptorFromStartMethod = runtimes.start(workspace,
  //
  // workspace.getConfig().getDefaultEnv(),
  //                                                           null);
  //        Runtime descriptorFromGetMethod = runtimes.get(workspace.getId());
  //
  //        // then
  //        assertEquals(descriptorFromStartMethod,
  //                     descriptorFromGetMethod);
  //    }
  //
  //    @Test
  //    public void startingEventShouldBePublishedBeforeStart() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        // when
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //
  //        // then
  //        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
  //                                               .withWorkspaceId(workspace.getId())
  //                                               .withStatus(WorkspaceStatus.STARTING)
  //
  // .withEventType(WorkspaceStatusEvent.EventType.STARTING)
  //                                               .withPrevStatus(WorkspaceStatus.STOPPED));
  //    }
  //
  //    @Test
  //    public void runningEventShouldBePublishedAfterEnvStart() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //
  //        // when
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //
  //        // then
  //        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
  //                                               .withStatus(WorkspaceStatus.RUNNING)
  //                                               .withWorkspaceId(workspace.getId())
  //
  // .withEventType(WorkspaceStatusEvent.EventType.RUNNING)
  //                                               .withPrevStatus(WorkspaceStatus.STARTING));
  //    }
  //
  //    @Test
  //    public void errorEventShouldBePublishedIfDevMachineFailedToStart() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        when(envEngine.start(anyString(),
  //                             anyString(),
  //                             any(Environment.class),
  //                             anyBoolean(),
  //                             any()))
  //                .thenReturn(singletonList(createMachine(false)));
  //
  //        try {
  //            // when
  //            runtimes.start(workspace,
  //                           workspace.getConfig().getDefaultEnv(),
  //                           false);
  //
  //        } catch (Exception e) {
  //            // then
  //            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
  //                                                   .withWorkspaceId(workspace.getId())
  //                                                   .withEventType(EventType.FAILED)
  //                                                   .withPrevStatus(WorkspaceStatus.STARTING));
  //        }
  //    }
  //
  //    @Test
  //    public void stoppingEventShouldBePublishedBeforeStop() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //
  //        // when
  //        runtimes.stop(workspace.getId(), null);
  //
  //        // then
  //        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
  //                                               .withStatus(WorkspaceStatus.STOPPING)
  //                                               .withWorkspaceId(workspace.getId())
  //
  // .withEventType(WorkspaceStatusEvent.EventType.STOPPING)
  //                                               .withPrevStatus(WorkspaceStatus.RUNNING));
  //    }
  //
  //    @Test
  //    public void stoppedEventShouldBePublishedAfterEnvStop() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //
  //        // when
  //        runtimes.stop(workspace.getId(), null);
  //
  //        // then
  //        verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
  //                                               .withStatus(WorkspaceStatus.STOPPED)
  //                                               .withWorkspaceId(workspace.getId())
  //
  // .withEventType(WorkspaceStatusEvent.EventType.STOPPED)
  //                                               .withPrevStatus(WorkspaceStatus.STOPPING));
  //    }
  //
  //    @Test
  //    public void errorEventShouldBePublishedIfEnvFailedToStop() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       null);
  //
  //        try {
  //            // when
  //            runtimes.stop(workspace.getId(), null);
  //        } catch (Exception e) {
  //            // then
  //            verify(eventService).publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
  //                                                   .withWorkspaceId(workspace.getId())
  //
  // .withEventType(WorkspaceStatusEvent.EventType.FAILED)
  //                                                   .withPrevStatus(WorkspaceStatus.STOPPING)
  //                                                   .withError("Test error"));
  //        }
  //    }
  //
  //    @Test
  //    public void shouldBeAbleToStartMachine() throws Exception {
  //        // when
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //        OldMachineConfigImpl config = createConfig(false);
  //        Instance instance = mock(Instance.class);
  //        when(envEngine.startMachine(anyString(), any(OldMachineConfig.class),
  // any())).thenReturn(instance);
  //        when(instance.getConfig()).thenReturn(config);
  //
  //        // when
  //        Instance actual = runtimes.startMachine(workspace.getId(), config);
  //
  //        // then
  //        assertEquals(actual, instance);
  //        verify(envEngine).startMachine(eq(workspace.getId()), eq(config), any());
  //    }
  //
  ////    @Test
  ////    public void shouldAddTerminalAgentOnMachineStart() throws Exception {
  ////        // when
  ////        WorkspaceImpl workspace = createWorkspace();
  ////        runtimes.start(workspace,
  ////                       workspace.getConfig().getDefaultEnv(),
  ////                       false);
  ////        OldMachineConfigImpl config = createConfig(false);
  ////        Runtime instance = mock(Runtime.class);
  ////        when(envEngine.startMachine(anyString(), any(OldMachineConfig.class),
  // any())).thenReturn(instance);
  ////        when(instance.getConfig()).thenReturn(config);
  ////
  ////        // when
  ////        Runtime actual = runtimes.startMachine(workspace.getId(), config);
  ////
  ////        // then
  ////        assertEquals(actual, instance);
  ////        verify(envEngine).startMachine(eq(workspace.getId()),
  ////                                       eq(config),
  ////                                       eq(singletonList("org.eclipse.che.terminal")));
  ////        verify(runtimes).launchAgents(instance, singletonList("org.eclipse.che.terminal"));
  ////    }
  //
  //    @Test(expectedExceptions = NotFoundException.class,
  //          expectedExceptionsMessageRegExp = "Workspace with id '.*' is not running")
  //    public void shouldNotStartMachineIfEnvironmentIsNotRunning() throws Exception {
  //        // when
  //        OldMachineConfigImpl config = createConfig(false);
  //
  //        // when
  //        runtimes.startMachine("someWsID", config);
  //
  //        // then
  //        verify(envEngine, never()).startMachine(anyString(), any(OldMachineConfig.class),
  // any());
  //    }
  //
  //    @Test
  //    public void shouldBeAbleToStopMachine() throws Exception {
  //        // when
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //
  //        // when
  //        runtimes.stopMachine(workspace.getId(), "testMachineId");
  //
  //        // then
  //        verify(envEngine).stopMachine(workspace.getId(), "testMachineId");
  //    }
  //
  //    @Test(expectedExceptions = NotFoundException.class,
  //          expectedExceptionsMessageRegExp = "Workspace with id 'someWsID' is not running")
  //    public void shouldNotStopMachineIfEnvironmentIsNotRunning() throws Exception {
  //        // when
  //        runtimes.stopMachine("someWsID", "someMachineId");
  //
  //        // then
  //        verify(envEngine, never()).stopMachine(anyString(), anyString());
  //    }
  //
  //    @Test
  //    public void shouldBeAbleToGetMachine() throws Exception {
  //        // given
  //        Instance expected = createMachine(false);
  //        when(envEngine.getMachine(WORKSPACE_ID, expected.getId())).thenReturn(expected);
  //
  //        // when
  //        OldMachine actualMachine = runtimes.getMachine(WORKSPACE_ID, expected.getId());
  //
  //        // then
  //        assertEquals(actualMachine, expected);
  //        verify(envEngine).getMachine(WORKSPACE_ID, expected.getId());
  //    }
  //
  //    @Test
  //    public void shouldBeAbleToGetStatusOfRunningWorkspace() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //
  //        // when
  //        WorkspaceStatus status = runtimes.getStatus(workspace.getId());
  //
  //        // then
  //        assertEquals(status, RUNNING);
  //    }
  //
  //
  //    @Test
  //    public void shouldBeAbleToGetStatusOfStoppedWorkspace() throws Exception {
  //        // given
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //        runtimes.stop(workspace.getId());
  //
  //        // when
  //        WorkspaceStatus status = runtimes.getStatus(workspace.getId());
  //
  //        // then
  //        assertEquals(status, STOPPED);
  //    }
  //
  //    @Test(expectedExceptions = NotFoundException.class,
  //          expectedExceptionsMessageRegExp = "test exception")
  //    public void shouldThrowExceptionIfGetMachineFromEnvEngineThrowsException() throws Exception
  // {
  //        // given
  //        Instance expected = createMachine(false);
  //        when(envEngine.getMachine(WORKSPACE_ID, expected.getId()))
  //                .thenThrow(new NotFoundException("test exception"));
  //
  //        // when
  //        runtimes.getMachine(WORKSPACE_ID, expected.getId());
  //
  //        // then
  //        verify(envEngine).getMachine(WORKSPACE_ID, expected.getId());
  //    }
  //
  //    @Test
  //    public void shouldBeAbleToGetAllWorkspacesWithExistingRuntime() throws Exception {
  //        // then
  //        Map<String, WorkspaceRuntimes.WorkspaceState> expectedWorkspaces = new HashMap<>();
  //        WorkspaceImpl workspace = createWorkspace();
  //        runtimes.start(workspace,
  //                       workspace.getConfig().getDefaultEnv(),
  //                       false);
  //        expectedWorkspaces.put(workspace.getId(),
  //                               new WorkspaceRuntimes.WorkspaceState(RUNNING,
  //
  // workspace.getConfig().getDefaultEnv()));
  //        WorkspaceImpl workspace2 = spy(createWorkspace());
  //        when(workspace2.getId()).thenReturn("testWsId");
  //        when(envEngine.getMachines(workspace2.getId()))
  //                .thenReturn(Collections.singletonList(createMachine(true)));
  //        runtimes.start(workspace2,
  //                       workspace2.getConfig().getDefaultEnv(),
  //                       false);
  //        expectedWorkspaces.put(workspace2.getId(),
  //                               new WorkspaceRuntimes.WorkspaceState(RUNNING,
  //
  // workspace2.getConfig().getDefaultEnv()));
  //
  //        // when
  //        Map<String, WorkspaceRuntimes.WorkspaceState> actualWorkspaces =
  // runtimes.getWorkspaces();
  //
  //        // then
  //        assertEquals(actualWorkspaces, expectedWorkspaces);
  //    }
  //
  //
  //    private static Instance createMachine(boolean isDev) {
  //        return createMachine(createConfig(isDev));
  //    }
  //
  //    private static Instance createMachine(OldMachineConfig cfg) {
  //        return new TestMachineInstance(OldMachineImpl.builder()
  //                                                  .setId(NameGenerator.generate("machine", 10))
  //                                                  .setWorkspaceId(WORKSPACE_ID)
  //                                                  .setEnvName(ENV_NAME)
  //                                                  .setConfig(new OldMachineConfigImpl(cfg))
  //                                                  .build());
  //    }
  //
  //    private static OldMachineConfigImpl createConfig(boolean isDev) {
  //        return OldMachineConfigImpl.builder()
  //                                .setDev(isDev)
  //                                .setType("docker")
  //                                .setLimits(new MachineLimitsImpl(1024))
  //                                .setSource(new MachineSourceImpl("git").setLocation("location"))
  //                                .setName(UUID.randomUUID().toString())
  //                                .build();
  //    }
  //
  //    private static WorkspaceImpl createWorkspace() {
  //        EnvironmentImpl environment = new EnvironmentImpl(new RecipeImpl("test", "text/text",
  // "", ""), null);
  //        WorkspaceConfigImpl wsConfig = WorkspaceConfigImpl.builder()
  //                                                          .setName("test workspace")
  //
  // .setEnvironments(singletonMap(ENV_NAME, environment))
  //                                                          .setDefaultEnv(ENV_NAME)
  //                                                          .build();
  //        return new WorkspaceImpl(WORKSPACE_ID, new AccountImpl("accountId", "user123", "test"),
  // wsConfig);
  //    }

  //    @SuppressWarnings("unchecked")
  //    private void captureAsyncTaskAndExecuteSynchronously() throws Exception {
  //        verify(sharedPool).submit(taskCaptor.capture());
  //        taskCaptor.getValue().call();
  //    }
  //
  //    private static class TestMachineInstance extends NoOpMachineInstance {
  //
  //        MachineImpl runtime;
  //
  //        public TestMachineInstance(OldMachine machine) {
  //            super(machine);
  //            runtime = mock(MachineImpl.class);
  //        }
  //
  //        @Override
  //        public MachineImpl getRuntime() {
  //            return runtime;
  //        }
  //    }

  //
  //    public static class TestRuntimeInfra extends RuntimeInfrastructure {
  //
  //        private Map<RuntimeIdentityImpl, InternalRuntime> runtimes = new HashMap<>();
  //
  //        public TestRuntimeInfra() throws ValidationException {
  //            super("test", singleton("test"));
  //        }
  //
  //        @Override
  //        public Environment estimate(Environment environment) throws ServerException {
  //            return environment;
  //        }
  //
  //        @Override
  //        public Set<RuntimeIdentityImpl> getIdentities() {
  //            return runtimes.keySet();
  //        }
  //
  //        @Override
  //        public InternalRuntime getRuntime(RuntimeIdentityImpl id) {
  //            return runtimes.get(id);
  //        }
  //
  //        @Override
  //        public RuntimeContext prepare(RuntimeIdentityImpl id, Environment environment) throws
  // ValidationException, ApiException, IOException {
  //            return new TestRuntimeContext(environment, id, this);
  //        }
  //
  //

  //
  //        @Override
  //        public void stop(RuntimeIdentityImpl runtimeId, Map<String, String> options) {
  //            LOG.info("Stopped " + runtimeId.getWorkspaceId());
  //        }
  //
  //
  //        @Override
  //        public Runtime start(RuntimeIdentityImpl runtimeId, Environment environment,
  // MessageConsumer<MachineLogMessage> logger,
  //                                      Map<String, String> options, Subject subject)
  //                throws NotFoundException, ConflictException, ServerException {
  //
  //            Runtime rt = new RuntimeImpl(runtimeId.getEnvName(), new HashMap<>(),
  // subject.getUserName());
  //            runtimes.put(runtimeId, rt);
  //            return rt;
  //        }
  //
  //        @Override
  //        public Map<RuntimeIdentityImpl, Runtime> getAll() {
  //            return runtimes;
  //        }
  //    }
  //
  //    public static class TestRuntimeContext extends RuntimeContext {
  //        public TestRuntimeContext(Environment environment, RuntimeIdentityImpl identity,
  //                                  RuntimeInfrastructure infrastructure) throws ApiException,
  // IOException, ValidationException {
  //            super(environment, identity, infrastructure, null);
  //        }
  //
  //        @Override
  //        protected InternalRuntime internalStart(Map<String, String> startOptions) throws
  // ServerException {
  //            return new TestInternalRuntime(this);
  //        }
  //
  //        @Override
  //        protected void internalStop(Map<String, String> stopOptions) throws ServerException {
  //
  //        }
  //
  //        @Override
  //        public URL getOutputChannel() throws NotSupportedException, ServerException {
  //            return null;
  //        }
  //    }
  //
  //    public static class TestInternalRuntime extends InternalRuntime {
  //
  //        public TestInternalRuntime(RuntimeContext context) {
  //            super(context);
  //        }
  //
  //        @Override
  //        public Map<String, ? extends Machine> getMachines() {
  //
  //            Map<String, Machine> machines = new HashMap<>();
  //
  //            Map<String, Server> servers = new HashMap<>();
  //
  //            servers.put("server", new ServerImpl("http://localhost"));
  //
  //            machines.put("machine", new MachineImpl(new HashMap<>(), servers));
  //
  //            return machines;
  //        }
  //
  //        @Override
  //        public List<? extends Warning> getWarnings() {
  //            return null;
  //        }
  //
  //        @Override
  //        public Map<String, String> getProperties() {
  //            return new HashMap<>();
  //        }
  //    }

}
