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
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
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

    private static final SettingsPageViewImplUiBinder UI_BINDER = GWT.create(SettingsPageViewImplUiBinder.class);

    // initial height of this view
    private static final int VIEW_HEIGHT_INITIAL_PX          = 100;
    // height of the 'Projects' table's header
    private static final int PROJECTS_TABLE_HEADER_HEIGHT_PX = 20;

    @UiField
    SimpleLayoutPanel mainPanel;

    @UiField
    CustomComboBox goalComboBox;

    @UiField
    CheckBox workspaceCheckBox;

    @UiField
    FlowPanel projectsSection;

    @UiField
    FlowPanel projectsPanel;

    private ActionDelegate delegate;

    @Inject
    public SettingsPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));

        setHeight(VIEW_HEIGHT_INITIAL_PX + "px");
        projectsSection.setVisible(false);
    }

    @Override
    public void setAvailableGoals(Set<CommandGoal> goals) {
        goalComboBox.clear();
        goals.forEach(g -> goalComboBox.addItem(g.getId()));
    }

    @Override
    public void setGoal(String goalId) {
        goalComboBox.setValue(goalId);
    }

    @Override
    public void setWorkspace(boolean value) {
        workspaceCheckBox.setValue(value);
    }

    @Override
    public void setProjects(Map<Project, Boolean> projects) {
        projectsPanel.clear();
        projectsSection.setVisible(!projects.isEmpty());

        projects.entrySet().forEach(entry -> addProjectSwitcherToPanel(entry.getKey(), entry.getValue()));

        // set view's height depending on the amount project switchers
        if (!projects.isEmpty()) {
            Scheduler.get().scheduleDeferred(() -> setHeight(VIEW_HEIGHT_INITIAL_PX +
                                                             PROJECTS_TABLE_HEADER_HEIGHT_PX +
                                                             projectsSection.getOffsetHeight() + "px"));
        }
    }

    private void addProjectSwitcherToPanel(Project project, boolean applicable) {
        final ProjectSwitcher switcher = new ProjectSwitcher(project.getName());
        switcher.setValue(applicable);
        switcher.addValueChangeHandler(event -> delegate.onApplicableProjectChanged(project, event.getValue()));

        projectsPanel.add(switcher);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler({"goalComboBox"})
    void onGoalKeyUp(KeyUpEvent event) {
        delegate.onGoalChanged(goalComboBox.getValue());
    }

    @UiHandler({"goalComboBox"})
    void onGoalChanged(ChangeEvent event) {
        delegate.onGoalChanged(goalComboBox.getValue());
    }

    @UiHandler({"workspaceCheckBox"})
    void onWorkspaceChanged(ValueChangeEvent<Boolean> event) {
        delegate.onWorkspaceChanged(event.getValue());
    }

    interface SettingsPageViewImplUiBinder extends UiBinder<Widget, SettingsPageViewImpl> {
    }
}
