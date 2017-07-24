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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftEnvironment {
    private Map<String, Pod>     pods;
    private Map<String, Service> services;
    private Map<String, Route>   routes;

    public OpenshiftEnvironment() {
        routes = new HashMap<>();
        services = new HashMap<>();
        pods = new HashMap<>();
    }

    public Map<String, Pod> getPods() {
        return ImmutableMap.copyOf(pods);
    }

    public void addPod(Pod pod) {
        pods.put(pod.getMetadata().getName(), pod);
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public void addService(Service service) {
        services.put(service.getMetadata().getName(), service);
    }

    public Map<String, Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        routes.put(route.getMetadata().getName(), route);
    }
}
