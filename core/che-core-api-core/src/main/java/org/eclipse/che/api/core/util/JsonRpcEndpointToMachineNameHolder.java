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
package org.eclipse.che.api.core.util;

import org.eclipse.che.api.core.jsonrpc.RequestHandlerConfigurator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newConcurrentHashSet;

@Singleton
public class JsonRpcEndpointToMachineNameHolder {
    private final Map<String, Set<String>> endpointIds = new ConcurrentHashMap<>();

    @Inject
    private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {

        configurator.newConfiguration()
                    .methodName("event:environment-output:subscribe-by-machine-name")
                    .paramsAsString()
                    .noResult()
                    .withConsumer((endpointId, workspaceIdPlusMachineName) -> {
                        endpointIds.putIfAbsent(endpointId, newConcurrentHashSet());
                        endpointIds.get(endpointId).add(workspaceIdPlusMachineName);
                    });
    }

    @Inject
    private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                    .methodName("event:environment-output:un-subscribe-by-machine-name")
                    .paramsAsString()
                    .noResult()
                    .withConsumer((endpointId, workspaceIdPlusMachineName) -> {
                        Set<String> workspaceIds = endpointIds.get(endpointId);
                        if (workspaceIds != null) {
                            workspaceIds.remove(workspaceIdPlusMachineName);

                            if (workspaceIds.isEmpty()) {
                                endpointIds.remove(endpointId);
                            }
                        }
                    });
    }

    public Set<String> getEndpointIdsByWorkspaceIdPlusMachineName(String workspaceIdPlusMachineName){
        return endpointIds.entrySet()
                          .stream()
                          .filter(it -> it.getValue().contains(workspaceIdPlusMachineName))
                          .map(Map.Entry::getKey)
                          .collect(Collectors.toSet());
    }
}
