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
package org.eclipse.che.ide.selection;

import com.google.gwt.junit.GWTMockUtilities;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedEvent;
import org.eclipse.che.ide.api.event.SelectionChangedHandler;
import org.eclipse.che.ide.api.parts.AbstractPartPresenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.selection.Selection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Test covers {@link SelectionAgentImpl} functionality.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSelectionAgent {

    private EventBus eventBus = new SimpleEventBus();

    private SelectionAgentImpl agent = new SelectionAgentImpl(eventBus);

    @Mock
    private PartPresenter part;

    @Mock
    private Selection selection;

    @Before
    public void disarm() {
        // don't throw an exception if GWT.create() invoked
        GWTMockUtilities.disarm();
    }

    @After
    public void restore() {
        GWTMockUtilities.restore();
    }

    /** Check proper Selection returned when part changed */
    @Test
    public void shouldChangeSelectionAfterPartGetsActivated() {
        when(part.getSelection()).thenReturn(selection);

        // fire event, for agent to get information about active part
        eventBus.fireEvent(new ActivePartChangedEvent(part));

        assertEquals("Agent should return proper Selection", selection, agent.getSelection());
    }

    /** Event should be fired, when active part changed */
    @Test
    public void shouldFireEventWhenPartChanged() {
        when(part.getSelection()).thenReturn(selection);
        SelectionChangedHandler handler = mock(SelectionChangedHandler.class);
        eventBus.addHandler(SelectionChangedEvent.TYPE, handler);

        // fire event, for agent to get information about active part
        eventBus.fireEvent(new ActivePartChangedEvent(part));

        verify(handler).onSelectionChanged((SelectionChangedEvent)any());
    }

    /**
     * If selection chang in active part, Selection Agent should fire
     * an Event
     */
    @Test
    public void shouldFireEventWhenSelectionInActivePartChanged() {

        AbstractPartPresenter part = new AbstractPartPresenter() {
            @Override
            public void go(AcceptsOneWidget container) {
            }

            @Override
            public String getTitleToolTip() {
                return null;
            }

            @Override
            public SVGResource getTitleImage() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public IsWidget getView() {
                return null;
            }
        };

        // fire event, for agent to get information about active part
        eventBus.fireEvent(new ActivePartChangedEvent(part));
        SelectionChangedHandler handler = mock(SelectionChangedHandler.class);
        eventBus.addHandler(SelectionChangedEvent.TYPE, handler);

        part.setSelection(mock(Selection.class));

        verify(handler).onSelectionChanged((SelectionChangedEvent)any());
    }

    /**
     * If selection chang in non-active part, no events should be fired by
     * Selection Agent
     */
    @Test
    public void shouldNOTFireEventWhenSelectionInNONActivePartChanged() {

        AbstractPartPresenter firstPart = new AbstractPartPresenter() {
            @Override
            public void go(AcceptsOneWidget container) {
            }

            @Override
            public String getTitleToolTip() {
                return null;
            }

            @Override
            public SVGResource getTitleImage() {
                return null;
            }

            @Override
            public String getTitle() {
                return null;
            }

            @Override
            public IsWidget getView() {
                return null;
            }
        };

        // fire event, for agent to get information about active part
        eventBus.fireEvent(new ActivePartChangedEvent(firstPart));
        // change part
        eventBus.fireEvent(new ActivePartChangedEvent(mock(PartPresenter.class)));

        SelectionChangedHandler handler = mock(SelectionChangedHandler.class);
        eventBus.addHandler(SelectionChangedEvent.TYPE, handler);

        // call setSelection on the first Part.
        firstPart.setSelection(mock(Selection.class));

        verifyZeroInteractions(handler);
    }

}
