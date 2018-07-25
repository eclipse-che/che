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

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Set;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.ui.listbox.CustomListBox;

/**
 * Implementation of {@link GoalPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class GoalPageViewImpl extends Composite implements GoalPageView {

  @VisibleForTesting static final String CREATE_GOAL_ITEM = "New Command Goal...";

  private static final GoalPageViewImplUiBinder UI_BINDER =
      GWT.create(GoalPageViewImplUiBinder.class);

  @UiField CustomListBox goalsList;

  private ActionDelegate delegate;
  private String lastValue;

  @Inject
  public GoalPageViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setAvailableGoals(Set<CommandGoal> goals) {
    goalsList.clear();
    goals.forEach(g -> goalsList.addItem(g.getId()));

    goalsList.addItem(CREATE_GOAL_ITEM);
  }

  @Override
  public void setGoal(String goalId) {
    goalsList.select(goalId);
    lastValue = goalId;
  }

  @UiHandler({"goalsList"})
  void onGoalChanged(ChangeEvent event) {
    String chosenValue = goalsList.getValue();

    if (chosenValue.equals(CREATE_GOAL_ITEM)) {
      goalsList.select(lastValue);
      delegate.onCreateGoal();
    } else {
      lastValue = chosenValue;
      delegate.onGoalChanged(lastValue);
    }
  }

  @Override
  public void setFocusOnGoal() {
    goalsList.setFocus(true);
  }

  interface GoalPageViewImplUiBinder extends UiBinder<Widget, GoalPageViewImpl> {}
}
