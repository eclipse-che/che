/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Holds objects of OpenShift environment.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironment extends KubernetesEnvironment {

  public static final String TYPE = "openshift";

  private final Map<String, Route> routes;

  public OpenShiftEnvironment(KubernetesEnvironment k8sEnv) {
    super(k8sEnv);
    this.routes = new HashMap<>();
  }

  public static Builder builder() {
    return new Builder();
  }

  public OpenShiftEnvironment(
      InternalRecipe internalRecipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings,
      Map<String, Pod> pods,
      Map<String, Service> services,
      Map<String, Ingress> ingresses,
      Map<String, PersistentVolumeClaim> persistentVolumeClaims,
      Map<String, Route> routes) {
    super(internalRecipe, machines, warnings, pods, services, ingresses, persistentVolumeClaims);
    this.routes = routes;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Route> getRoutes() {
    return routes;
  }

  public static class Builder extends KubernetesEnvironment.Builder {
    private final Map<String, Route> routes = new HashMap<>();

    private Builder() {}

    public Builder setInternalRecipe(InternalRecipe internalRecipe) {
      this.internalRecipe = internalRecipe;
      return this;
    }

    public Builder setMachines(Map<String, InternalMachineConfig> machines) {
      this.machines.putAll(machines);
      return this;
    }

    public Builder setWarnings(List<Warning> warnings) {
      this.warnings.addAll(warnings);
      return this;
    }

    public Builder setPods(Map<String, Pod> pods) {
      this.pods.putAll(pods);
      return this;
    }

    public Builder setServices(Map<String, Service> services) {
      this.services.putAll(services);
      return this;
    }

    public Builder setIngresses(Map<String, Ingress> ingresses) {
      this.ingresses.putAll(ingresses);
      return this;
    }

    public Builder setPersistentVolumeClaims(Map<String, PersistentVolumeClaim> pvcs) {
      this.persistentVolumeClaims.putAll(pvcs);
      return this;
    }

    public Builder setRoutes(Map<String, Route> route) {
      this.routes.putAll(route);
      return this;
    }

    public OpenShiftEnvironment build() {
      return new OpenShiftEnvironment(
          internalRecipe,
          machines,
          warnings,
          pods,
          services,
          ingresses,
          persistentVolumeClaims,
          routes);
    }
  }
}
