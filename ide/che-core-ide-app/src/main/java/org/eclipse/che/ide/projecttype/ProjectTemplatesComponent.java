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

import org.eclipse.che.ide.api.project.ProjectTemplateServiceClient;

import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ProjectTemplatesComponent implements Component {

    private final ProjectTemplateServiceClient projectTemplateServiceClient;
    private final ProjectTemplateRegistry projectTemplateRegistry;
    private final DtoUnmarshallerFactory       dtoUnmarshallerFactory;

    @Inject
    public ProjectTemplatesComponent(ProjectTemplateServiceClient projectTemplateServiceClient,
                                     ProjectTemplateRegistry projectTemplateRegistry,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.projectTemplateServiceClient = projectTemplateServiceClient;
        this.projectTemplateRegistry = projectTemplateRegistry;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        projectTemplateServiceClient.getProjectTemplates(new AsyncRequestCallback<List<ProjectTemplateDescriptor>>(
                dtoUnmarshallerFactory.newListUnmarshaller(ProjectTemplateDescriptor.class)) {
            @Override
            protected void onSuccess(List<ProjectTemplateDescriptor> result) {
                for (ProjectTemplateDescriptor template : result) {
                    projectTemplateRegistry.register(template);
                }
                callback.onSuccess(ProjectTemplatesComponent.this);
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(new Exception("Can't load project templates", exception));
            }
        });
    }
}
