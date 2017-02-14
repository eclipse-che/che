/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.system.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.eclipse.che.api.system.shared.SystemStatus.PREPARING_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.READY_TO_SHUTDOWN;
import static org.eclipse.che.api.system.shared.SystemStatus.RUNNING;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link SystemManager}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class SystemManagerTest {

    @Mock
    private ServiceTerminator terminator;

    @Mock
    private EventService eventService;

    @Captor
    private ArgumentCaptor<SystemStatusChangedEvent> eventsCaptor;

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

        verifyShutdownCompleted();
    }

    private void verifyShutdownCompleted() throws InterruptedException {
        verify(terminator, timeout(2000)).terminateAll();
        verify(eventService, times(2)).publish(eventsCaptor.capture());
        Iterator<SystemStatusChangedEvent> eventsIt = eventsCaptor.getAllValues().iterator();
        assertEquals(eventsIt.next(), new SystemStatusChangedEvent(RUNNING, PREPARING_TO_SHUTDOWN));
        assertEquals(eventsIt.next(), new SystemStatusChangedEvent(PREPARING_TO_SHUTDOWN, READY_TO_SHUTDOWN));
    }
}
