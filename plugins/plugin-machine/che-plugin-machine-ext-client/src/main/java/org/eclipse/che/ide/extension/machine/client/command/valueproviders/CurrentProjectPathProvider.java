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
package org.eclipse.che.ide.extension.machine.client.command.valueproviders;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectHandler;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedEvent;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedHandler;
import org.eclipse.che.ide.api.event.project.ProjectReadyEvent;
import org.eclipse.che.ide.api.event.project.ProjectReadyHandler;
import org.eclipse.che.ide.extension.machine.client.machine.events.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.machine.events.MachineStateHandler;

import javax.validation.constraints.NotNull;

/**
 * Provides current project's path.
 * Path means full absolute path to project on the FS, e.g. /projects/project_name
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CurrentProjectPathProvider implements CommandPropertyValueProvider,
                                                   MachineStateHandler,
                                                   CloseCurrentProjectHandler,
                                                   ProjectReadyHandler,
                                                   CurrentProjectChangedHandler {

    private static final String KEY = "${current.project.path}";

    private final AppContext appContext;

    private String value;

    @Inject
    public CurrentProjectPathProvider(EventBus eventBus, AppContext appContext) {
        this.appContext = appContext;
        value = "";

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(ProjectReadyEvent.TYPE, this);
        eventBus.addHandler(CurrentProjectChangedEvent.TYPE, this);
        updateValue();
    }

    @NotNull
    @Override
    public String getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        CurrentProject currentProject = appContext.getCurrentProject();

        final MachineDto machine = event.getMachine();
        if (currentProject == null || !machine.getConfig().isDev()) {
            return;
        }

        value = currentProject.getProjectConfig().getPath();
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        if (event.getMachine().getConfig().isDev()) {
            value = "";
        }
    }

    @Override
    public void onProjectReady(ProjectReadyEvent event) {
        updateValue();
    }

    @Override
    public void onCloseCurrentProject(CloseCurrentProjectEvent event) {
        value = "";
    }

    private void updateValue() {
        final String devMachineId = appContext.getDevMachineId();
        final CurrentProject currentProject = appContext.getCurrentProject();
        if (devMachineId == null || currentProject == null) {
            return;
        }

        value = appContext.getProjectsRoot() + currentProject.getProjectConfig().getPath();
    }

    @Override
    public void onCurrentProjectChanged(CurrentProjectChangedEvent event) {
        updateValue();
    }
}
