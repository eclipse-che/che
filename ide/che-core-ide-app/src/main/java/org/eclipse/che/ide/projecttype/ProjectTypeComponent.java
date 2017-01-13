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

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.component.WsAgentComponent;
import org.eclipse.che.ide.api.project.ProjectTypeServiceClient;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;

import java.util.List;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ProjectTypeComponent implements WsAgentComponent {

    private final ProjectTypeServiceClient projectTypeService;
    private final ProjectTypeRegistry      projectTypeRegistry;
    private final AppContext               appContext;

    @Inject
    public ProjectTypeComponent(ProjectTypeServiceClient projectTypeService,
                                ProjectTypeRegistry projectTypeRegistry,
                                AppContext appContext) {
        this.projectTypeService = projectTypeService;
        this.projectTypeRegistry = projectTypeRegistry;
        this.appContext = appContext;
    }

    @Override
    public void start(final Callback<WsAgentComponent, Exception> callback) {
        projectTypeService.getProjectTypes(appContext.getDevMachine()).then(new Operation<List<ProjectTypeDto>>() {
            @Override
            public void apply(List<ProjectTypeDto> arg) throws OperationException {
                projectTypeRegistry.registerAll(arg);
                callback.onSuccess(ProjectTypeComponent.this);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new Exception("Can't load project types: " + arg.toString()));
            }
        });
    }
}
