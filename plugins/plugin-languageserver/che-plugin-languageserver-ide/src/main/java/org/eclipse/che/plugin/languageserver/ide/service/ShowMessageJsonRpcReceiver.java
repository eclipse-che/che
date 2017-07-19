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
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.plugin.languageserver.ide.editor.ShowMessageProcessor;
import org.eclipse.lsp4j.ShowMessageRequestParams;

import static org.eclipse.che.ide.api.workspace.Constants.WORKSPACE_AGENT_ENDPOINT_ID;

/**
 * Subscribes and receives JSON-RPC messages related to 'window/showMessage' events
 */
@Singleton
public class ShowMessageJsonRpcReceiver {
    @Inject
    private void configureReceiver(Provider<ShowMessageProcessor> provider, RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("window/showMessage")
                    .paramsAsDto(ShowMessageRequestParams.class)
                    .noResult()
                    .withConsumer(params -> provider.get().processNotification(params));
    }

    @Inject
    private void subscribe(RequestTransmitter transmitter) {
        transmitter.newRequest()
                   .endpointId(WORKSPACE_AGENT_ENDPOINT_ID)
                   .methodName("window/showMessage/subscribe")
                   .noParams()
                   .sendAndSkipResult();
    }
}
