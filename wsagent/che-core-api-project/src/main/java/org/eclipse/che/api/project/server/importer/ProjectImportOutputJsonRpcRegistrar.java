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
package org.eclipse.che.api.project.server.importer;

import org.eclipse.che.api.core.jsonrpc.RequestHandlerConfigurator;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Set;

import static com.google.common.collect.Sets.newConcurrentHashSet;

/**
 * Endpoint registry for broadcasting project import events. Holds registered client's endpoint ids.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
@Singleton
public class ProjectImportOutputJsonRpcRegistrar {

    private static final String EVENT_IMPORT_OUTPUT_SUBSCRIBE    = "event:import-project:subscribe";
    private static final String EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE = "event:import-project:un-subscribe";

    private final Set<String> endpointIds = newConcurrentHashSet();

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(EVENT_IMPORT_OUTPUT_SUBSCRIBE)
                    .paramsAsEmpty()
                    .noResult()
                    .withConsumer((endpointId, aVoid) -> endpointIds.add(endpointId));
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE)
                    .paramsAsEmpty()
                    .noResult()
                    .withConsumer((endpointId, aVoid) -> endpointIds.remove(endpointId));
    }

    public Set<String> getRegisteredEndpoints() {
        return newConcurrentHashSet(endpointIds);
    }
}
