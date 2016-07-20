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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.Thread.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link HiEventService}
 *
 * @author Dmitry Kuleshov
 *
 * @since 4.5
 */
@RunWith(MockitoJUnitRunner.class)
public class HiEventServiceTest {
    @Mock
    private HiEventDetectorManager    hiEventDetectorManager;
    @Mock
    private HiEventBroadcasterManager hiEventBroadcasterManager;
    @Spy
    private EventTreeQueueHolder      eventTreeQueueHolder;
    @InjectMocks
    private HiEventService            hiEventService;

    @Before
    public void setUp() throws Exception {
        hiEventService.postConstruct();
    }

    @After
    public void tearDown() throws Exception {
        hiEventService.preDestroy();
    }

    @Test
    public void shouldInvokeAllComponents() throws Exception {
        final EventTreeNode eventTreeNode = mock(EventTreeNode.class);

        eventTreeQueueHolder.put(eventTreeNode);

        sleep(1000);

        verify(eventTreeQueueHolder, atLeastOnce()).take();
        verify(hiEventDetectorManager).getDetectedEvents(eq(eventTreeNode));
        verify(hiEventBroadcasterManager).manageEvents(any());
    }

}
