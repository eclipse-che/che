/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.editor.page.goal;

import java.util.Set;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link GoalPage}.
 *
 * @author Artem Zatsarynnyi
 */
public interface GoalPageView extends View<GoalPageView.ActionDelegate> {

  /** Set the list of goals which are available to set for command. */
  void setAvailableGoals(Set<CommandGoal> goals);

  /** Sets the command's goal value. */
  void setGoal(String goalId);

  /** Sets the focus on goal field. */
  void setFocusOnGoal();

  /** The action delegate for this view. */
  interface ActionDelegate {

    /**
     * Called when command goal has been changed.
     *
     * @param goalId new value of the command goal
     */
    void onGoalChanged(String goalId);

    /** Called when creating new goal is requested. */
    void onCreateGoal();
  }
}
