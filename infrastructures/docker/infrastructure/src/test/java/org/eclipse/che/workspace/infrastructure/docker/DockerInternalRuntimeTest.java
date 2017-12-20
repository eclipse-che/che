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
package org.eclipse.che.workspace.infrastructure.docker;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.FAILED;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.runtime.MachineStatus.STOPPED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.hc.ServersChecker;
import org.eclipse.che.api.workspace.server.hc.ServersCheckerFactory;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
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
import org.testng.annotations.Test;

/**
 * Tests {@link DockerInternalRuntime}.
 *
 * @author Anton Korneta
 */
public class DockerInternalRuntimeTest {

  private static final RuntimeIdentity IDENTITY = new RuntimeIdentityImpl("ws1", "env1", "usr1");
  private static final String DEV_MACHINE = "DEV_MACHINE";
  private static final String DB_MACHINE = "DB_MACHINE";

  @Mock private DockerBootstrapperFactory bootstrapperFactory;
  @Mock private DockerRuntimeContext runtimeContext;
  @Mock private EventService eventService;
  @Mock private DockerMachineStarter starter;
  @Mock private NetworkLifecycle networks;
  @Mock private DockerBootstrapper bootstrapper;

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
            mock(MachineLoggersFactory.class));
  }

  @Test
  public void startsDockerRuntimeAndPropagatesMachineStatusEvents() throws Exception {
    mockInstallersBootstrap();
    mockContainerStart();
    dockerRuntime.start(emptyMap());

    verify(starter, times(2))
        .startContainer(nullable(String.class), nullable(String.class), any(), any(), any());
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
          .startContainer(nullable(String.class), nullable(String.class), any(), any(), any());
      verify(eventService, times(2)).publish(any(MachineStatusEvent.class));
      verifyEventsOrder(newEvent(DEV_MACHINE, STARTING, null), newEvent(DEV_MACHINE, FAILED, msg));
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
          .startContainer(nullable(String.class), nullable(String.class), any(), any(), any());
      verify(bootstrapper, times(1)).bootstrap();
      verify(eventService, times(3)).publish(any(MachineStatusEvent.class));
      verifyEventsOrder(
          newEvent(DEV_MACHINE, STARTING, null),
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
          .startContainer(nullable(String.class), nullable(String.class), any(), any(), any());
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
          .startContainer(nullable(String.class), nullable(String.class), any(), any(), any());
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
            any(DockerContainerConfig.class),
            any(RuntimeIdentity.class),
            any(AbnormalMachineStopHandler.class));

    try {
      dockerRuntime.start(emptyMap());
    } catch (InfrastructureException ex) {
      verify(starter, times(1))
          .startContainer(nullable(String.class), nullable(String.class), any(), any(), any());
      throw ex;
    }
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
            nullable(DockerContainerConfig.class),
            nullable(RuntimeIdentity.class),
            nullable(AbnormalMachineStopHandler.class)))
        .thenReturn(mock(DockerMachine.class));
  }

  private void mockContainerStartFailed(InfrastructureException exception)
      throws InfrastructureException {
    when(starter.startContainer(
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
    doNothing().when(bootstrapper).bootstrap();
  }

  private void mockInstallersBootstrapFailed(InfrastructureException exception) throws Exception {
    when(bootstrapperFactory.create(
            nullable(String.class),
            nullable(RuntimeIdentity.class),
            anyList(),
            nullable(DockerMachine.class)))
        .thenReturn(bootstrapper);
    doThrow(exception).when(bootstrapper).bootstrap();
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
