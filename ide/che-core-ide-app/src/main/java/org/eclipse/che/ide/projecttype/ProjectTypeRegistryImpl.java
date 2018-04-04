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

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.api.project.type.ProjectTypesLoadedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

@Singleton
public class ProjectTypeRegistryImpl implements ProjectTypeRegistry {

  private final AsyncRequestFactory asyncRequestFactory;
  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AppContext appContext;
  private final EventBus eventBus;

  private final Map<String, ProjectTypeDto> projectTypes;

  @Inject
  public ProjectTypeRegistryImpl(
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      AppContext appContext,
      EventBus eventBus) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.appContext = appContext;
    this.eventBus = eventBus;

    projectTypes = new HashMap<>();

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        e -> {
          if (RUNNING == appContext.getWorkspace().getStatus()) {
            registerProjectTypes();
          }
        });

    eventBus.addHandler(WsAgentServerRunningEvent.TYPE, e -> registerProjectTypes());
    eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, e -> projectTypes.clear());
  }

  @Override
  public List<ProjectTypeDto> getProjectTypes() {
    return new ArrayList<>(projectTypes.values());
  }

  @Nullable
  @Override
  public ProjectTypeDto getProjectType(String id) {
    return projectTypes.get(id);
  }

  private void registerProjectTypes() {
    fetchProjectTypes()
        .then(
            typeDescriptors -> {
              typeDescriptors.forEach(
                  projectTypeDto -> projectTypes.put(projectTypeDto.getId(), projectTypeDto));

              eventBus.fireEvent(new ProjectTypesLoadedEvent());
            })
        .catchError(
            error -> {
              Log.error(
                  ProjectTypeRegistryImpl.this.getClass(),
                  "Can't load project types: " + error.getCause());
            });
  }

  private Promise<List<ProjectTypeDto>> fetchProjectTypes() {
    final String url = appContext.getWsAgentServerApiEndpoint() + "/project-type";

    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .send(dtoUnmarshallerFactory.newListUnmarshaller(ProjectTypeDto.class));
  }
}
