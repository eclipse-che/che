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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import static java.util.Collections.singletonMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * Creates {@link KubernetesEnvironment} with everything needed to deploy Plugin brokers.
 *
 * <p>It has to be extended to be used in the kubernetes or openshift infrastructures because of the
 * usage of a complex inheritance between components of these infrastructures.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public abstract class BrokerEnvironmentFactory<E extends KubernetesEnvironment> {

  @VisibleForTesting static final String CONFIG_MAP_NAME_SUFFIX = "broker-config-map";
  @VisibleForTesting static final String CONFIG_FILE = "config.json";
  private static final String BROKER_VOLUME = "broker-config-volume";
  private static final String CONF_FOLDER = "/broker-config";
  private static final String PLUGINS_VOLUME_NAME = "plugins";
  private static final String BROKERS_POD_NAME = "che-plugin-broker";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String cheWebsocketEndpoint;
  private final String brokerPullPolicy;
  private final AgentAuthEnableEnvVarProvider authEnableEnvVarProvider;
  private final MachineTokenEnvVarProvider machineTokenEnvVarProvider;
  private final String artifactsBrokerImage;
  private final String metadataBrokerImage;
  private final String pluginRegistryUrl;
  private final CertificateProvisioner certProvisioner;

  public BrokerEnvironmentFactory(
      String cheWebsocketEndpoint,
      String brokerPullPolicy,
      AgentAuthEnableEnvVarProvider authEnableEnvVarProvider,
      MachineTokenEnvVarProvider machineTokenEnvVarProvider,
      String artifactsBrokerImage,
      String metadataBrokerImage,
      String pluginRegistryUrl,
      CertificateProvisioner certProvisioner) {
    this.cheWebsocketEndpoint = cheWebsocketEndpoint;
    this.brokerPullPolicy = brokerPullPolicy;
    this.authEnableEnvVarProvider = authEnableEnvVarProvider;
    this.machineTokenEnvVarProvider = machineTokenEnvVarProvider;
    this.artifactsBrokerImage = artifactsBrokerImage;
    this.metadataBrokerImage = metadataBrokerImage;
    this.pluginRegistryUrl = pluginRegistryUrl;
    this.certProvisioner = certProvisioner;
  }

  /**
   * Creates {@link KubernetesEnvironment} with everything needed to deploy metadata plugin broker.
   *
   * @param pluginFQNs fully qualified names of plugins that needs to be resolved by the broker
   * @param runtimeID ID of the runtime the broker would be started
   * @return kubernetes environment (or its extension) with the Plugin broker objects
   */
  public E createForMetadataBroker(Collection<PluginFQN> pluginFQNs, RuntimeIdentity runtimeID)
      throws InfrastructureException {
    BrokersConfigs brokersConfigs = getBrokersConfigs(pluginFQNs, runtimeID, metadataBrokerImage);
    return doCreate(brokersConfigs);
  }

  /**
   * Creates {@link KubernetesEnvironment} with everything needed to deploy artifacts plugin broker.
   *
   * @param pluginFQNs fully qualified names of plugins that needs to be resolved by the broker
   * @param runtimeID ID of the runtime the broker would be started
   * @return kubernetes environment (or its extension) with the Plugin broker objects
   */
  public E createForArtifactsBroker(Collection<PluginFQN> pluginFQNs, RuntimeIdentity runtimeID)
      throws InfrastructureException {
    BrokersConfigs brokersConfigs = getBrokersConfigs(pluginFQNs, runtimeID, artifactsBrokerImage);
    brokersConfigs
        .machines
        .values()
        .forEach(
            m -> m.getVolumes().put(PLUGINS_VOLUME_NAME, new VolumeImpl().withPath("/plugins")));
    return doCreate(brokersConfigs);
  }

  private BrokersConfigs getBrokersConfigs(
      Collection<PluginFQN> pluginFQNs, RuntimeIdentity runtimeID, String brokerImage)
      throws InfrastructureException {

    String configMapName = generateUniqueName(CONFIG_MAP_NAME_SUFFIX);
    String configMapVolume = generateUniqueName(BROKER_VOLUME);
    ConfigMap configMap = newConfigMap(configMapName, pluginFQNs);

    Pod pod = newPod();
    List<EnvVar> envVars =
        Stream.of(
                authEnableEnvVarProvider.get(runtimeID), machineTokenEnvVarProvider.get(runtimeID))
            .map(this::asEnvVar)
            .collect(Collectors.toList());
    Container container = newContainer(runtimeID, envVars, brokerImage, configMapVolume);
    pod.getSpec().getContainers().add(container);
    pod.getSpec().getVolumes().add(newConfigMapVolume(configMapName, configMapVolume));

    InternalMachineConfig machineConfig = new InternalMachineConfig();
    String machineName = Names.machineName(pod.getMetadata(), container);

    BrokersConfigs configs = new BrokersConfigs();
    configs.configMaps = singletonMap(configMapName, configMap);
    configs.machines = singletonMap(machineName, machineConfig);
    configs.pods = singletonMap(pod.getMetadata().getName(), pod);

    return configs;
  }

  /** Needed to implement this component in both - Kubernetes and Openshift infrastructures. */
  protected abstract E doCreate(BrokersConfigs brokerConfigs);

  private Container newContainer(
      RuntimeIdentity runtimeId,
      List<EnvVar> envVars,
      String image,
      @Nullable String brokerVolumeName) {
    String containerName = generateContainerNameFromImageRef(image);
    final ContainerBuilder cb =
        new ContainerBuilder()
            .withName(containerName)
            .withImage(image)
            .withArgs(
                "-push-endpoint",
                cheWebsocketEndpoint,
                "-runtime-id",
                String.format(
                    "%s:%s:%s",
                    runtimeId.getWorkspaceId(),
                    MoreObjects.firstNonNull(runtimeId.getEnvName(), ""),
                    runtimeId.getOwnerId()),
                "-cacert",
                certProvisioner.isConfigured() ? certProvisioner.getCertPath() : "",
                "--registry-address",
                Strings.nullToEmpty(pluginRegistryUrl))
            .withImagePullPolicy(brokerPullPolicy)
            .withEnv(envVars);
    if (brokerVolumeName != null) {
      cb.withVolumeMounts(new VolumeMount(CONF_FOLDER + "/", null, brokerVolumeName, true, null));
      cb.addToArgs("-metas", CONF_FOLDER + "/" + CONFIG_FILE);
    }
    Container container = cb.build();
    Containers.addRamLimit(container, "250Mi");
    Containers.addRamRequest(container, "250Mi");
    return container;
  }

  private Pod newPod() {
    return new PodBuilder()
        .withNewMetadata()
        .withName(BROKERS_POD_NAME)
        .endMetadata()
        .withNewSpec()
        .withRestartPolicy("Never")
        .endSpec()
        .build();
  }

  private ConfigMap newConfigMap(String configMapName, Collection<PluginFQN> pluginFQNs)
      throws InternalInfrastructureException {
    try {
      return new ConfigMapBuilder()
          .withNewMetadata()
          .withName(configMapName)
          .endMetadata()
          .withData(singletonMap(CONFIG_FILE, objectMapper.writeValueAsString(pluginFQNs)))
          .build();
    } catch (JsonProcessingException e) {
      throw new InternalInfrastructureException(e.getMessage(), e);
    }
  }

  private Volume newConfigMapVolume(String configMapName, String configMapVolume) {
    return new VolumeBuilder()
        .withName(configMapVolume)
        .withNewConfigMap()
        .withName(configMapName)
        .endConfigMap()
        .build();
  }

  private EnvVar asEnvVar(Pair<String, String> envVar) {
    return new EnvVarBuilder().withName(envVar.first).withValue(envVar.second).build();
  }

  private String generateUniqueName(String suffix) {
    return NameGenerator.generate(suffix, 6);
  }

  /**
   * Generate a container name from an image reference. Since full image references can be over 63
   * characters, we need to strip registry and organization from the image reference to limit name
   * length.
   */
  @VisibleForTesting
  protected String generateContainerNameFromImageRef(String image) {
    return image.toLowerCase().replaceAll("[^/]*/", "").replaceAll("[^\\d\\w-]", "-");
  }

  public static class BrokersConfigs {
    public Map<String, InternalMachineConfig> machines;
    public Map<String, ConfigMap> configMaps;
    public Map<String, Pod> pods;
  }
}
