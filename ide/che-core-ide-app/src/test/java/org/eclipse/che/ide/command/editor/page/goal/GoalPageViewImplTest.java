/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.editor.page.goal;

import static org.eclipse.che.ide.command.editor.page.goal.GoalPageViewImpl.CREATE_GOAL_ITEM;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.command.editor.page.goal.GoalPageView.ActionDelegate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Tests for {@link GoalPageViewImpl}. */
@RunWith(GwtMockitoTestRunner.class)
public class GoalPageViewImplTest {

  @Mock private ActionDelegate actionDelegate;

  @InjectMocks private GoalPageViewImpl view;

  @Before
  public void setUp() throws Exception {
    view.setDelegate(actionDelegate);
  }

  @Test
  public void shouldSetAvailableGoals() throws Exception {
    // given
    CommandGoal goal1 = mock(CommandGoal.class);
    when(goal1.getId()).thenReturn("g1");

    CommandGoal goal2 = mock(CommandGoal.class);
    when(goal2.getId()).thenReturn("g2");

    Set<CommandGoal> goals = new HashSet<>();
    goals.add(goal1);
    goals.add(goal2);

    // when
    view.setAvailableGoals(goals);

    // then
    verify(view.goalsList).clear();
    verify(view.goalsList).addItem(eq("g1"));
    verify(view.goalsList).addItem(eq("g2"));
  }

  @Test
  public void shouldSetGoal() throws Exception {
    String goalId = "new goal";

    view.setGoal(goalId);

    verify(view.goalsList).select(eq(goalId));
  }

  @Test
  public void shouldCallOnCreateGoal() throws Exception {
    when(view.goalsList.getValue()).thenReturn(CREATE_GOAL_ITEM);

    view.onGoalChanged(null);

    verify(actionDelegate).onCreateGoal();
  }

  @Test
  public void shouldCallOnGoalChanged() throws Exception {
    String chosenGoalId = "g1";
    when(view.goalsList.getValue()).thenReturn(chosenGoalId);

    view.onGoalChanged(null);

    verify(view.goalsList).getValue();
    verify(actionDelegate).onGoalChanged(eq(chosenGoalId));
  }
}
