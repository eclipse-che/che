/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableSet;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.RuntimeEventsPublisher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link InconsistentRuntimesDetectorTest}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class InconsistentRuntimesDetectorTest {

  private RuntimeIdentity runtimeId =
      new RuntimeIdentityImpl("workspace1", "envName", "owner1", "infraNamespace");

  @Mock private RuntimeEventsPublisher eventPublisher;
  @Mock private WorkspaceRuntimes workspaceRuntimes;

  @Mock private KubernetesInternalRuntime k8sRuntime;
  @Mock private KubernetesRuntimeContext k8sContext;

  private InconsistentRuntimesDetector inconsistentRuntimesDetector;

  @BeforeMethod
  public void setUp() throws Exception {
    inconsistentRuntimesDetector =
        spy(new InconsistentRuntimesDetector(eventPublisher, workspaceRuntimes));
    lenient().when(k8sRuntime.getContext()).thenReturn(k8sContext);
    lenient().when(k8sContext.getIdentity()).thenReturn(runtimeId);

    lenient().when(k8sRuntime.getStatus()).thenReturn(WorkspaceStatus.RUNNING);
  }

  @Test
  public void shouldCheckRuntimesConsistencyOneByOne() throws Exception {
    // given
    when(workspaceRuntimes.getRunning())
        .thenReturn(ImmutableSet.of("workspace1", "workspace2", "workspace3"));
    doNothing().when(inconsistentRuntimesDetector).checkOne(any());

    // when
    inconsistentRuntimesDetector.check();

    // then
    verify(inconsistentRuntimesDetector).checkOne("workspace1");
    verify(inconsistentRuntimesDetector).checkOne("workspace2");
    verify(inconsistentRuntimesDetector).checkOne("workspace3");
  }

  @Test
  public void shouldCheckRuntimesConsistencyOneByOneWhenExceptionOccursOnChecking()
      throws Exception {
    // given
    when(workspaceRuntimes.getRunning())
        .thenReturn(ImmutableSet.of("workspace1", "workspace2", "workspace3"));
    doThrow(new InfrastructureException("error"))
        .when(inconsistentRuntimesDetector)
        .checkOne(any());

    // when
    inconsistentRuntimesDetector.check();

    // then
    verify(inconsistentRuntimesDetector).checkOne("workspace1");
    verify(inconsistentRuntimesDetector).checkOne("workspace2");
    verify(inconsistentRuntimesDetector).checkOne("workspace3");
  }

  @Test
  public void shouldDoNothingIfRuntimeHasConsistentStateOnChecking() throws Exception {
    // given
    doReturn(k8sRuntime).when(workspaceRuntimes).getInternalRuntime("workspace1");
    doReturn(true).when(k8sRuntime).isConsistent();

    // when
    inconsistentRuntimesDetector.checkOne("workspace1");

    // then
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Fetched internal runtime 'workspace1:owner1' is not Kubernetes, it is not possible to check consistency.")
  public void shouldThrowExceptionIfNonK8sRuntimeIsReturnOnCheckingConsistency() throws Exception {
    // given
    InternalRuntime runtime = mock(InternalRuntime.class);
    when(runtime.getContext()).thenReturn(k8sContext);
    doReturn(runtime).when(workspaceRuntimes).getInternalRuntime("workspace1");

    // when
    inconsistentRuntimesDetector.checkOne("workspace1");

    // then
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Failed to get internal runtime for workspace `workspace1` to check consistency. Cause: error")
  public void shouldThrowExceptionIfExceptionOccursDuringRuntimeFetchingOnCheckingConsistency()
      throws Exception {
    // given
    doThrow(new InfrastructureException("error"))
        .when(workspaceRuntimes)
        .getInternalRuntime("workspace1");

    // when
    inconsistentRuntimesDetector.checkOne("workspace1");

    // then
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred during runtime 'workspace1:owner1' consistency checking. Cause: error")
  public void shouldThrowExceptionIfExceptionOccursOnCheckingConsistency() throws Exception {
    // given
    doReturn(k8sRuntime).when(workspaceRuntimes).getInternalRuntime("workspace1");
    doThrow(new InfrastructureException("error")).when(k8sRuntime).isConsistent();

    // when
    inconsistentRuntimesDetector.checkOne("workspace1");

    // then
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void
      shouldMarkRuntimeAsStoppedEvenWhenExceptionOccursDuringRuntimeStoppingOnCheckingConsistency()
          throws Exception {
    // given
    doReturn(k8sRuntime).when(workspaceRuntimes).getInternalRuntime("workspace1");
    doReturn(false).when(k8sRuntime).isConsistent();
    doThrow(new InfrastructureException("error")).when(k8sRuntime).stop(any());

    // when
    InfrastructureException caughtException = null;
    try {
      inconsistentRuntimesDetector.checkOne("workspace1");
    } catch (InfrastructureException e) {
      caughtException = e;
    }

    // then
    assertNotNull(caughtException);
    assertEquals(
        caughtException.getMessage(),
        "Failed to stop the runtime 'workspace1:owner1' which has inconsistent state. Error: error");
    verify(k8sRuntime).stop(emptyMap());
    verify(eventPublisher)
        .sendAbnormalStoppingEvent(runtimeId, "The runtime has inconsistent state.");
    verify(eventPublisher)
        .sendAbnormalStoppedEvent(runtimeId, "The runtime has inconsistent state.");
  }

  @Test
  public void shouldStopRuntimeAbnormallyIfRuntimeHasInConsistentStateOnChecking()
      throws Exception {
    // given
    doReturn(k8sRuntime).when(workspaceRuntimes).getInternalRuntime("workspace1");
    doReturn(false).when(k8sRuntime).isConsistent();

    // when
    inconsistentRuntimesDetector.checkOne("workspace1");

    // then
    verify(k8sRuntime).stop(emptyMap());
    verify(eventPublisher)
        .sendAbnormalStoppingEvent(runtimeId, "The runtime has inconsistent state.");
    verify(eventPublisher)
        .sendAbnormalStoppedEvent(runtimeId, "The runtime has inconsistent state.");
  }

  @Test
  public void shouldNotStopRuntimeAbnormallyIfRuntimeHasInconsistentStateButIsNotRunningAnyMore()
      throws Exception {
    // given
    doReturn(k8sRuntime).when(workspaceRuntimes).getInternalRuntime("workspace1");
    doReturn(false).when(k8sRuntime).isConsistent();
    when(k8sRuntime.getStatus()).thenReturn(WorkspaceStatus.STOPPING);

    // when
    inconsistentRuntimesDetector.checkOne("workspace1");

    // then
    verify(k8sRuntime, never()).stop(emptyMap());
    verify(eventPublisher, never())
        .sendAbnormalStoppingEvent(runtimeId, "The runtime has inconsistent state.");
    verify(eventPublisher, never())
        .sendAbnormalStoppedEvent(runtimeId, "The runtime has inconsistent state.");
  }
}
