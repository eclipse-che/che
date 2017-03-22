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
package org.eclipse.che.ide.command.palette;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Tests for {@link ShowCommandsPaletteAction}. */
@RunWith(MockitoJUnitRunner.class)
public class ShowCommandsPaletteActionTest {

    @Mock
    private PaletteMessages          messages;
    @Mock
    private CommandsPalettePresenter presenter;

    @InjectMocks
    private ShowCommandsPaletteAction action;

    @Test
    public void shouldInitializeAction() {
        verify(messages).actionShowPaletteTitle();
        verify(messages).actionShowPaletteDescription();
    }

    @Test
    public void shouldShowDialog() {
        action.actionPerformed(mock(ActionEvent.class));

        verify(presenter).showDialog();
    }
}
