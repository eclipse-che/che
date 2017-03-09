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
package org.eclipse.che.ide.command.editor.page.project;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.ContextualCommand.ApplicableContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent;
import org.eclipse.che.ide.api.resources.ResourceChangedEvent.ResourceChangedHandler;
import org.eclipse.che.ide.api.resources.ResourceDelta;
import org.eclipse.che.ide.command.editor.EditorMessages;
import org.eclipse.che.ide.command.editor.page.AbstractCommandEditorPage;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presenter for {@link CommandEditorPage} which allows to edit command's applicable projects.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectsPage extends AbstractCommandEditorPage implements ProjectsPageView.ActionDelegate,
                                                                       ResourceChangedHandler {

    private final ProjectsPageView view;
    private final AppContext       appContext;

    private final Map<Project, Boolean> projectsState;

    /** Initial value of the applicable projects list. */
    private List<String> applicableProjectsInitial;

    @Inject
    public ProjectsPage(ProjectsPageView view,
                        AppContext appContext,
                        EditorMessages messages,
                        EventBus eventBus) {
        super(messages.pageProjectsTitle());

        this.view = view;
        this.appContext = appContext;

        eventBus.addHandler(ResourceChangedEvent.getType(), this);

        projectsState = new HashMap<>();

        view.setDelegate(this);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    protected void initialize() {
        final ApplicableContext context = editedCommand.getApplicableContext();

        applicableProjectsInitial = new ArrayList<>(context.getApplicableProjects());

        refreshProjects();
    }

    /** Refresh 'Projects' section in the view. */
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

        final ApplicableContext applicableContext = editedCommand.getApplicableContext();

        return !(applicableProjectsInitial.equals(applicableContext.getApplicableProjects()));
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
