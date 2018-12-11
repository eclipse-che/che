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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;

/**
 * {@link CommandEditorPage} which allows to edit command's goal.
 *
 * @author Artem Zatsarynnyi
 */
public class GoalPage extends AbstractCommandEditorPage implements GoalPageView.ActionDelegate {

  private final GoalPageView view;
  private final CommandGoalRegistry goalRegistry;
  private final EditorMessages messages;
  private final DialogFactory dialogFactory;

  /** Initial value of the command's goal. */
  private String initialGoal;

  @Inject
  public GoalPage(
      GoalPageView view,
      CommandGoalRegistry goalRegistry,
      EditorMessages messages,
      DialogFactory dialogFactory) {
    super(messages.pageGoalTitle());

    this.view = view;
    this.goalRegistry = goalRegistry;
    this.messages = messages;
    this.dialogFactory = dialogFactory;

    view.setDelegate(this);
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  protected void initialize() {
    String goal = editedCommand.getGoal();
    initialGoal = isNullOrEmpty(goal) ? goalRegistry.getDefaultGoal().getId() : goal;

    view.setAvailableGoals(goalRegistry.getAllGoals());
    view.setGoal(initialGoal);
  }

  @Override
  public boolean isDirty() {
    if (editedCommand == null) {
      return false;
    }

    return !(initialGoal.equals(editedCommand.getGoal()));
  }

  @Override
  public void onGoalChanged(String goalId) {
    editedCommand.setGoal(goalId);
    notifyDirtyStateChanged();
  }

  @Override
  public void onCreateGoal() {
    createGoal("");
  }

  /**
   * Asks user for the the new goal name nad creates it if another one with the same name doesn't
   * exist.
   */
  private void createGoal(String initialName) {
    final InputCallback inputCallback =
        value -> {
          final String newGoalName = value.trim();

          final Set<CommandGoal> allGoals = goalRegistry.getAllGoals();

          final Optional<CommandGoal> existingGoal =
              allGoals
                  .stream()
                  .filter(goal -> goal.getId().equalsIgnoreCase(newGoalName))
                  .findAny();

          if (existingGoal.isPresent()) {
            dialogFactory
                .createMessageDialog(
                    messages.pageGoalNewGoalTitle(),
                    messages.pageGoalNewGoalAlreadyExistsMessage(existingGoal.get().getId()),
                    () -> createGoal(newGoalName))
                .show();
          } else {
            setGoal(newGoalName);
          }
        };

    dialogFactory
        .createInputDialog(
            messages.pageGoalNewGoalTitle(),
            messages.pageGoalNewGoalLabel(),
            initialName,
            0,
            initialName.length(),
            messages.pageGoalNewGoalButtonCreate(),
            inputCallback,
            null)
        .show();
  }

  /** Set the specified goal name for the currently edited command. */
  private void setGoal(String goalName) {
    editedCommand.setGoal(goalName);

    Set<CommandGoal> allGoals = goalRegistry.getAllGoals();
    allGoals.add(goalRegistry.getGoalForId(goalName));

    view.setAvailableGoals(allGoals);
    view.setGoal(goalName);

    notifyDirtyStateChanged();
  }

  @Override
  public void focus() {
    view.setFocusOnGoal();
  }
}
