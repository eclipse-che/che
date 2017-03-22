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
package org.eclipse.che.ide.command.editor.page.project;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link ProjectsPage}. */
@RunWith(MockitoJUnitRunner.class)
public class ProjectsPageTest {

    @Mock
    private ProjectsPageView view;
    @Mock
    private AppContext       appContext;
    @Mock
    private EditorMessages   messages;
    @Mock
    private EventBus         eventBus;

    @InjectMocks
    private ProjectsPage page;

    @Mock
    private DirtyStateListener dirtyStateListener;
    @Mock
    private CommandImpl        editedCommand;
    @Mock
    private ApplicableContext  editedCommandApplicableContext;

    @Before
    public void setUp() throws Exception {
        when(appContext.getProjects()).thenReturn(new Project[0]);

        when(editedCommand.getApplicableContext()).thenReturn(editedCommandApplicableContext);

        page.setDirtyStateListener(dirtyStateListener);
        page.edit(editedCommand);
    }

    @Test
    public void shouldSetViewDelegate() throws Exception {
        verify(view).setDelegate(page);
    }

    @Test
    public void shouldReturnView() throws Exception {
        assertEquals(view, page.getView());
    }

    @Test
    public void shouldNotifyListenerWhenApplicableProjectChanged() throws Exception {
        page.onApplicableProjectChanged(mock(Project.class), true);

        verify(dirtyStateListener, times(2)).onDirtyStateChanged();
    }
}
