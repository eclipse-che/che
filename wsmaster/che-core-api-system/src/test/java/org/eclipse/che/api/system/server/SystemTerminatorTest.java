/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.system.server;

import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SuspendingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceSuspendedEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ServiceTerminator}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class SystemTerminatorTest {

  @Mock private EventService eventService;
  @Mock private ServiceTermination termination1;
  @Mock private ServiceTermination termination2;

  private ServiceTerminator terminator;

  @BeforeMethod
  public void setUp() {
    when(termination1.getServiceName()).thenReturn("service1");
    when(termination2.getServiceName()).thenReturn("service2");
    terminator = new ServiceTerminator(eventService, ImmutableSet.of(termination1, termination2));
  }

  @Test
  public void executesTerminations() throws Exception {
    terminator.terminateAll();

    verify(termination1).terminate();
    verify(termination2).terminate();
    verify(eventService).publish(new StoppingSystemServiceEvent("service1"));
    verify(eventService).publish(new SystemServiceStoppedEvent("service1"));
    verify(eventService).publish(new StoppingSystemServiceEvent("service2"));
    verify(eventService).publish(new SystemServiceStoppedEvent("service2"));
  }

  @Test
  public void executesSuspendals() throws Exception {
    terminator.suspendAll();

    verify(termination1).suspend();
    verify(termination2).suspend();
    verify(eventService).publish(new SuspendingSystemServiceEvent("service1"));
    verify(eventService).publish(new SystemServiceSuspendedEvent("service1"));
    verify(eventService).publish(new SuspendingSystemServiceEvent("service2"));
    verify(eventService).publish(new SystemServiceSuspendedEvent("service2"));
  }

  @Test
  public void executesTermitationsWhenSuspendalsNotSupported() throws Exception {

    doThrow(UnsupportedOperationException.class).when(termination1).suspend();
    terminator.suspendAll();

    verify(termination1).terminate();
    verify(termination2).suspend();

    verify(eventService).publish(new SuspendingSystemServiceEvent("service1"));
    verify(eventService).publish(new StoppingSystemServiceEvent("service1"));
    verify(eventService).publish(new SystemServiceStoppedEvent("service1"));
    verify(eventService).publish(new SuspendingSystemServiceEvent("service2"));
    verify(eventService).publish(new SystemServiceSuspendedEvent("service2"));
  }

  @Test(
    expectedExceptions = InterruptedException.class,
    expectedExceptionsMessageRegExp = "interrupt!"
  )
  public void stopsExecutingTerminationIfOneIsInterrupted() throws Exception {
    doThrow(new InterruptedException("interrupt!")).when(termination1).terminate();

    terminator.terminateAll();
  }

  @Test(dataProvider = "dependableTerminations")
  public void shouldOrderTerminationsByDependency(
      Set<ServiceTermination> terminations, Set<String> expectedOrder) throws Exception {
    ServiceTerminator localTerminator = spy(new ServiceTerminator(eventService, terminations));
    localTerminator.suspendAll();
    ArgumentCaptor<ServiceTermination> captor = ArgumentCaptor.forClass(ServiceTermination.class);
    verify(localTerminator, times(terminations.size())).doTerminate(captor.capture());
    assertEquals(
        captor.getAllValues().stream().map(ServiceTermination::getServiceName).collect(toSet()),
        expectedOrder);
  }

  @Test(
    dataProvider = "loopableTerminations",
    expectedExceptions = RuntimeException.class,
    expectedExceptionsMessageRegExp = "Circular dependency found between terminations \\[B, D\\]"
  )
  public void shouldFailOnCyclicDependency(Set<ServiceTermination> terminations) throws Exception {
    new ServiceTerminator(eventService, terminations);
  }

  @Test(
    dataProvider = "sameNameTerminations",
    expectedExceptions = RuntimeException.class,
    expectedExceptionsMessageRegExp = "Duplicate termination found with service name .+"
  )
  public void shouldFailOnTerminationsWithSameServiceName(Set<ServiceTermination> terminations)
      throws Exception {
    new ServiceTerminator(eventService, terminations);
  }

  @Test(
    dataProvider = "wrongDependencyTerminations",
    expectedExceptions = RuntimeException.class,
    expectedExceptionsMessageRegExp = "Unknown dependency found in termination .+"
  )
  public void shouldFailOnTerminationsWithUnexistingDeps(Set<ServiceTermination> terminations)
      throws Exception {
    new ServiceTerminator(eventService, terminations);
  }

  @DataProvider(name = "dependableTerminations")
  public Object[][] dependableTerminations() {
    return new Object[][] {
      {
        ImmutableSet.of(
            getServiceTerminationWithDependency("A", Collections.emptySet()),
            getServiceTerminationWithDependency("B", ImmutableSet.of("C", "D", "G")),
            getServiceTerminationWithDependency("C", Collections.emptySet()),
            getServiceTerminationWithDependency("D", ImmutableSet.of("C")),
            getServiceTerminationWithDependency("E", ImmutableSet.of("B")),
            getServiceTerminationWithDependency("F", ImmutableSet.of("C")),
            getServiceTerminationWithDependency("G", ImmutableSet.of("C"))),
        ImmutableSet.of("A", "C", "D", "F", "G", "B", "E")
      }
    };
  }

  @DataProvider(name = "loopableTerminations")
  public Object[][] loopableTerminations() {
    return new Object[][] {
      {
        ImmutableSet.of(
            getServiceTerminationWithDependency("A", Collections.emptySet()),
            getServiceTerminationWithDependency("B", ImmutableSet.of("C", "D")),
            getServiceTerminationWithDependency("C", Collections.emptySet()),
            getServiceTerminationWithDependency("D", ImmutableSet.of("B")) // loop here
            )
      }
    };
  }

  @DataProvider(name = "sameNameTerminations")
  public Object[][] sameNameTerminations() {
    return new Object[][] {
      {
        ImmutableSet.of(
            getServiceTerminationWithDependency("A", Collections.emptySet()),
            getServiceTerminationWithDependency("C", Collections.emptySet()),
            getServiceTerminationWithDependency("C", Collections.emptySet()))
      }
    };
  }

  @DataProvider(name = "wrongDependencyTerminations")
  public Object[][] wrongDependencyTerminations() {
    return new Object[][] {
      {
        ImmutableSet.of(
            getServiceTerminationWithDependency("A", Collections.emptySet()),
            // no such termination
            getServiceTerminationWithDependency("C", ImmutableSet.of("B")))
      }
    };
  }

  private ServiceTermination getServiceTerminationWithDependency(
      String name, Set<String> depencencies) {
    return new ServiceTermination() {
      @Override
      public void terminate() throws InterruptedException {}

      @Override
      public String getServiceName() {
        return name;
      }

      @Override
      public Set<String> getDependencies() {
        return depencencies;
      }
    };
  }
}
