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
package org.eclipse.che.ide.extension.machine.client.command.macros;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;

import javax.validation.constraints.NotNull;

/**
 * Provides current project's path.
 * Path means full absolute path to project on the FS, e.g. /projects/project_name
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class CurrentProjectPathMacro implements Macro, WsAgentStateHandler {

    private static final String KEY = "${current.project.path}";

    private final AppContext      appContext;
    private final PromiseProvider promises;
    private final MachineLocalizationConstant localizationConstants;

    private String value;

    @Inject
    public CurrentProjectPathMacro(EventBus eventBus,
                                   AppContext appContext,
                                   PromiseProvider promises,
                                   MachineLocalizationConstant localizationConstants) {
        this.appContext = appContext;
        this.promises = promises;
        this.localizationConstants = localizationConstants;
        value = "";

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @NotNull
    @Override
    public String getName() {
        return KEY;
    }

    @Override
    public String getDescription() {
        return localizationConstants.macroCurrentProjectPathDescription();
    }

    @NotNull
    @Override
    public Promise<String> expand() {
        updateValue();

        return promises.resolve(value);
    }

    private void updateValue() {
        final Resource[] resources = appContext.getResources();

        if (resources != null && resources.length == 1) {
            final Optional<Project> project = appContext.getResource().getRelatedProject();

            if (project.isPresent()) {
                value = appContext.getProjectsRoot().append(project.get().getLocation()).toString();
            } else {
                value = "";
            }
        } else {
            value = "";
        }
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        updateValue();
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
        value = "";
    }
}
