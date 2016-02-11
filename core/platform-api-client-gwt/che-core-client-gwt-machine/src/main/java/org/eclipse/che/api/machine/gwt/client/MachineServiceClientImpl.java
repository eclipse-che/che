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
package org.eclipse.che.api.machine.gwt.client;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.gwt.http.client.RequestBuilder.DELETE;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * Implementation for {@link MachineServiceClient}.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
public class MachineServiceClientImpl implements MachineServiceClient {
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final LoaderFactory          loaderFactory;
    private final String                 baseHttpUrl;

    @Inject
    protected MachineServiceClientImpl(@RestContext String restContext,
                                       DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                       AsyncRequestFactory asyncRequestFactory,
                                       LoaderFactory loaderFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.baseHttpUrl = restContext + "/machine";
    }

    @Override
    public Promise<MachineDto> getMachine(@NotNull final String machineId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + '/' + machineId)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about machine..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<MachineStateDto> getMachineState(@NotNull final String machineId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + '/' + machineId + "/state")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about machine..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineStateDto.class));
    }

    @Override
    public Promise<List<MachineDto>> getWorkspaceMachines(@NotNull String workspaceId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "?workspace=" + workspaceId)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about bound machines..."))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<List<MachineStateDto>> getMachinesStates(@NotNull final String workspaceId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "/state" + "?workspace=" + workspaceId)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about bound machines..."))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineStateDto.class));
    }

    @Override
    public Promise<Void> destroyMachine(@NotNull final String machineId) {
        return asyncRequestFactory.createRequest(DELETE, baseHttpUrl + '/' + machineId, null, false)
                                  .loader(loaderFactory.newLoader("Destroying machine..."))
                                  .send();
    }

    @Override
    public Promise<MachineProcessDto> executeCommand(@NotNull final String machineId,
                                                     @NotNull final Command command,
                                                     @Nullable final String outputChannel) {
        return asyncRequestFactory.createPostRequest(baseHttpUrl + '/' + machineId + "/command?outputChannel=" + outputChannel, command)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Executing command..."))
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineProcessDto.class));
    }

    @Override
    public Promise<List<MachineProcessDto>> getProcesses(@NotNull final String machineId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + "/" + machineId + "/process")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting machine processes..."))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineProcessDto.class));
    }

    @Override
    public Promise<Void> stopProcess(@NotNull final String machineId, final int processId) {
        return asyncRequestFactory.createDeleteRequest(baseHttpUrl + '/' + machineId + "/process/" + processId)
                                  .loader(loaderFactory.newLoader("Stopping process..."))
                                  .send();
    }

    @Override
    public Promise<String> getFileContent(@NotNull final String machineId, final @NotNull String path, final int startFrom,
                                          final int limit) {
        String url = baseHttpUrl + "/" + machineId + "/filepath/" + path + "?startFrom=" + startFrom + "&limit=" + limit;
        return asyncRequestFactory.createGetRequest(url)
                                  .loader(loaderFactory.newLoader("Loading file content..."))
                                  .send(new StringUnmarshaller());
    }
}
