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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ui.listbox.CustomComboBox;

import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link SettingsPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class SettingsPageViewImpl extends Composite implements SettingsPageView {

    private static final InfoPageViewImplUiBinder UI_BINDER = GWT.create(InfoPageViewImplUiBinder.class);

    @UiField
    TextBox commandName;

    @UiField
    CustomComboBox goal;

    @UiField
    CheckBox workspace;

    @UiField
    FlowPanel projectsSection;

    @UiField
    FlowPanel projectsPanel;

    private ActionDelegate delegate;

    @Inject
    public SettingsPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));

        projectsSection.setVisible(false);
    }

    @Override
    public void setAvailableGoals(Set<CommandGoal> goals) {
        goal.clear();

        for (CommandGoal g : goals) {
            goal.addItem(g.getDisplayName());
        }
    }

    @Override
    public void setGoal(String goal) {
        this.goal.setValue(goal);
    }

    @Override
    public void setCommandName(String name) {
        commandName.setValue(name);
    }

    @Override
    public void setWorkspace(boolean value) {
        workspace.setValue(value);
    }

    @Override
    public void setProjects(Map<Project, Boolean> projects) {
        projectsPanel.clear();

        projectsSection.setVisible(!projects.isEmpty());

        for (final Map.Entry<Project, Boolean> entry : projects.entrySet()) {
            final Project project = entry.getKey();

            final ProjectSwitcher switcher = new ProjectSwitcher(project.getName());

            switcher.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    delegate.onApplicableProjectChanged(project, event.getValue());
                }
            });

            switcher.setValue(entry.getValue());

            projectsPanel.add(switcher);
        }
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler({"commandName"})
    void onCommandNameChanged(KeyUpEvent event) {
        delegate.onNameChanged(commandName.getValue());
    }

    @UiHandler({"goal"})
    void onPortKeyUp(KeyUpEvent event) {
        delegate.onGoalChanged(goal.getValue());
    }

    @UiHandler({"goal"})
    void onPortChanged(ChangeEvent event) {
        delegate.onGoalChanged(goal.getValue());
    }

    @UiHandler({"workspace"})
    void onWorkspaceChanged(ValueChangeEvent<Boolean> event) {
        delegate.onWorkspaceChanged(event.getValue());
    }

    interface InfoPageViewImplUiBinder extends UiBinder<Widget, SettingsPageViewImpl> {
    }
}
