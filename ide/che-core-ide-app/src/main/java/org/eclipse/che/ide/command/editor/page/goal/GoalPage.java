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

import org.eclipse.che.ide.api.command.BaseCommandGoal;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * {@link CommandEditorPage} which allows to edit command's goal.
 *
 * @author Artem Zatsarynnyi
 */
public class GoalPage extends AbstractCommandEditorPage implements GoalPageView.ActionDelegate {

    private final GoalPageView        view;
    private final CommandGoalRegistry goalRegistry;
    private final CommandManager      commandManager;

    /** Initial value of the command's goal. */
    private String goalInitial;

    @Inject
    public GoalPage(GoalPageView view,
                    CommandGoalRegistry commandGoalRegistry,
                    CommandManager commandManager,
                    EditorMessages messages) {
        super(messages.pageGoalTitle());

        this.view = view;
        this.goalRegistry = commandGoalRegistry;
        this.commandManager = commandManager;

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        final String goalId = editedCommand.getGoal();
        final CommandGoal commandGoal = goalRegistry.getGoalForId(goalId);

        goalInitial = commandGoal.getId();

        final Set<CommandGoal> goals = new HashSet<>();
        goals.addAll(goalRegistry.getAllPredefinedGoals());
        goals.addAll(getCustomGoals());

        view.setAvailableGoals(goals);
        view.setGoal(commandGoal.getId());
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        final CommandGoal commandGoal = goalRegistry.getGoalForId(editedCommand.getGoal());

        return !(goalInitial.equals(commandGoal.getId()));
    }

    @Override
    public void onGoalChanged(String goalId) {
        editedCommand.setGoal(goalId);

        notifyDirtyStateChanged();
    }

    /** Returns all custom (non-predefined) command goals. */
    private Set<CommandGoal> getCustomGoals() {
        final Set<CommandGoal> list = new HashSet<>();

        for (CommandImpl command : commandManager.getCommands()) {
            final String goalId = command.getGoal();

            final Optional<CommandGoal> goalOptional = goalRegistry.getPredefinedGoalById(goalId);
            if (!goalOptional.isPresent() && !isNullOrEmpty(goalId)) {
                list.add(new BaseCommandGoal(goalId));
            }
        }

        return list;
    }
}
