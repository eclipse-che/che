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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.BrokersResult;

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

  private static final String CONFIG_MAP_NAME_SUFFIX = "broker-config-map";
  private static final String BROKER_VOLUME = "broker-config-volume";
  private static final String CONF_FOLDER = "/broker-config";
  private static final String CONFIG_FILE = "config.json";
  private static final String CONTAINER_NAME_SUFFIX = "broker";
  private static final String PLUGINS_VOLUME_NAME = "plugins";
  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private final String cheWebsocketEndpoint;
  private final String brokerPullPolicy;
  private final AgentAuthEnableEnvVarProvider authEnableEnvVarProvider;
  private final MachineTokenEnvVarProvider machineTokenEnvVarProvider;
  private final Map<String, String> pluginTypeToImage;

  @Inject
  public BrokerEnvironmentFactory(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.workspace.plugin_broker.pull_policy") String brokerPullPolicy,
      AgentAuthEnableEnvVarProvider authEnableEnvVarProvider,
      MachineTokenEnvVarProvider machineTokenEnvVarProvider,
      @Named("che.workspace.plugin_broker.images") Map<String, String> pluginTypeToImage) {
    this.cheWebsocketEndpoint = cheWebsocketEndpoint;
    this.brokerPullPolicy = brokerPullPolicy;
    this.authEnableEnvVarProvider = authEnableEnvVarProvider;
    this.machineTokenEnvVarProvider = machineTokenEnvVarProvider;
    this.pluginTypeToImage = pluginTypeToImage;
  }

  /**
   * Creates {@link KubernetesEnvironment} with everything needed to deploy Plugin broker.
   *
   * @param pluginsMeta meta info of plugins that needs to be resolved by the broker
   * @param runtimeID ID of the runtime the broker would be started
   * @param brokersResult needs to be called with {@link BrokersResult#oneMoreBroker()} for each
   * broker to allow proper waiting of execution of all the brokers
   * @return kubernetes environment (or its extension) with the Plugin broker objects
   */
  public E create(
      Collection<PluginMeta> pluginsMeta, RuntimeIdentity runtimeID, BrokersResult brokersResult)
      throws InfrastructureException {

    BrokersConfigs brokersConfigs = new BrokersConfigs();
    Pod pod = brokersConfigs.pod = newPod();
    brokersConfigs.configMaps = new HashMap<>();
    brokersConfigs.machines = new HashMap<>();

    PodSpec spec = pod.getSpec();
    List<EnvVar> envVars =
        Stream.of(
                authEnableEnvVarProvider.get(runtimeID), machineTokenEnvVarProvider.get(runtimeID))
            .map(this::asEnvVar)
            .collect(Collectors.toList());

    Multimap<String, PluginMeta> brokersImageToMetas = sortByBrokerImage(pluginsMeta);
    for (Entry<String, Collection<PluginMeta>> brokerImageToMetas :
        brokersImageToMetas.asMap().entrySet()) {
      BrokerConfig brokerConfig =
          createBrokerConfig(
              runtimeID, brokerImageToMetas.getValue(), envVars, brokerImageToMetas.getKey(), pod);

      brokersConfigs.machines.put(brokerConfig.machineName, brokerConfig.machineConfig);
      brokersConfigs.configMaps.put(brokerConfig.configMapName, brokerConfig.configMap);
      spec.getContainers().add(brokerConfig.container);
      spec.getVolumes()
          .add(
              new VolumeBuilder()
                  .withName(brokerConfig.configMapVolume)
                  .withNewConfigMap()
                  .withName(brokerConfig.configMapName)
                  .endConfigMap()
                  .build());

      brokersResult.oneMoreBroker();
    }
    return doCreate(brokersConfigs);
  }

  /** Needed to implement this component in both - Kubernetes and Openshift infrastructures. */
  protected abstract E doCreate(BrokersConfigs brokersConfigs);

  private String generateUniqueName(String suffix) {
    return NameGenerator.generate(suffix, 6);
  }

  private Container newContainer(
      RuntimeIdentity runtimeId, List<EnvVar> envVars, String image, String brokerVolumeName) {
    final Container container =
        new ContainerBuilder()
            .withName(generateUniqueName(CONTAINER_NAME_SUFFIX))
            .withImage(image)
            .withArgs(
                "-metas",
                CONF_FOLDER + "/" + CONFIG_FILE,
                "-push-endpoint",
                cheWebsocketEndpoint,
                "-runtime-id",
                String.format(
                    "%s:%s:%s",
                    runtimeId.getWorkspaceId(), runtimeId.getEnvName(), runtimeId.getOwnerId()))
            .withImagePullPolicy(brokerPullPolicy)
            .withVolumeMounts(new VolumeMount(CONF_FOLDER + "/", brokerVolumeName, true, null))
            .withEnv(envVars)
            .withNewResources()
            .endResources()
            .build();
    Containers.addRamLimit(container, "250Mi");
    Containers.addRamRequest(container, "250Mi");
    return container;
  }

  private Pod newPod() {
    return new PodBuilder()
        .withNewMetadata()
        .withName("che-plugin-broker")
        .endMetadata()
        .withNewSpec()
        .withRestartPolicy("Never")
        .endSpec()
        .build();
  }

  private ConfigMap newConfigMap(String configMapName, Collection<PluginMeta> pluginsMetas) {
    return new ConfigMapBuilder()
        .withNewMetadata()
        .withName(configMapName)
        .endMetadata()
        .withData(singletonMap(CONFIG_FILE, GSON.toJson(pluginsMetas)))
        .build();
  }

  private EnvVar asEnvVar(Pair<String, String> envVar) {
    return new EnvVarBuilder().withName(envVar.first).withValue(envVar.second).build();
  }

  private BrokerConfig createBrokerConfig(
      RuntimeIdentity runtimeId,
      Collection<PluginMeta> pluginsMeta,
      List<EnvVar> envVars,
      String image,
      Pod pod) {

    BrokerConfig data = new BrokerConfig();
    data.configMapName = generateUniqueName(CONFIG_MAP_NAME_SUFFIX);
    data.configMapVolume = generateUniqueName(BROKER_VOLUME);
    data.configMap = newConfigMap(data.configMapName, pluginsMeta);
    data.container = newContainer(runtimeId, envVars, image, data.configMapVolume);
    data.machineName = Names.machineName(pod, data.container);
    data.machineConfig = new InternalMachineConfig();
    data.machineConfig.getVolumes().put(PLUGINS_VOLUME_NAME, new VolumeImpl().withPath("/plugins"));

    return data;
  }

  private Multimap<String, PluginMeta> sortByBrokerImage(Collection<PluginMeta> pluginMetas)
      throws InfrastructureException {
    Multimap<String, PluginMeta> sortedPlugins = ArrayListMultimap.create();
    for (PluginMeta pluginMeta : pluginMetas) {
      String type = pluginMeta.getType();
      if (isNullOrEmpty(type)) {
        throw new InfrastructureException(
            format(
                "Plugin '%s:%s' has invalid type '%s'",
                pluginMeta.getId(), pluginMeta.getVersion(), type));
      }
      String image = pluginTypeToImage.get(type);
      if (isNullOrEmpty(image)) {
        throw new InfrastructureException(
            format(
                "Plugin '%s:%s' has unsupported type '%s'",
                pluginMeta.getId(), pluginMeta.getVersion(), type));
      }
      sortedPlugins.put(image, pluginMeta);
    }

    return sortedPlugins;
  }

  private static class BrokerConfig {
    String configMapName;
    ConfigMap configMap;
    Container container;
    String machineName;
    InternalMachineConfig machineConfig;
    String configMapVolume;
  }

  public static class BrokersConfigs {
    public Map<String, InternalMachineConfig> machines;
    public Map<String, ConfigMap> configMaps;
    public Pod pod;
  }
}
