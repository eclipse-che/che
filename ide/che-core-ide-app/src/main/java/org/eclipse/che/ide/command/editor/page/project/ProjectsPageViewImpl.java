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
package org.eclipse.che.ide.command.editor.page.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.che.ide.api.resources.Project;

/**
 * Implementation of {@link ProjectsPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectsPageViewImpl extends Composite implements ProjectsPageView {

  private static final ProjectsPageViewImplUiBinder UI_BINDER =
      GWT.create(ProjectsPageViewImplUiBinder.class);

  @UiField FlowPanel mainPanel;

  @UiField FlowPanel projectsPanel;

  private ActionDelegate delegate;

  @Inject
  public ProjectsPageViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));

    mainPanel.setVisible(false);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setProjects(Map<Project, Boolean> projects) {
    projectsPanel.clear();
    mainPanel.setVisible(!projects.isEmpty());

    projects.forEach(this::addProjectSwitcherToPanel);
  }

  private void addProjectSwitcherToPanel(Project project, boolean applicable) {
    final ProjectSwitcher switcher = new ProjectSwitcher(project.getName());
    switcher.setValue(applicable);
    switcher.addValueChangeHandler(
        event -> delegate.onApplicableProjectChanged(project, event.getValue()));

    projectsPanel.add(switcher);
  }

  interface ProjectsPageViewImplUiBinder extends UiBinder<Widget, ProjectsPageViewImpl> {}
}
