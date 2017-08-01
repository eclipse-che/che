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

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.system.shared.event.service.StoppingSystemServiceEvent;
import org.eclipse.che.api.system.shared.event.service.SystemServiceStoppedEvent;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ServiceTerminator}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class SystemTerminatorTest {

    @Mock
    private EventService       eventService;
    @Mock
    private ServiceTermination termination1;
    @Mock
    private ServiceTermination termination2;

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

    @Test(expectedExceptions = InterruptedException.class, expectedExceptionsMessageRegExp = "interrupt!")
    public void stopsExecutingTerminationIfOneIsInterrupted() throws Exception {
        doThrow(new InterruptedException("interrupt!")).when(termination1).terminate();

        terminator.terminateAll();
    }
}
