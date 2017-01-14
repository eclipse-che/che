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
package org.eclipse.che.ide.command.editor.page.settings;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SettingsPage}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class SettingsPageTest {

    private static final String COMMAND_NAME    = "build";
    private static final String COMMAND_GOAL_ID = "build";

    @Mock
    private SettingsPageView              view;
    @Mock
    private AppContext                    appContext;
    @Mock
    private PredefinedCommandGoalRegistry predefinedCommandGoalRegistry;
    @Mock
    private CommandManager                commandManager;
    @Mock
    private EditorMessages                messages;
    @Mock
    private EventBus                      eventBus;

    @InjectMocks
    private SettingsPage page;

    @Mock
    private DirtyStateListener dirtyStateListener;
    @Mock
    private ContextualCommand  editedCommand;
    @Mock
    private ApplicableContext  editedCommandApplicableContext;

    @Before
    public void setUp() throws Exception {
        when(appContext.getProjects()).thenReturn(new Project[0]);

        when(editedCommandApplicableContext.isWorkspaceApplicable()).thenReturn(true);
        when(editedCommand.getName()).thenReturn(COMMAND_NAME);
        when(editedCommand.getApplicableContext()).thenReturn(editedCommandApplicableContext);

        Map<String, String> attributes = new HashMap<>();
        attributes.put(COMMAND_GOAL_ATTRIBUTE_NAME, COMMAND_GOAL_ID);
        when(editedCommand.getAttributes()).thenReturn(attributes);

        page.setDirtyStateListener(dirtyStateListener);
        page.edit(editedCommand);
    }

    @Test
    public void shouldSetViewDelegate() throws Exception {
        verify(view).setDelegate(page);
    }

    @Test
    public void shouldInitializeView() throws Exception {
        verify(predefinedCommandGoalRegistry).getAllGoals();
        verify(view).setAvailableGoals(Matchers.<CommandGoal>anySet());
        verify(view).setGoal(eq(COMMAND_GOAL_ID));
        verify(view).setWorkspace(eq(true));
    }

    @Test
    public void shouldReturnView() throws Exception {
        assertEquals(view, page.getView());
    }

    @Test
    public void shouldNotifyListenerWhenGoalChanged() throws Exception {
        page.onGoalChanged("test");

        verify(dirtyStateListener, times(2)).onDirtyStateChanged();
    }

    @Test
    public void shouldNotifyListenerWhenWorkspaceChanged() throws Exception {
        page.onWorkspaceChanged(true);

        verify(dirtyStateListener, times(2)).onDirtyStateChanged();
    }

    @Test
    public void shouldNotifyListenerWhenApplicableProjectChanged() throws Exception {
        page.onApplicableProjectChanged(mock(Project.class), true);

        verify(dirtyStateListener, times(2)).onDirtyStateChanged();
    }
}
