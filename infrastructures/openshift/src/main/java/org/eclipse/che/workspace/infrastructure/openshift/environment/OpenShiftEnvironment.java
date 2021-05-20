/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
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
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
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

  /** Returns builder for creating environment from blank {@link KubernetesEnvironment}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns builder for creating environment based on specified {@link InternalEnvironment}.
   *
   * <p>It means that {@link InternalEnvironment} specific fields like machines, warnings will be
   * preconfigured in Builder.
   */
  public static Builder builder(InternalEnvironment internalEnvironment) {
    return new Builder(internalEnvironment);
  }

  public OpenShiftEnvironment(KubernetesEnvironment k8sEnv) {
    super(k8sEnv);
    setType(TYPE);
    this.routes = new HashMap<>();
  }

  public OpenShiftEnvironment(
      InternalEnvironment internalEnvironment,
      Map<String, Pod> pods,
      Map<String, Deployment> deployments,
      Map<String, Service> services,
      Map<String, Ingress> ingresses,
      Map<String, PersistentVolumeClaim> pvcs,
      Map<String, Secret> secrets,
      Map<String, ConfigMap> configMaps,
      Map<String, Route> routes) {
    super(internalEnvironment, pods, deployments, services, ingresses, pvcs, secrets, configMaps);
    setType(TYPE);
    this.routes = routes;
  }

  public OpenShiftEnvironment(
      InternalRecipe internalRecipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings,
      Map<String, Pod> pods,
      Map<String, Deployment> deployments,
      Map<String, Service> services,
      Map<String, Ingress> ingresses,
      Map<String, PersistentVolumeClaim> pvcs,
      Map<String, Secret> secrets,
      Map<String, ConfigMap> configMaps,
      Map<String, Route> routes) {
    super(
        internalRecipe,
        machines,
        warnings,
        pods,
        deployments,
        services,
        ingresses,
        pvcs,
        secrets,
        configMaps);
    setType(TYPE);
    this.routes = routes;
  }

  @Override
  public OpenShiftEnvironment setType(String type) {
    return (OpenShiftEnvironment) super.setType(type);
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Route> getRoutes() {
    return routes;
  }

  public static class Builder extends KubernetesEnvironment.Builder {
    private final Map<String, Route> routes = new HashMap<>();

    private Builder() {}

    private Builder(InternalEnvironment internalEnvironment) {
      super(internalEnvironment);
    }

    @Override
    public Builder setInternalRecipe(InternalRecipe internalRecipe) {
      super.setInternalRecipe(internalRecipe);
      return this;
    }

    @Override
    public Builder setMachines(Map<String, InternalMachineConfig> machines) {
      super.setMachines(machines);
      return this;
    }

    @Override
    public Builder setWarnings(List<Warning> warnings) {
      super.setWarnings(warnings);
      return this;
    }

    @Override
    public Builder setCommands(List<? extends Command> commands) {
      super.setCommands(new ArrayList<>(commands));
      return this;
    }

    @Override
    public Builder setPods(Map<String, Pod> pods) {
      this.pods.putAll(pods);
      return this;
    }

    @Override
    public Builder setDeployments(Map<String, Deployment> deployments) {
      this.deployments.putAll(deployments);
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

    @Override
    public Builder setAttributes(Map<String, String> attributes) {
      this.attributes.putAll(attributes);
      return this;
    }

    public Builder setRoutes(Map<String, Route> route) {
      this.routes.putAll(route);
      return this;
    }

    public OpenShiftEnvironment build() {
      return new OpenShiftEnvironment(
          internalEnvironment,
          pods,
          deployments,
          services,
          ingresses,
          pvcs,
          secrets,
          configMaps,
          routes);
    }
  }
}
