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

import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.ide.api.project.ProjectTemplateServiceClient;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ProjectTemplatesRegistrar {

    @Inject
    public ProjectTemplatesRegistrar(ProjectTemplateServiceClient serviceClient,
                                     ProjectTemplateRegistry registry,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        Unmarshallable<List<ProjectTemplateDescriptor>> unmarshaller =
                dtoUnmarshallerFactory.newListUnmarshaller(ProjectTemplateDescriptor.class);

        serviceClient.getProjectTemplates(new AsyncRequestCallback<List<ProjectTemplateDescriptor>>(unmarshaller) {
            @Override
            protected void onSuccess(List<ProjectTemplateDescriptor> result) {
                result.forEach(registry::register);
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(ProjectTemplatesRegistrar.this.getClass(), "Can't load project templates");
            }
        });
    }
}
