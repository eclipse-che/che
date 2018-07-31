/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.Builder;

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
      Map<String, PersistentVolumeClaim> pvcs,
      Map<String, Secret> secrets,
      Map<String, ConfigMap> configMaps,
      Map<String, Route> routes) {
    super(internalRecipe, machines, warnings, pods, services, ingresses, pvcs, secrets, configMaps);
    this.routes = routes;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Route> getRoutes() {
    return routes;
  }

  public static class Builder extends KubernetesEnvironment.Builder {
    private final Map<String, Route> routes = new HashMap<>();

    private Builder() {}

    @Override
    public Builder setInternalRecipe(InternalRecipe internalRecipe) {
      this.internalRecipe = internalRecipe;
      return this;
    }

    @Override
    public Builder setMachines(Map<String, InternalMachineConfig> machines) {
      this.machines.putAll(machines);
      return this;
    }

    @Override
    public Builder setWarnings(List<Warning> warnings) {
      this.warnings.addAll(warnings);
      return this;
    }

    @Override
    public Builder setPods(Map<String, Pod> pods) {
      this.pods.putAll(pods);
      return this;
    }

    @Override
    public Builder setServices(Map<String, Service> services) {
      this.services.putAll(services);
      return this;
    }

    @Override
    public Builder setIngresses(Map<String, Ingress> ingresses) {
      this.ingresses.putAll(ingresses);
      return this;
    }

    @Override
    public Builder setPersistentVolumeClaims(Map<String, PersistentVolumeClaim> pvcs) {
      this.pvcs.putAll(pvcs);
      return this;
    }

    @Override
    public Builder setSecrets(Map<String, Secret> secrets) {
      this.secrets.putAll(secrets);
      return this;
    }

    @Override
    public Builder setConfigMaps(Map<String, ConfigMap> configMaps) {
      this.configMaps.putAll(configMaps);
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
          pvcs,
          secrets,
          configMaps,
          routes);
    }
  }
}
