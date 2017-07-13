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
import org.eclipse.che.api.languageserver.shared.model.ExtendedPublishDiagnosticsParams;
import org.eclipse.che.plugin.languageserver.ide.editor.PublishDiagnosticsProcessor;

/**
 * Subscribes and receives JSON-RPC messages related to 'textDocument/publishDiagnostics' events
 */
@Singleton
public class PublishDiagnosticsReceiver {
    @Inject
    private void configureReceiver(Provider<PublishDiagnosticsProcessor> provider, RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("textDocument/publishDiagnostics")
                    .paramsAsDto(ExtendedPublishDiagnosticsParams.class)
                    .noResult()
                    .withConsumer(params -> provider.get().processDiagnostics(params));
    }

    @Inject
    private void subscribe(RequestTransmitter transmitter) {
        transmitter.newRequest()
                   .endpointId("ws-agent")
                   .methodName("textDocument/publishDiagnostics/subscribe")
                   .noParams()
                   .sendAndSkipResult();
    }
}
