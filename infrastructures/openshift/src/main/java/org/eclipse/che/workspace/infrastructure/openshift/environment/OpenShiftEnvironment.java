/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds objects of OpenShift environment.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironment {

  private final Map<String, Pod> pods;
  private final Map<String, Service> services;
  private final Map<String, Route> routes;
  private final Map<String, PersistentVolumeClaim> persistentVolumeClaims;

  public static Builder builder() {
    return new Builder();
  }

  private OpenShiftEnvironment(
      Map<String, Pod> pods,
      Map<String, Service> services,
      Map<String, Route> routes,
      Map<String, PersistentVolumeClaim> persistentVolumeClaims) {
    this.pods = pods;
    this.services = services;
    this.routes = routes;
    this.persistentVolumeClaims = persistentVolumeClaims;
  }

  /** Returns pods that should be created when environment starts. */
  public Map<String, Pod> getPods() {
    return pods;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Service> getServices() {
    return services;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Route> getRoutes() {
    return routes;
  }

  /** Returns PVCs that should be created when environment starts. */
  public Map<String, PersistentVolumeClaim> getPersistentVolumeClaims() {
    return persistentVolumeClaims;
  }

  public static class Builder {
    private final Map<String, Pod> pods = new HashMap<>();
    private final Map<String, Service> services = new HashMap<>();
    private final Map<String, Route> routes = new HashMap<>();
    private final Map<String, PersistentVolumeClaim> persistentVolumeClaims = new HashMap<>();

    private Builder() {}

    public Builder setPods(Map<String, Pod> pods) {
      this.pods.putAll(pods);
      return this;
    }

    public Builder setServices(Map<String, Service> services) {
      this.services.putAll(services);
      return this;
    }

    public Builder setRoutes(Map<String, Route> route) {
      this.routes.putAll(route);
      return this;
    }

    public Builder setPersistentVolumeClaims(Map<String, PersistentVolumeClaim> pvcs) {
      this.persistentVolumeClaims.putAll(pvcs);
      return this;
    }

    public OpenShiftEnvironment build() {
      return new OpenShiftEnvironment(pods, services, routes, persistentVolumeClaims);
    }
  }
}
