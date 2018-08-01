/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.FAILED;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STOPPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeResult.ProbeStatus;
import org.eclipse.che.api.workspace.server.hc.probe.ProbeScheduler;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbes;
import org.eclipse.che.api.workspace.server.hc.probe.WorkspaceProbesFactory;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.docker.bootstrap.DockerBootstrapper;
import org.eclipse.che.workspace.infrastructure.docker.bootstrap.DockerBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.docker.logs.MachineLoggersFactory;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.monit.AbnormalMachineStopHandler;
import org.eclipse.che.workspace.infrastructure.docker.network.NetworkLifecycle;
import org.eclipse.che.workspace.infrastructure.docker.server.mapping.ExternalIpURLRewriter;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests {@link DockerInternalRuntime}.
 *
 * @author Anton Korneta
 */
public class DockerInternalRuntimeTest {

  private static final RuntimeIdentity IDENTITY = new RuntimeIdentityImpl("ws1", "env1", "id1");
  private static final String DEV_MACHINE = "DEV_MACHINE";
  private static final String DB_MACHINE = "DB_MACHINE";
  private static final String SERVER_1 = "serv1";
  private static final String SERVER_URL = "https://localhost:443/path";

  private static final int BOOTSTRAPPING_TIMEOUT_MINUTES = 5;

  @Mock private DockerBootstrapperFactory bootstrapperFactory;
  @Mock private DockerRuntimeContext runtimeContext;
  @Mock private EventService eventService;
  @Mock private DockerMachineStarter starter;
  @Mock private NetworkLifecycle networks;
  @Mock private DockerBootstrapper bootstrapper;
  @Mock private WorkspaceProbesFactory workspaceProbesFactory;
  @Mock private ProbeScheduler probesScheduler;
  @Mock private WorkspaceProbes workspaceProbes;
  @Mock private DockerMachine dockerMachine;
  @Mock private ParallelDockerImagesBuilderFactory dockerImagesBuilderFactory;
  @Mock private ParallelDockerImagesBuilder dockerImagesBuilder;

  @Captor private ArgumentCaptor<Consumer<ProbeResult>> probeResultConsumerCaptor;
  @Captor private ArgumentCaptor<MachineStatusEvent> eventCaptor;

  private DockerInternalRuntime dockerRuntime;

  @BeforeMethod
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    final DockerContainerConfig config1 = new DockerContainerConfig();
    final DockerContainerConfig config2 = new DockerContainerConfig();
    final InternalMachineConfig internalMachineCfg1 = mock(InternalMachineConfig.class);
    when(internalMachineCfg1.getInstallers()).thenReturn(singletonList(newInstaller(1)));
    final InternalMachineConfig internalMachineCfg2 = mock(InternalMachineConfig.class);
    when(internalMachineCfg2.getInstallers()).thenReturn(singletonList(newInstaller(2)));

    ImmutableMap<String, InternalMachineConfig> machines =
        ImmutableMap.of(DEV_MACHINE, internalMachineCfg1, DB_MACHINE, internalMachineCfg2);
    final DockerEnvironment environment = new DockerEnvironment(null, machines, emptyList());
    environment.setContainers(
        Maps.newLinkedHashMap(ImmutableMap.of(DEV_MACHINE, config1, DB_MACHINE, config2)));

    when(runtimeContext.getEnvironment()).thenReturn(environment);

