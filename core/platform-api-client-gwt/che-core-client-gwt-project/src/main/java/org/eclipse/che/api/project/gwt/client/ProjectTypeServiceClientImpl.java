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
package org.eclipse.che.api.project.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.gwt.client.WsAgentUrlProvider;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
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
    private final WsAgentUrlProvider     urlProvider;

    @Inject
    protected ProjectTypeServiceClientImpl(LoaderFactory loaderFactory,
                                           AsyncRequestFactory asyncRequestFactory,
                                           DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                           WsAgentUrlProvider urlProvider) {
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.urlProvider = urlProvider;
    }

    @Override
    public Promise<List<ProjectTypeDto>> getProjectTypes(@NotNull final String workspaceId) {
        return newPromise(new RequestCall<List<ProjectTypeDto>>() {
            @Override
            public void makeCall(AsyncCallback<List<ProjectTypeDto>> callback) {
                getProjectTypes(workspaceId, callback);
            }
        }).then(new Function<List<ProjectTypeDto>, List<ProjectTypeDto>>() {
            @Override
            public List<ProjectTypeDto> apply(List<ProjectTypeDto> arg) throws FunctionException {
                final List<ProjectTypeDto> descriptors = new ArrayList<>();
                for (ProjectTypeDto descriptor : arg) {
                    descriptors.add(descriptor);
                }
                return descriptors;
            }
        });
    }

    private void getProjectTypes(String workspaceId, @NotNull AsyncCallback<List<ProjectTypeDto>> callback) {
        final String url = urlProvider.get() + "/project-type/" + workspaceId;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting info about registered project types..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newListUnmarshaller(ProjectTypeDto.class)));
    }

    @Override
    public Promise<ProjectTypeDto> getProjectType(final String workspaceId, final String id) {
        return newPromise(new RequestCall<ProjectTypeDto>() {
            @Override
            public void makeCall(AsyncCallback<ProjectTypeDto> callback) {
                getProjectType(workspaceId, id, callback);
            }
        });
    }

    private void getProjectType(@NotNull String workspaceId, @NotNull String id, @NotNull AsyncCallback<ProjectTypeDto> callback) {
        final String url = urlProvider.get() + "/project-type/" + workspaceId + '/' + id;
        asyncRequestFactory.createGetRequest(url)
                           .header(ACCEPT, APPLICATION_JSON)
                           .loader(loaderFactory.newLoader("Getting info about project type..."))
                           .send(newCallback(callback, dtoUnmarshallerFactory.newUnmarshaller(ProjectTypeDto.class)));
    }
}
