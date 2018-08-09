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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeStartInterruptedException;
import org.eclipse.che.workspace.infrastructure.kubernetes.event.KubernetesRuntimeStoppedEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.event.KubernetesRuntimeStoppingEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link StartSynchronizer}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class StartSynchronizerTest {
  @Mock private EventService eventService;
  private RuntimeIdentityImpl runtimeId;

  private StartSynchronizer startSynchronizer;

  @BeforeMethod
  public void setUp() {
    runtimeId = new RuntimeIdentityImpl("workspace123", "envName", "ownerId");
    startSynchronizer = new StartSynchronizer(eventService, runtimeId);
  }

  @Test
  public void testSuccessfulStartCompletion() throws Exception {
    // given
    startSynchronizer.setStartThread();

    // when
    startSynchronizer.complete();

    // then
    assertTrue(startSynchronizer.isCompleted());
    assertTrue(startSynchronizer.getStartFailure().isDone());
    assertFalse(startSynchronizer.getStartFailure().isCompletedExceptionally());
  }

  @Test
  public void testFailureStartCompletion() throws Exception {
    // given
    startSynchronizer.setStartThread();
    RuntimeStartInterruptedException expected = new RuntimeStartInterruptedException(runtimeId);

    // when
    startSynchronizer.completeExceptionally(expected);

    // then
    assertTrue(startSynchronizer.isCompleted());
    assertTrue(startSynchronizer.getStartFailure().isCompletedExceptionally());
    try {
      startSynchronizer.getStartFailure().getNow(null);
      fail("Start failure is empty");
    } catch (CompletionException actual) {
      assertEquals(actual.getCause(), expected);
    }
  }

  @Test(expectedExceptions = RuntimeStartInterruptedException.class)
  public void shouldThrowExceptionOnCheckingFailureIfStartIsCompletedExceptionally()
      throws Exception {
    // given
    startSynchronizer.start();
    RuntimeStartInterruptedException expected = new RuntimeStartInterruptedException(runtimeId);
    startSynchronizer.completeExceptionally(expected);

    // when
    startSynchronizer.checkFailure();
  }

  @Test
  public void shouldNotThrowExceptionOnCheckingFailureIfStartIsNotCompleted() throws Exception {
    // given
    startSynchronizer.start();

    // when
    startSynchronizer.checkFailure();
  }

  @Test
  public void shouldNotThrowExceptionOnCheckingFailureIfStartIsCompleted() throws Exception {
    // given
    startSynchronizer.start();
    startSynchronizer.complete();

    // when
    startSynchronizer.checkFailure();
  }

  @Test
  public void shouldInterruptStartThread() throws Exception {
    // given
    startSynchronizer.start();
    startSynchronizer.setStartThread();

    // when
    startSynchronizer.interrupt();

    // then
    assertTrue(Thread.interrupted());
  }

  @Test(
      expectedExceptions = InternalInfrastructureException.class,
      expectedExceptionsMessageRegExp = "Runtime is already started")
  public void shouldNotSetStartThreadIfItIsAlreadySet() throws Exception {
    // given
    startSynchronizer.setStartThread();

    // when
    startSynchronizer.setStartThread();
  }

  @Test
  public void shouldNotInterruptThreadIfItWasNotSet() {
    // given
    startSynchronizer.start();

    // when
    startSynchronizer.interrupt();

    // then
    assertFalse(Thread.interrupted());
  }

  @Test
  public void shouldSubscribeOnRuntimeStoppingAndStoppedEventsWhenStartIsCalled() {
    // when
    startSynchronizer.start();

    // then
    verify(eventService).subscribe(any(), eq(KubernetesRuntimeStoppingEvent.class));
    verify(eventService).subscribe(any(), eq(KubernetesRuntimeStoppedEvent.class));
  }

  @Test(expectedExceptions = RuntimeStartInterruptedException.class)
  public void shouldInterruptStartWhenStoppingEventIsPublished() throws Exception {
    // given
    EventService eventService = new EventService();
    StartSynchronizer localStartSynchronizer = new StartSynchronizer(eventService, runtimeId);
    localStartSynchronizer.start();

    // when
    eventService.publish(new KubernetesRuntimeStoppingEvent("workspace123"));

    // then
    localStartSynchronizer.checkFailure();
  }

  @Test
  public void shouldCompleteStartWhenStoppedEventIsPublished() {
    // given
    EventService eventService = new EventService();
    StartSynchronizer localStartSynchronizer = new StartSynchronizer(eventService, runtimeId);
    localStartSynchronizer.start();

    // when
    eventService.publish(new KubernetesRuntimeStoppedEvent("workspace123"));

    // then
    assertTrue(localStartSynchronizer.isCompleted());
  }

  @Test(expectedExceptions = RuntimeStartInterruptedException.class)
  public void shouldRethrowOriginalExceptionIfStartCompletedExceptionallyOnCompletion()
      throws Exception {
    // given
    startSynchronizer.completeExceptionally(new RuntimeStartInterruptedException(runtimeId));

    // when
    startSynchronizer.complete();
  }

  @Test
  public void shouldAwaitTerminationWhenItIsCompleted() throws Exception {
    // given
    startSynchronizer.complete();

    // when
    boolean isInterrupted = startSynchronizer.awaitInterruption(1, TimeUnit.SECONDS);

    // then
    assertFalse(isInterrupted);
  }

  @Test
  public void shouldAwaitTerminationWhenItIsCompletedExceptionally() throws Exception {
    // given
    startSynchronizer.completeExceptionally(new RuntimeStartInterruptedException(runtimeId));

    // when
    boolean isInterrupted = startSynchronizer.awaitInterruption(1, TimeUnit.SECONDS);

    // then
    assertTrue(isInterrupted);
  }

  @Test
  public void shouldReturnFalseOnAwaitingTerminationWhenItIsNotCompletedInTime() throws Exception {
    // given
    startSynchronizer.start();

    // when
    boolean isInterrupted = startSynchronizer.awaitInterruption(10, TimeUnit.MILLISECONDS);

    // then
    assertFalse(isInterrupted);
  }

  @Test
  public void shouldNotInterruptStartIfItIsNotStarted() {
    // when
    boolean isInterrupted = startSynchronizer.interrupt();

    // then
    assertFalse(isInterrupted);
  }

  @Test
  public void shouldWrapExceptionIntoInternalExcWhenItIsCompletedWithNonInfraException() {
    // given
    RuntimeException toThrow = new RuntimeException("test exception");
    startSynchronizer.completeExceptionally(toThrow);

    // when
    InfrastructureException startFailure = startSynchronizer.getStartFailureNow();

    // then
    assertTrue(startFailure instanceof InternalInfrastructureException);
    assertEquals(startFailure.getCause(), toThrow);
  }

  @Test
  public void shouldReturnStartFailureWhenItIsCompletedExceptionally() {
    // given
    InfrastructureException toThrow = new RuntimeStartInterruptedException(runtimeId);
    startSynchronizer.completeExceptionally(toThrow);

    // when
    InfrastructureException startFailure = startSynchronizer.getStartFailureNow();

    // then
    assertTrue(startFailure instanceof RuntimeStartInterruptedException);
    assertEquals(startFailure, toThrow);
  }

  @Test
  public void shouldReturnNullOnGettingStartFailureWhenItIsNotCompletedExceptionally()
      throws Exception {
    // given
    startSynchronizer.complete();

    // when
    InfrastructureException startFailure = startSynchronizer.getStartFailureNow();

    // then
    assertNull(startFailure);
  }

  @Test
  public void shouldReturnStartFailureWhenItIsNotCompletedYet() {
    // when
    InfrastructureException startFailure = startSynchronizer.getStartFailureNow();

    // then
    assertNull(startFailure);
  }
}
