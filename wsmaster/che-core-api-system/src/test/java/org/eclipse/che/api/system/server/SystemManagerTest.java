/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.system.server;

import static org.eclipse.che.api.system.shared.SystemStatus.PREPARING_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.READY_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.RUNNING;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.util.Iterator;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.dto.SystemStatusChangedEventDto;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link SystemManager}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class SystemManagerTest {

  @Mock private ServiceTerminator terminator;

  @Mock private EventService eventService;

  @Captor private ArgumentCaptor<SystemStatusChangedEventDto> eventsCaptor;

  private SystemManager systemManager;

  @BeforeMethod
  public void init() {
    MockitoAnnotations.initMocks(this);
    systemManager = new SystemManager(terminator, eventService);
  }

  @Test
  public void isRunningByDefault() {
    assertEquals(systemManager.getSystemStatus(), RUNNING);
  }

  @Test
  public void servicesAreSuspended() throws Exception {
    systemManager.suspendServices();

    verifySuspendCompleted();
  }

  @Test
  public void servicesAreStopped() throws Exception {
    systemManager.stopServices();

    verifyShutdownCompleted();
  }

  @Test(expectedExceptions = ConflictException.class)
  public void exceptionIsThrownWhenStoppingServicesTwice() throws Exception {
    systemManager.stopServices();
    systemManager.stopServices();
  }

  @Test
  public void shutdownDoesNotFailIfServicesAreAlreadyStopped() throws Exception {
    systemManager.stopServices();
    systemManager.shutdown();

    verifyShutdownCompleted();
  }

  @Test
  public void shutdownStopsServicesIfNotStopped() throws Exception {
    systemManager.shutdown();

    verifySuspendCompleted();
  }

  private void verifyShutdownCompleted() throws InterruptedException {
    verify(terminator, timeout(2000)).terminateAll();
    verifyEvents();
  }

  private void verifySuspendCompleted() throws InterruptedException {
    verify(terminator, timeout(2000)).suspendAll();
    verifyEvents();
  }

  private void verifyEvents() {
    verify(eventService, times(2)).publish(eventsCaptor.capture());
    Iterator<SystemStatusChangedEventDto> eventsIt = eventsCaptor.getAllValues().iterator();
    assertEquals(
        eventsIt.next(),
        newDto(SystemStatusChangedEventDto.class)
            .withPrevStatus(RUNNING)
            .withStatus(PREPARING_TO_SHUTDOWN));
    assertEquals(
        eventsIt.next(),
        newDto(SystemStatusChangedEventDto.class)
            .withPrevStatus(PREPARING_TO_SHUTDOWN)
            .withStatus(READY_TO_SHUTDOWN));
  }
}
