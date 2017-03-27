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

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import java.util.Set;

/**
 * {@link CommandEditorPage} which allows to edit command's goal.
 *
 * @author Artem Zatsarynnyi
 */
public class GoalPage extends AbstractCommandEditorPage implements GoalPageView.ActionDelegate {

    private final GoalPageView        view;
    private final CommandGoalRegistry goalRegistry;
    private final EditorMessages      messages;
    private final DialogFactory       dialogFactory;

    /** Initial value of the command's goal. */
    private String goalInitial;

    @Inject
    public GoalPage(GoalPageView view,
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
        final String goalId = editedCommand.getGoal();
        final CommandGoal goal = goalRegistry.getGoalForId(goalId);

        goalInitial = goal.getId();

        view.setAvailableGoals(goalRegistry.getAllGoals());
        view.setGoal(goal.getId());
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        CommandGoal goal = goalRegistry.getGoalForId(editedCommand.getGoal());

        return !(goalInitial.equals(goal.getId()));
    }

    @Override
    public void onGoalChanged(String goalId) {
        editedCommand.setGoal(goalId);
        notifyDirtyStateChanged();
    }

    @Override
    public void onCreateGoal() {
        InputCallback inputCallback = value -> {
            Set<CommandGoal> goals = goalRegistry.getAllGoals();
            goals.add(goalRegistry.getGoalForId(value));

            view.setAvailableGoals(goals);
            view.setGoal(value);

            editedCommand.setGoal(value);
            notifyDirtyStateChanged();
        };

        dialogFactory.createInputDialog(messages.pageGoalNewGoalTitle(),
                                        messages.pageGoalNewGoalLabel(),
                                        "",
                                        0,
                                        0,
                                        messages.pageGoalNewGoalButtonCreate(),
                                        inputCallback,
                                        null).show();
    }
}
