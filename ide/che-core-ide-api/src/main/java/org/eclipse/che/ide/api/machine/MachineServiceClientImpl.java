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
package org.eclipse.che.ide.api.machine;

import com.google.inject.Inject;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
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
    protected MachineServiceClientImpl(AppContext appContext,
                                       DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                       AsyncRequestFactory asyncRequestFactory,
                                       LoaderFactory loaderFactory) {
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.baseHttpUrl = appContext.getMasterEndpoint() + "/workspace/";
    }

    @Override
    public Promise<List<MachineDto>> getMachines(String workspaceId) {
        return asyncRequestFactory.createGetRequest(baseHttpUrl + workspaceId + "/machine")
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .loader(loaderFactory.newLoader("Getting info about bound machines..."))
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(MachineDto.class));
    }

    @Override
    public Promise<Void> destroyMachine(@NotNull final String workspaceId,
                                        @NotNull final String machineId) {
        return asyncRequestFactory.createRequest(DELETE,
                                                 baseHttpUrl + workspaceId +
                                                 "/machine/" + machineId,
                                                 null,
                                                 false)
                                  .loader(loaderFactory.newLoader("Destroying machine..."))
                                  .send();
    }
}
