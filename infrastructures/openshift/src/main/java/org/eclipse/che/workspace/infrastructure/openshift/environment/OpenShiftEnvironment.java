/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironment {
    private Map<String, Pod>                   pods;
    private Map<String, Service>               services;
    private Map<String, Route>                 routes;
    private Map<String, PersistentVolumeClaim> persistentVolumeClaims;

    public OpenShiftEnvironment() {
    }

    public Map<String, Pod> getPods() {
        if (pods == null) {
            pods = new HashMap<>();
        }
        return pods;
    }

    public void setPods(Map<String, Pod> pods) {
        this.pods = pods;
    }

    public OpenShiftEnvironment withPods(Map<String, Pod> pods) {
        this.pods = pods;
        return this;
    }

    public Map<String, Service> getServices() {
        if (services == null) {
            services = new HashMap<>();
        }
        return services;
    }

    public void setServices(Map<String, Service> services) {
        this.services = services;
    }

    public OpenShiftEnvironment withServices(Map<String, Service> services) {
        this.services = services;
        return this;
    }

    public Map<String, Route> getRoutes() {
        if (routes == null) {
            routes = new HashMap<>();
        }
        return routes;
    }

    public void setRoutes(Map<String, Route> routes) {
        this.routes = routes;
    }

    public OpenShiftEnvironment withRoutes(Map<String, Route> routes) {
        this.routes = routes;
        return this;
    }

    public Map<String, PersistentVolumeClaim> getPersistentVolumeClaims() {
        if (persistentVolumeClaims == null) {
            persistentVolumeClaims = new HashMap<>();
        }
        return persistentVolumeClaims;
    }

    public void setPersistentVolumeClaims(Map<String, PersistentVolumeClaim> persistentVolumeClaims) {
        this.persistentVolumeClaims = persistentVolumeClaims;
    }

    public OpenShiftEnvironment withPersistentVolumeClaims(Map<String, PersistentVolumeClaim> persistentVolumeClaims) {
        this.persistentVolumeClaims = persistentVolumeClaims;
        return this;
    }
}
