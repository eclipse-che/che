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
package org.eclipse.che.plugin.languageserver.ide.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.languageserver.shared.lsapi.SymbolInformationDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.WorkspaceSymbolParamsDTO;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import java.util.List;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class WorkspaceServiceClient {
    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final AppContext appContext;
    private final AsyncRequestFactory asyncRequestFactory;
    
    
    @Inject
    public WorkspaceServiceClient(final DtoUnmarshallerFactory unmarshallerFactory,
                                  final AppContext appContext,
                                  final AsyncRequestFactory asyncRequestFactory
                                  ) {
        this.unmarshallerFactory = unmarshallerFactory;
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
    }

    /**
     * GWT client implementation of {@link io.typefox.lsapi.WorkspaceService#symbol(io.typefox.lsapi.WorkspaceSymbolParams)}
     *
     * @param params
     * @return
     */
    public Promise<List<SymbolInformationDTO>> symbol(WorkspaceSymbolParamsDTO params) {
        String requestUrl = appContext.getDevMachine().getWsAgentBaseUrl() + "/languageserver/workspace/symbol";
        Unmarshallable<List<SymbolInformationDTO>> unmarshaller = unmarshallerFactory.newListUnmarshaller(SymbolInformationDTO.class);
        return asyncRequestFactory.createPostRequest(requestUrl, params)
                                  .header(ACCEPT, APPLICATION_JSON)
                                  .header(CONTENT_TYPE, APPLICATION_JSON)
                                  .send(unmarshaller);
    }
    
}
