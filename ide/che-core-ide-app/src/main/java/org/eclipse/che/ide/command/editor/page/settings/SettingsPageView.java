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

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.resources.Project;

import java.util.Map;
import java.util.Set;

/**
 * The view of {@link SettingsPage}.
 *
 * @author Artem Zatsarynnyi
 */
public interface SettingsPageView extends View<SettingsPageView.ActionDelegate> {

    /** Set the list of goals which are available to set for command. */
    void setAvailableGoals(Set<CommandGoal> goals);

    /** Sets the command's goal value. */
    void setGoal(String goalId);

    void setWorkspace(boolean value);

    void setProjects(Map<Project, Boolean> projects);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /**
         * Called when command goal has been changed.
         *
         * @param goalId
         *         new value of the command goal
         */
        void onGoalChanged(String goalId);

        void onWorkspaceChanged(boolean value);

        void onApplicableProjectChanged(Project project, boolean value);
    }
}