    doNothing().when(networks).createNetwork(nullable(String.class));
    when(runtimeContext.getIdentity()).thenReturn(IDENTITY);
    when(runtimeContext.getEnvironment()).thenReturn(environment);
    ServersCheckerFactory serversCheckerFactory = mock(ServersCheckerFactory.class);
    when(serversCheckerFactory.create(any(), nullable(String.class), any()))
        .thenReturn(mock(ServersChecker.class));
    when(workspaceProbesFactory.getProbes(eq(IDENTITY), anyString(), any()))
        .thenReturn(workspaceProbes);
    when(dockerImagesBuilderFactory.create(any())).thenReturn(dockerImagesBuilder);
    when(dockerImagesBuilder.prepareImages(anyMap())).thenReturn(emptyMap());
    dockerRuntime =
        new DockerInternalRuntime(
            runtimeContext,
            emptyList(),
            mock(ExternalIpURLRewriter.class),
            networks,
            starter,
            eventService,
            bootstrapperFactory,
            serversCheckerFactory,
            mock(MachineLoggersFactory.class),
            probesScheduler,
            workspaceProbesFactory,
            dockerImagesBuilderFactory,
            BOOTSTRAPPING_TIMEOUT_MINUTES);
  }

  @Test
  public void startsDockerRuntimeAndPropagatesMachineStatusEvents() throws Exception {
    mockInstallersBootstrap();
    mockContainerStart();
    dockerRuntime.start(emptyMap());

    verify(starter, times(2))
        .startContainer(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            any(),
            any(),
            any());
    verify(eventService, times(4)).publish(any(MachineStatusEvent.class));
    verifyEventsOrder(
        newEvent(DEV_MACHINE, STARTING, null),
        newEvent(DEV_MACHINE, RUNNING, null),
        newEvent(DB_MACHINE, STARTING, null),
        newEvent(DB_MACHINE, RUNNING, null));
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsExceptionWhenOneMachineStartFailed() throws Exception {
    final String msg = "container start failed";
    mockInstallersBootstrap();
    mockContainerStartFailed(new InfrastructureException(msg));

    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(starter, times(1))
          .startContainer(
              nullable(String.class),
              nullable(String.class),
              nullable(String.class),
              any(),
              any(),
              any());
      verify(eventService, times(3)).publish(any(MachineStatusEvent.class));
      verifyEventsOrder(
          newEvent(DEV_MACHINE, STARTING, null),
          newEvent(DEV_MACHINE, FAILED, msg),
          newEvent(DEV_MACHINE, STOPPED, null));
      throw ex;
    }
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsExceptionWhenBootstrappingOfInstallersFailed() throws Exception {
    mockInstallersBootstrapFailed(new InfrastructureException("bootstrap failed"));
    mockContainerStart();
    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(starter, times(1))
          .startContainer(
              nullable(String.class),
              nullable(String.class),
              nullable(String.class),
              any(),
              any(),
              any());
      verify(bootstrapper, times(1)).bootstrap(BOOTSTRAPPING_TIMEOUT_MINUTES);
      verify(eventService, times(4)).publish(any(MachineStatusEvent.class));
      verifyEventsOrder(
          newEvent(DEV_MACHINE, STARTING, null),
          newEvent(DEV_MACHINE, RUNNING, null),
          newEvent(DEV_MACHINE, FAILED, "bootstrap failed"),
          newEvent(DEV_MACHINE, STOPPED, null));
      throw ex;
    }
  }

  @Test(expectedExceptions = InternalInfrastructureException.class)
  public void throwsInternalInfrastructureExceptionWhenNetworkCreationInterrupted()
      throws Exception {
    doThrow(InternalInfrastructureException.class)
        .when(networks)
        .createNetwork(nullable(String.class));

    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(starter, never())
          .startContainer(
              nullable(String.class),
              nullable(String.class),
              nullable(String.class),
              any(),
              any(),
              any());
      throw ex;
    }
  }

  @Test(expectedExceptions = RuntimeStartInterruptedException.class)
  public void throwsInterruptionExceptionWhenContainerStartInterrupted() throws Exception {
    mockInstallersBootstrap();
    mockContainerStartFailed(new RuntimeStartInterruptedException(IDENTITY));

    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(starter, times(1))
          .startContainer(
              nullable(String.class),
              nullable(String.class),
              nullable(String.class),
              any(),
              any(),
              any());
      throw ex;
    }
  }

  @Test(expectedExceptions = RuntimeStartInterruptedException.class)
  public void throwsInterruptionExceptionWhenThreadInterruptedOnStarFailedBeforeDestroying()
      throws Exception {
    final String msg = "container start failed";
    mockInstallersBootstrap();
    doAnswer(
            invocationOnMock -> {
              Thread.currentThread().interrupt();
              throw new InfrastructureException(msg);
            })
        .when(starter)
        .startContainer(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            any(DockerContainerConfig.class),
            any(RuntimeIdentity.class),
            any(AbnormalMachineStopHandler.class));

    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(starter, times(1))
          .startContainer(
              nullable(String.class),
              nullable(String.class),
              nullable(String.class),
              any(),
              any(),
              any());
      throw ex;
    }
  }

  @Test
  public void cancelsProbesCheckingOnRuntimeStartFailed() throws Exception {
    mockInstallersBootstrap();
    mockContainerStartFailed(new InfrastructureException(""));

    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(probesScheduler).cancel(IDENTITY.getWorkspaceId());
    }
  }

  @Test
  public void cancelsProbesCheckingOnRuntimeStop() throws Exception {
    dockerRuntime.internalStop(emptyMap());

    verify(probesScheduler).cancel(IDENTITY.getWorkspaceId());
  }

  @Test
  public void schedulesProbesOnServerChecksCall() throws Exception {
    mockInstallersBootstrap();
    mockContainerStart();
    dockerRuntime.start(emptyMap());
    verify(probesScheduler, times(2)).schedule(eq(workspaceProbes), any());

    dockerRuntime.checkServers();

    verify(probesScheduler, times(4)).schedule(eq(workspaceProbes), any());
  }

  @Test
  public void schedulesProbesOnMachineStart() throws Exception {
    mockInstallersBootstrap();
    mockContainerStart();
    WorkspaceProbes m1Probes = mock(WorkspaceProbes.class);
    WorkspaceProbes m2Probes = mock(WorkspaceProbes.class);
    when(workspaceProbesFactory.getProbes(eq(IDENTITY), eq(DB_MACHINE), anyMap()))
        .thenReturn(m1Probes);
    when(workspaceProbesFactory.getProbes(eq(IDENTITY), eq(DEV_MACHINE), any()))
        .thenReturn(m2Probes);

    dockerRuntime.start(emptyMap());

    verify(probesScheduler).schedule(eq(m1Probes), any());
    verify(probesScheduler).schedule(eq(m2Probes), any());
  }

  @Test(dataProvider = "serverProbeReactionProvider")
  public void updatesServerStatusOnProbeResult(
      ProbeStatus probeStatus,
      ServerStatus oldServerStatus,
      boolean serverStatusChanged,
      ServerStatus newServerStatus)
      throws Exception {

    when(dockerMachine.getServers())
        .thenReturn(
            singletonMap(
                SERVER_1, new ServerImpl().withUrl(SERVER_URL).withStatus(oldServerStatus)));
    mockInstallersBootstrap();
    mockContainerStart();
    WorkspaceProbes m1Probes = mock(WorkspaceProbes.class);
    when(workspaceProbesFactory.getProbes(eq(IDENTITY), eq(DB_MACHINE), any()))
        .thenReturn(m1Probes);
    dockerRuntime.start(emptyMap());
    verify(probesScheduler).schedule(eq(m1Probes), probeResultConsumerCaptor.capture());
    Consumer<ProbeResult> resultConsumer = probeResultConsumerCaptor.getValue();

    resultConsumer.accept(
        new ProbeResult(IDENTITY.getWorkspaceId(), DB_MACHINE, SERVER_1, probeStatus));

    if (serverStatusChanged) {
      verify(dockerMachine).setServerStatus(SERVER_1, newServerStatus);
    } else {
      verify(dockerMachine, never()).setServerStatus(eq(SERVER_1), any());
    }
  }

  @DataProvider
  public static Object[][] serverProbeReactionProvider() {
    return new Object[][] {
      {ProbeStatus.FAILED, ServerStatus.RUNNING, true, ServerStatus.STOPPED},
      {ProbeStatus.FAILED, ServerStatus.UNKNOWN, false, null},
      {ProbeStatus.FAILED, null, false, null},
      {ProbeStatus.FAILED, ServerStatus.STOPPED, false, null},
      {ProbeStatus.PASSED, ServerStatus.STOPPED, true, ServerStatus.RUNNING},
      {ProbeStatus.PASSED, ServerStatus.UNKNOWN, true, ServerStatus.RUNNING},
      {ProbeStatus.PASSED, null, true, ServerStatus.RUNNING},
      {ProbeStatus.PASSED, ServerStatus.RUNNING, false, null},
    };
  }

  private void verifyEventsOrder(MachineStatusEvent... expectedEvents) {
    final Iterator<MachineStatusEvent> actualEvents = captureEvents().iterator();
    for (MachineStatusEvent expected : expectedEvents) {
      if (!actualEvents.hasNext()) {
        fail("It is expected to receive machine status events");
      }
      final MachineStatusEvent actual = actualEvents.next();
      assertEquals(actual, expected);
    }
    if (actualEvents.hasNext()) {
      fail("No more events expected");
    }
  }

  private List<MachineStatusEvent> captureEvents() {
    verify(eventService, atLeastOnce()).publish(eventCaptor.capture());
    return eventCaptor.getAllValues();
  }

  private static MachineStatusEvent newEvent(
      String machineName, MachineStatus status, String error) {
    return DtoFactory.newDto(MachineStatusEvent.class)
        .withIdentity(DtoConverter.asDto(IDENTITY))
        .withMachineName(machineName)
        .withEventType(status)
        .withError(error);
  }

  private void mockContainerStart() throws InfrastructureException {
    when(starter.startContainer(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(DockerContainerConfig.class),
            nullable(RuntimeIdentity.class),
            nullable(AbnormalMachineStopHandler.class)))
        .thenReturn(dockerMachine);
  }

  private void mockContainerStartFailed(InfrastructureException exception)
      throws InfrastructureException {
    when(starter.startContainer(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(DockerContainerConfig.class),
            nullable(RuntimeIdentity.class),
            nullable(AbnormalMachineStopHandler.class)))
        .thenThrow(exception);
  }

  private void mockInstallersBootstrap() throws Exception {
    final DockerBootstrapper bootstrapper = mock(DockerBootstrapper.class);
    when(bootstrapperFactory.create(
            nullable(String.class),
            nullable(RuntimeIdentity.class),
            anyList(),
            nullable(DockerMachine.class)))
        .thenReturn(bootstrapper);
    doNothing().when(bootstrapper).bootstrap(BOOTSTRAPPING_TIMEOUT_MINUTES);
  }

  private void mockInstallersBootstrapFailed(InfrastructureException exception) throws Exception {
    when(bootstrapperFactory.create(
            nullable(String.class),
            nullable(RuntimeIdentity.class),
            anyList(),
            nullable(DockerMachine.class)))
        .thenReturn(bootstrapper);
    doThrow(exception).when(bootstrapper).bootstrap(BOOTSTRAPPING_TIMEOUT_MINUTES);
  }

  private InstallerImpl newInstaller(int i) {
    return new InstallerImpl(
        "installer_" + i,
        "installer_name" + i,
        String.valueOf(i) + ".0.0",
        "test installer",
        Collections.emptyList(),
        emptyMap(),
        "echo hello",
        emptyMap());
  }
}
