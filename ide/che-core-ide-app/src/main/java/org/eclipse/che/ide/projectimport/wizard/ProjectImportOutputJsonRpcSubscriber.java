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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.ImportProgressRecord;
import org.eclipse.che.api.project.shared.dto.ImportProgressRecordDto;

import java.util.function.Consumer;

import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_PROGRESS;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE;

/**
 * Json RPC subscriber for listening to the project import events. Register itself for the listening events from the server side.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
@Singleton
public class ProjectImportOutputJsonRpcSubscriber {

    public static final String WS_AGENT_ENDPOINT = "ws-agent";

    private final RequestTransmitter         transmitter;
    private final RequestHandlerConfigurator configurator;
    private final RequestHandlerManager      requestHandlerManager;

    @Inject
    public ProjectImportOutputJsonRpcSubscriber(RequestTransmitter transmitter,
                                                RequestHandlerConfigurator configurator,
                                                RequestHandlerManager requestHandlerManager) {
        this.transmitter = transmitter;
        this.configurator = configurator;
        this.requestHandlerManager = requestHandlerManager;
    }

    protected void subscribeForImportOutputEvents(Consumer<ImportProgressRecord> progressConsumer) {
        transmitter.newRequest().endpointId(WS_AGENT_ENDPOINT).methodName(EVENT_IMPORT_OUTPUT_SUBSCRIBE).noParams().sendAndSkipResult();

        configurator.newConfiguration()
                    .methodName(EVENT_IMPORT_OUTPUT_PROGRESS)
                    .paramsAsDto(ImportProgressRecordDto.class)
                    .noResult()
                    .withConsumer(progress -> progressConsumer.accept(progress));
    }

    protected void unSubscribeForImportOutputEvents() {
        transmitter.newRequest().endpointId(WS_AGENT_ENDPOINT).methodName(EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE).noParams().sendAndSkipResult();

        requestHandlerManager.deregister(EVENT_IMPORT_OUTPUT_PROGRESS);
    }
}
