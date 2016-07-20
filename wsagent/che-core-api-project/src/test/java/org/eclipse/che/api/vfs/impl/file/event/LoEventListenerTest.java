/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.impl.file.event;


import org.eclipse.che.api.core.notification.EventService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link LoEventListener}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@RunWith(MockitoJUnitRunner.class)
public class LoEventListenerTest {
    @Spy
    private EventService       eventService;
    @Mock
    private LoEventQueueHolder loEventQueueHolder;
    @InjectMocks
    private LoEventListener    loEventListener;

    @Before
    public void setUp() throws Exception {
        loEventListener.postConstruct();
        verify(eventService).subscribe(any(LoEventListener.class));
    }

    @After
    public void tearDown() throws Exception {
        loEventListener.preDestroy();
        verify(eventService).unsubscribe(isA(LoEventListener.class));
    }

    @Test
    public void shouldEnqueuePublishedVfsEvent() throws Exception {
        final LoEvent loEvent = mock(LoEvent.class);

        eventService.publish(loEvent);
        verify(loEventQueueHolder).put(eq(loEvent));
    }

    @Test
    public void shouldNotEnqueuePublishedNonVfsEvent() throws Exception {
        eventService.publish(new Object());
        verifyZeroInteractions(loEventQueueHolder);
    }

}
