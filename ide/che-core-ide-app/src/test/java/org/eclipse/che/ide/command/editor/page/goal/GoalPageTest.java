/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.editor.page.goal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link GoalPage}. */
@RunWith(MockitoJUnitRunner.class)
public class GoalPageTest {

  private static final String COMMAND_GOAL_ID = "build";

  @Mock private GoalPageView view;
  @Mock private CommandGoalRegistry goalRegistry;
  @Mock private EditorMessages messages;
  @Mock private DialogFactory dialogFactory;

  @InjectMocks private GoalPage page;

  @Mock private DirtyStateListener dirtyStateListener;
  @Mock private CommandImpl editedCommand;
  @Mock private ApplicableContext editedCommandApplicableContext;

  @Before
  public void setUp() throws Exception {
    CommandGoal goal = mock(CommandGoal.class);
    when(goal.getId()).thenReturn(COMMAND_GOAL_ID);
    when(goalRegistry.getGoalForId(anyString())).thenReturn(goal);

    when(editedCommand.getApplicableContext()).thenReturn(editedCommandApplicableContext);
    when(editedCommand.getGoal()).thenReturn(COMMAND_GOAL_ID);

    page.setDirtyStateListener(dirtyStateListener);
    page.edit(editedCommand);
  }

  @Test
  public void shouldSetViewDelegate() throws Exception {
    verify(view).setDelegate(page);
  }

  @Test
  public void shouldInitializeView() throws Exception {
    verify(goalRegistry).getAllGoals();
    verify(view).setAvailableGoals(anySet());
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

  @SuppressWarnings("unchecked")
  @Test
  public void shouldSetDefaultGoalIfInitialIsNull() throws Exception {
    reset(goalRegistry);
    reset(view);

    String defaultGoalId = "Common";
    CommandGoal defaultGoal = mock(CommandGoal.class);
    when(goalRegistry.getDefaultGoal()).thenReturn(defaultGoal);
    when(defaultGoal.getId()).thenReturn(defaultGoalId);
    when(editedCommand.getGoal()).thenReturn(null);

    page.initialize();

    verify(goalRegistry).getAllGoals();
    verify(view).setAvailableGoals(anySet());
    verify(view).setGoal(defaultGoalId);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldSetDefaultGoalIfInitialIsEmpty() throws Exception {
    reset(goalRegistry);
    reset(view);

    String defaultGoalId = "Common";
    CommandGoal defaultGoal = mock(CommandGoal.class);
    when(goalRegistry.getDefaultGoal()).thenReturn(defaultGoal);
    when(defaultGoal.getId()).thenReturn(defaultGoalId);
    when(editedCommand.getGoal()).thenReturn("");

    page.initialize();

    verify(goalRegistry).getAllGoals();
    verify(view).setAvailableGoals(anySet());
    verify(view).setGoal(defaultGoalId);
  }

  @Test
  public void shouldCreateGoal() throws Exception {
    // given
    InputDialog inputDialog = mock(InputDialog.class);
    when(dialogFactory.createInputDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            eq(0),
            eq(0),
            nullable(String.class),
            nullable(InputCallback.class),
            nullable(CancelCallback.class)))
        .thenReturn(inputDialog);
    String newGoalId = "new goal";

    // when
    page.onCreateGoal();

    // then
    ArgumentCaptor<InputCallback> inputCaptor = ArgumentCaptor.forClass(InputCallback.class);
    verify(dialogFactory)
        .createInputDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            eq(0),
            eq(0),
            nullable(String.class),
            inputCaptor.capture(),
            isNull());
    verify(inputDialog).show();
    inputCaptor.getValue().accepted(newGoalId);
    verify(view).setGoal(eq(newGoalId));
    verify(editedCommand).setGoal(eq(newGoalId));
    verify(dirtyStateListener, times(2)).onDirtyStateChanged();
  }
}
