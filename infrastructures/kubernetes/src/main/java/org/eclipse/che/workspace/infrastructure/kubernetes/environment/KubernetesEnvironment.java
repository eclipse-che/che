/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;

/**
 * Holds objects of Kubernetes environment.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironment extends InternalEnvironment {

  public static final String TYPE = "kubernetes";

  private final Map<String, Pod> pods;
  private final Map<String, Service> services;
  private final Map<String, Ingress> ingresses;
  private final Map<String, PersistentVolumeClaim> persistentVolumeClaims;
  private final Map<String, Secret> secrets;
  private final Map<String, ConfigMap> configMaps;

  public KubernetesEnvironment(KubernetesEnvironment k8sEnv) {
    this(
        k8sEnv,
        k8sEnv.getPods(),
        k8sEnv.getServices(),
        k8sEnv.getIngresses(),
        k8sEnv.getPersistentVolumeClaims(),
        k8sEnv.getSecrets(),
        k8sEnv.getConfigMaps());
  }

  @Override
  public KubernetesEnvironment setType(String type) {
    return (KubernetesEnvironment) super.setType(type);
  }

  public static Builder builder() {
    return new Builder();
  }

  protected KubernetesEnvironment(
      InternalRecipe internalRecipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> warnings,
      Map<String, Pod> pods,
      Map<String, Service> services,
      Map<String, Ingress> ingresses,
      Map<String, PersistentVolumeClaim> persistentVolumeClaims,
      Map<String, Secret> secrets,
      Map<String, ConfigMap> configMaps) {
    super(internalRecipe, machines, warnings);
    this.pods = pods;
    this.services = services;
    this.ingresses = ingresses;
    this.persistentVolumeClaims = persistentVolumeClaims;
    this.secrets = secrets;
    this.configMaps = configMaps;
  }

  /** Returns pods that should be created when environment starts. */
  public Map<String, Pod> getPods() {
    return pods;
  }

  /** Returns services that should be created when environment starts. */
  public Map<String, Service> getServices() {
    return services;
  }

  /** Returns ingresses that should be created when environment starts. */
  public Map<String, Ingress> getIngresses() {
    return ingresses;
  }

  /** Returns PVCs that should be created when environment starts. */
  public Map<String, PersistentVolumeClaim> getPersistentVolumeClaims() {
    return persistentVolumeClaims;
  }

  /** Returns secrets that should be created when environment starts. */
  public Map<String, Secret> getSecrets() {
    return secrets;
  }

  /** Returns config maps that should be created when environment starts. */
  public Map<String, ConfigMap> getConfigMaps() {
    return configMaps;
  }

  public static class Builder {
    protected InternalRecipe internalRecipe;
    protected String type = TYPE;
    protected final Map<String, InternalMachineConfig> machines = new HashMap<>();
    protected final List<Warning> warnings = new ArrayList<>();
    protected final Map<String, Pod> pods = new HashMap<>();
    protected final Map<String, Service> services = new HashMap<>();
    protected final Map<String, Ingress> ingresses = new HashMap<>();
    protected final Map<String, PersistentVolumeClaim> pvcs = new HashMap<>();
    protected final Map<String, Secret> secrets = new HashMap<>();
    protected final Map<String, ConfigMap> configMaps = new HashMap<>();
    protected final Map<String, String> attributes = new HashMap<>();

    protected Builder() {}

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
      this.pvcs.putAll(pvcs);
      return this;
    }

    public Builder setSecrets(Map<String, Secret> secrets) {
      this.secrets.putAll(secrets);
      return this;
    }

    public Builder setConfigMaps(Map<String, ConfigMap> configMaps) {
      this.configMaps.putAll(configMaps);
      return this;
    }

    public Builder setAttributes(Map<String, String> attributes) {
      this.attributes.putAll(attributes);
      return this;
    }

    public Builder setType(String type) {
      this.type = type;
      return this;
    }

    public KubernetesEnvironment build() {
      KubernetesEnvironment kubernetesEnvironment =
          new KubernetesEnvironment(
              internalRecipe,
              machines,
              warnings,
              pods,
              services,
              ingresses,
              pvcs,
              secrets,
              configMaps);
      kubernetesEnvironment.setAttributes(attributes);
      kubernetesEnvironment.setType(type);
      return kubernetesEnvironment;
    }
  }
}
