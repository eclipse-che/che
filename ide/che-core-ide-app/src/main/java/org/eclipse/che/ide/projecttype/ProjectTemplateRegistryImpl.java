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
package org.eclipse.che.ide.projecttype;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.project.templates.shared.dto.ProjectTemplateDescriptor;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.type.ProjectTemplateRegistry;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

@Singleton
public class ProjectTemplateRegistryImpl implements ProjectTemplateRegistry {

  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final AppContext appContext;

  private final Map<String, List<ProjectTemplateDescriptor>> templateDescriptors;

  @Inject
  ProjectTemplateRegistryImpl(
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      AsyncRequestFactory asyncRequestFactory,
      AppContext appContext) {
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.appContext = appContext;

    templateDescriptors = new HashMap<>();
  }

  @Override
  public List<ProjectTemplateDescriptor> getTemplates(String projectTypeId) {
    return templateDescriptors.getOrDefault(projectTypeId, new ArrayList<>());
  }

  @Inject
  private void registerAllTemplates() {
    fetchTemplates()
        .then(
            templateDescriptors -> {
              templateDescriptors.forEach(this::register);
            })
        .catchError(
            error -> {
              Log.error(
                  ProjectTemplateRegistryImpl.this.getClass(),
                  "Can't load project templates: " + error.getCause());
            });
  }

  private void register(ProjectTemplateDescriptor descriptor) {
    templateDescriptors
        .computeIfAbsent(descriptor.getProjectType(), key -> new ArrayList<>())
        .add(descriptor);
  }

  private Promise<List<ProjectTemplateDescriptor>> fetchTemplates() {
    final String baseUrl = appContext.getMasterApiEndpoint() + "/project-template/all";

    return asyncRequestFactory
        .createGetRequest(baseUrl)
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(ProjectTemplateDescriptor.class));
  }
}
