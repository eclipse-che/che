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
package org.eclipse.che.ide.api.project;

import com.google.inject.Inject;

import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * The implementation of {@link ProjectTypeServiceClient}.
 *
 * @author Artem Zatsarynnyi
 */
public class ProjectTypeServiceClientImpl implements ProjectTypeServiceClient {
    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    protected ProjectTypeServiceClientImpl(LoaderFactory loaderFactory,
                                           AsyncRequestFactory asyncRequestFactory,
                                           DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    }

    @Override
    public Promise<List<ProjectTypeDto>> getProjectTypes(@NotNull final DevMachine devMachine) {
        return fetchProjectTypes(devMachine).then(new Function<List<ProjectTypeDto>, List<ProjectTypeDto>>() {
            @Override
            public List<ProjectTypeDto> apply(List<ProjectTypeDto> arg) throws FunctionException {
                return arg.stream().collect(Collectors.toList());
            }
        });
    }

    private Promise<List<ProjectTypeDto>> fetchProjectTypes(@NotNull final DevMachine devMachine) {
        final String url = devMachine.getWsAgentBaseUrl() + "/project-type";
        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about registered project types..."))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(ProjectTypeDto.class));
    }

    @Override
    public Promise<ProjectTypeDto> getProjectType(@NotNull DevMachine devMachine, @NotNull String id) {
        final String url = devMachine.getWsAgentBaseUrl() + "/project-type/" + id;
        return asyncRequestFactory.createGetRequest(url)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about project type..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(ProjectTypeDto.class));
    }
}
