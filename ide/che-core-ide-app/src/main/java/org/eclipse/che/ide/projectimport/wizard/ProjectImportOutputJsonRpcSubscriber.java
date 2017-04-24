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
package org.eclipse.che.ide.projectimport.wizard;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.project.shared.ImportProgressRecord;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;
import org.eclipse.che.ide.jsonrpc.RequestHandlerConfigurator;
import org.eclipse.che.ide.jsonrpc.RequestHandlerRegistry;
import org.eclipse.che.ide.jsonrpc.RequestTransmitter;

import java.util.function.Consumer;

/**
 * Json RPC subscriber for listening to the project import events. Register itself for the listening events from the server side.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
@Singleton
public class ProjectImportOutputJsonRpcSubscriber {

    public static final String WS_AGENT_ENDPOINT = "ws-agent";

    private static final String EVENT_IMPORT_OUTPUT_SUBSCRIBE    = "event:import-project:subscribe";
    private static final String EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE = "event:import-project:un-subscribe";
    private static final String EVENT_IMPORT_OUTPUT_PROGRESS     = "event:import-project:progress";

    private final RequestTransmitter         transmitter;
    private final RequestHandlerConfigurator configurator;
    private final RequestHandlerRegistry     handlerRegistry;

    @Inject
    public ProjectImportOutputJsonRpcSubscriber(RequestTransmitter transmitter,
                                                RequestHandlerConfigurator configurator,
                                                RequestHandlerRegistry handlerRegistry) {
        this.transmitter = transmitter;
        this.configurator = configurator;
        this.handlerRegistry = handlerRegistry;
    }

    protected void subscribeForImportOutputEvents(Consumer<ImportProgressRecord> progressConsumer) {
        transmitter.transmitNoneToNone(WS_AGENT_ENDPOINT, EVENT_IMPORT_OUTPUT_SUBSCRIBE);

        configurator.newConfiguration()
                    .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
                    .paramsAsDto(ImportProgressRecordDto.class)
                    .noResult()
                    .withOperation((endpointId, progress) -> progressConsumer.accept(progress));
    }

    protected void unSubscribeForImportOutputEvents() {
        transmitter.transmitNoneToNone(WS_AGENT_ENDPOINT, EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE);

        handlerRegistry.unregisterNotificationHandler(EVENT_IMPORT_OUTPUT_PROGRESS);
    }
}
