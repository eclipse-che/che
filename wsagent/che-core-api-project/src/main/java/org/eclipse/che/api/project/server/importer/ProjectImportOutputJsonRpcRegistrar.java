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

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Set;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.api.project.shared.Constants.EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE;

/**
 * Endpoint registry for broadcasting project import events. Holds registered client's endpoint ids.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
@Singleton
public class ProjectImportOutputJsonRpcRegistrar {

    private final Set<String> endpointIds = newConcurrentHashSet();

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(EVENT_IMPORT_OUTPUT_SUBSCRIBE)
                    .noParams()
                    .noResult()
                    .withConsumer(endpointId -> endpointIds.add(endpointId));
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName(EVENT_IMPORT_OUTPUT_UN_SUBSCRIBE)
                    .noParams()
                    .noResult()
                    .withConsumer(endpointId -> endpointIds.remove(endpointId));
    }

    public Set<String> getRegisteredEndpoints() {
        return newConcurrentHashSet(endpointIds);
    }
}
