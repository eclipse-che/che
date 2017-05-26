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

import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.project.type.ProjectTypeRegistry;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

@Singleton
public class ProjectTypeRegistryImpl implements ProjectTypeRegistry {

    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AppContext             appContext;

    private final Map<String, ProjectTypeDto> projectTypes;

    @Inject
    public ProjectTypeRegistryImpl(AsyncRequestFactory asyncRequestFactory,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   AppContext appContext,
                                   EventBus eventBus) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.appContext = appContext;

        projectTypes = new HashMap<>();

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                registerProjectTypes();
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });
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
        fetchProjectTypes().then(typeDescriptors -> {
            typeDescriptors.forEach(projectTypeDto -> projectTypes.put(projectTypeDto.getId(), projectTypeDto));
        }).catchError(error -> {
            Log.error(ProjectTypeRegistryImpl.this.getClass(), "Can't load project types: " + error.getCause());
        });
    }

    private Promise<List<ProjectTypeDto>> fetchProjectTypes() {
        final String url = appContext.getDevAgentEndpoint() + "/project-type";

        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(ProjectTypeDto.class));
    }
}
