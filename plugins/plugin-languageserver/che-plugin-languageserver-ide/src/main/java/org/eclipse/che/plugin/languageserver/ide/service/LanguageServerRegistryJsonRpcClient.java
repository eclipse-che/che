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

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;

import static org.eclipse.che.ide.api.workspace.Constants.WORKSAPCE_AGENT_ENDPOINT_ID;

@Singleton
public class LanguageServerRegistryJsonRpcClient {

    private final RequestTransmitter requestTransmitter;

    @Inject
    public LanguageServerRegistryJsonRpcClient(RequestTransmitter requestTransmitter) {
        this.requestTransmitter = requestTransmitter;
    }

    public JsonRpcPromise<Boolean> initializeServer(String path) {
        return requestTransmitter.newRequest()
                                 .endpointId(WORKSAPCE_AGENT_ENDPOINT_ID)
                                 .methodName("languageServer/initialize")
                                 .paramsAsString(path)
                                 .sendAndReceiveResultAsBoolean(30_000);
    }

}
