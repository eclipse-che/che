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
package org.eclipse.che.ide.command.editor.page.goal;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link GoalPage}. */
@RunWith(MockitoJUnitRunner.class)
public class GoalPageTest {

    private static final String COMMAND_GOAL_ID = "build";

    @Mock
    private GoalPageView        view;
    @Mock
    private CommandGoalRegistry goalRegistry;
    @Mock
    private CommandManager      commandManager;
    @Mock
    private EditorMessages      messages;

    @InjectMocks
    private GoalPage page;

    @Mock
    private DirtyStateListener dirtyStateListener;
    @Mock
    private CommandImpl        editedCommand;
    @Mock
    private ApplicableContext  editedCommandApplicableContext;

    @Before
    public void setUp() throws Exception {
        CommandGoal commandGoal = mock(CommandGoal.class);
        when(commandGoal.getId()).thenReturn(COMMAND_GOAL_ID);
        when(goalRegistry.getGoalForId(anyString())).thenReturn(commandGoal);

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
        verify(goalRegistry).getAllPredefinedGoals();
        verify(view).setAvailableGoals(Matchers.<CommandGoal>anySet());
        verify(view).setGoal(eq(COMMAND_GOAL_ID));
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
}
