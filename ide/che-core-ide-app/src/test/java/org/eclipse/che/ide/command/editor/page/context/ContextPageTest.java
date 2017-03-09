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
package org.eclipse.che.ide.command.editor.page.context;

import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ContextPage}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextPageTest {

    @Mock
    private ContextPageView view;
    @Mock
    private EditorMessages  messages;

    @InjectMocks
    private ContextPage page;

    @Mock
    private DirtyStateListener dirtyStateListener;
    @Mock
    private ContextualCommand  editedCommand;
    @Mock
    private ApplicableContext  editedCommandApplicableContext;

    @Before
    public void setUp() throws Exception {
        when(editedCommandApplicableContext.isWorkspaceApplicable()).thenReturn(true);
        when(editedCommand.getApplicableContext()).thenReturn(editedCommandApplicableContext);

        page.setDirtyStateListener(dirtyStateListener);
        page.edit(editedCommand);
    }

    @Test
    public void shouldSetViewDelegate() throws Exception {
        verify(view).setDelegate(page);
    }

    @Test
    public void shouldInitializeView() throws Exception {
        verify(view).setWorkspace(eq(true));
    }

    @Test
    public void shouldReturnView() throws Exception {
        assertEquals(view, page.getView());
    }

    @Test
    public void shouldNotifyListenerWhenWorkspaceChanged() throws Exception {
        page.onWorkspaceChanged(true);

        verify(dirtyStateListener, times(2)).onDirtyStateChanged();
    }
}
