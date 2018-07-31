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
package org.eclipse.che.ide.command.editor.page.project;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

/** Presenter for {@link CommandEditorPage} which allows to edit command's applicable projects. */
public class ProjectsPage extends AbstractCommandEditorPage
    implements ProjectsPageView.ActionDelegate, ResourceChangedHandler {

  private final ProjectsPageView view;
  private final AppContext appContext;

  /** Initial value of the applicable projects list. */
  private Set<String> applicableProjectsInitial;

  @Inject
  public ProjectsPage(
      ProjectsPageView view, AppContext appContext, EditorMessages messages, EventBus eventBus) {
    super(messages.pageProjectsTitle());

    this.view = view;
    this.appContext = appContext;

    eventBus.addHandler(ResourceChangedEvent.getType(), this);

    view.setDelegate(this);
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  protected void initialize() {
    final ApplicableContext context = editedCommand.getApplicableContext();

    applicableProjectsInitial = new HashSet<>(context.getApplicableProjects());

    refreshProjects();
  }

  /** Refresh 'Projects' section in the view. */
  private void refreshProjects() {
    final Map<Project, Boolean> projectsStates = new HashMap<>();

    for (Project project : appContext.getProjects()) {
      ApplicableContext context = editedCommand.getApplicableContext();
      boolean applicable = context.getApplicableProjects().contains(project.getPath());

      projectsStates.put(project, applicable);
    }

    view.setProjects(projectsStates);
  }

  @Override
  public boolean isDirty() {
    if (editedCommand == null) {
      return false;
    }

    ApplicableContext context = editedCommand.getApplicableContext();

    return !(applicableProjectsInitial.equals(context.getApplicableProjects()));
  }

  @Override
  public void onApplicableProjectChanged(Project project, boolean applicable) {
    final ApplicableContext context = editedCommand.getApplicableContext();

    if (applicable) {
      // if command is bound with one project at least
      // then remove command from the workspace
      if (context.getApplicableProjects().isEmpty()) {
        context.setWorkspaceApplicable(false);
      }

      context.addProject(project.getPath());
    } else {
      context.removeProject(project.getPath());

      // if command isn't bound to any project
      // then save it to the workspace
      if (context.getApplicableProjects().isEmpty()) {
        context.setWorkspaceApplicable(true);
      }
    }

    notifyDirtyStateChanged();
  }

  @Override
  public void onResourceChanged(ResourceChangedEvent event) {
    final ResourceDelta delta = event.getDelta();
    final Resource resource = delta.getResource();

    if (resource.isProject()) {
      // defer refreshing the projects section since appContext#getProjects may return old data
      Scheduler.get().scheduleDeferred(this::refreshProjects);
    }
  }
}
