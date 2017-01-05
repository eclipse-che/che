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
package org.eclipse.che.ide.api.action;

import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import javax.ws.rs.NotSupportedException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractPerspectiveActionTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private PerspectiveManager manager;
    @Mock
    private SVGResource        icon;
    @Mock
    private ActionEvent        event;

    private DummyAction dummyAction;

    @Before
    public void setUp() {
        dummyAction = new DummyAction(Arrays.asList(SOME_TEXT), SOME_TEXT, SOME_TEXT, icon);
    }

    @Test
    public void actionShouldBePerformed() {
        Presentation presentation = new Presentation();
        when(event.getPerspectiveManager()).thenReturn(manager);
        when(event.getPresentation()).thenReturn(presentation);
        when(manager.getPerspectiveId()).thenReturn("123");

        dummyAction.update(event);

        verify(event).getPerspectiveManager();
        verify(event).getPresentation();
        verify(manager).getPerspectiveId();
    }

    private class DummyAction extends AbstractPerspectiveAction {

        public DummyAction(@NotNull List<String> activePerspectives,
                           @NotNull String tooltip,
                           @NotNull String description,
                           @NotNull SVGResource icon) {
            super(activePerspectives, tooltip, description, null, icon);
        }

        @Override
        public void updateInPerspective(@NotNull ActionEvent event) {
            throw new NotSupportedException("Method isn't supported in current mode...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            throw new NotSupportedException("Method isn't supported in current mode...");
        }
    }

}