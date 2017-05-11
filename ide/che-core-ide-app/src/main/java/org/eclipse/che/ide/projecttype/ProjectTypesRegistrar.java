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
package org.eclipse.che.ide.projecttype;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.ProjectTypeServiceClient;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.workspace.event.WsStatusChangedEvent;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ProjectTypesRegistrar {

    private final ProjectTypeServiceClient projectTypeService;
    private final ProjectTypeRegistry      projectTypeRegistry;
    private final AppContext               appContext;

    @Inject
    public ProjectTypesRegistrar(ProjectTypeServiceClient projectTypeService,
                                 ProjectTypeRegistry projectTypeRegistry,
                                 AppContext appContext,
                                 EventBus eventBus) {
        this.projectTypeService = projectTypeService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.appContext = appContext;

        eventBus.addHandler(WsStatusChangedEvent.TYPE, event -> {
            if (event.getStatus() == WorkspaceStatus.RUNNING) {
                register();
            }
        });
    }

    private void register() {
        projectTypeService.getProjectTypes(appContext.getDevMachine())
                          .then(projectTypeRegistry::registerAll)
                          .catchError(err -> {
                              Log.error(ProjectTypesRegistrar.class, "Can't load project types: " + err.getMessage());
                          });
    }
}
