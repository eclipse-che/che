/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.editor.page.info;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.BaseCommandGoal;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.COMMAND_GOAL_ATTRIBUTE_NAME;

/**
 * {@link CommandEditorPage} which allows to edit basic command's information, like:
 * <ul>
 * <li>name;</li>
 * <li>goal;</li>
 * <li>applicable context.</li>
 * </ul>
 *
 * @author Artem Zatsarynnyi
 */
public class InfoPage extends AbstractCommandEditorPage implements InfoPageView.ActionDelegate {

    private final InfoPageView                  view;
    private final AppContext                    appContext;
    private final PredefinedCommandGoalRegistry goalRegistry;
    private final CommandManager                commandManager;

    private final Map<Project, Boolean> projectsState;

    private String       goalInitial;
    /** Initial value of the command's name. */
    private String       commandNameInitial;
    /** Initial value of the workspace flag. */
    private boolean      workspaceInitial;
    /** Initial value of the applicable projects list. */
    private List<String> applicableProjectsInitial;

    @Inject
    public InfoPage(InfoPageView view,
                    AppContext appContext,
                    PredefinedCommandGoalRegistry predefinedCommandGoalRegistry,
                    CommandManager commandManager,
                    EditorMessages messages) {
        super(messages.pageInfoTitle(), messages.pageInfoTooltip());

        this.view = view;
        this.appContext = appContext;
        this.goalRegistry = predefinedCommandGoalRegistry;
        this.commandManager = commandManager;

        projectsState = new HashMap<>();

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        String goalId = editedCommand.getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);
        if (isNullOrEmpty(goalId)) {
            goalId = "";
        }

        final ApplicableContext context = editedCommand.getApplicableContext();

        goalInitial = goalId;
        commandNameInitial = editedCommand.getName();
        workspaceInitial = context.isWorkspaceApplicable();
        applicableProjectsInitial = new ArrayList<>(context.getApplicableProjects());

        final Set<CommandGoal> goals = new HashSet<>();
        goals.addAll(goalRegistry.getAllGoals());
        goals.addAll(getCustomGoals());

        view.setAvailableGoals(goals);


        view.setGoal(goalId);
        view.setCommandName(editedCommand.getName());
        view.setWorkspace(editedCommand.getApplicableContext().isWorkspaceApplicable());

        refreshProjects();
    }

    /** Refresh projects in the view. */
    private void refreshProjects() {
        projectsState.clear();

        final ApplicableContext context = editedCommand.getApplicableContext();

        for (Project project : appContext.getProjects()) {
            final boolean applicable = context.getApplicableProjects().contains(project.getPath());

            projectsState.put(project, applicable);
        }

        view.setProjects(projectsState);
    }

    @Override
    public boolean isDirty() {
        if (editedCommand == null) {
            return false;
        }

        String goalId = editedCommand.getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);
        if (isNullOrEmpty(goalId)) {
            goalId = "";
        }

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        return !(goalInitial.equals(goalId) &&
                 commandNameInitial.equals(editedCommand.getName()) &&
                 workspaceInitial == applicableContext.isWorkspaceApplicable() &&
                 applicableProjectsInitial.equals(applicableContext.getApplicableProjects()));
    }

    @Override
    public void onGoalChanged(String goalId) {
        editedCommand.getAttributes().put(COMMAND_GOAL_ATTRIBUTE_NAME, goalId);

        notifyDirtyStateChanged();
    }

    @Override
    public void onNameChanged(String name) {
        editedCommand.setName(name);

        notifyDirtyStateChanged();
    }

    @Override
    public void onWorkspaceChanged(boolean value) {
        editedCommand.getApplicableContext().setWorkspaceApplicable(value);

        notifyDirtyStateChanged();
    }

    @Override
    public void onApplicableProjectChanged(Project project, boolean value) {
        projectsState.put(project, value);

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        if (value) {
            applicableContext.addProject(project.getPath());
        } else {
            applicableContext.removeProject(project.getPath());
        }

        notifyDirtyStateChanged();
    }

    /** Returns all custom (non-predefined) command goals. */
    private Set<CommandGoal> getCustomGoals() {
        final Set<CommandGoal> list = new HashSet<>();

        for (ContextualCommand command : commandManager.getCommands()) {
            final String goal = command.getAttributes().get(COMMAND_GOAL_ATTRIBUTE_NAME);

            if (!isNullOrEmpty(goal)) {
                list.add(new BaseCommandGoal(goal, goal));
            }
        }

        return list;
    }
}
