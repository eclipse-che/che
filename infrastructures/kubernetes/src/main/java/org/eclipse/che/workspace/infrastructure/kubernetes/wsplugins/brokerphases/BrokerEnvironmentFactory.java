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
import static java.util.stream.Collectors.toList;

import com.google.common.annotations.Beta;
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
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

/**
 * Creates {@link KubernetesEnvironment} with everything needed to deploy Plugin broker.
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
  private static final String CONTAINER_NAME = "broker";
  private static final String PLUGINS_VOLUME_NAME = "plugins";
  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

  private final String cheWebsocketEndpoint;
  private final String pluginBrokerImage;
  private final String brokerPullPolicy;
  private final AgentAuthEnableEnvVarProvider authEnableEnvVarProvider;
  private final MachineTokenEnvVarProvider machineTokenEnvVarProvider;

  @Inject
  public BrokerEnvironmentFactory(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.workspace.plugin_broker.image") String pluginBrokerImage,
      @Named("che.workspace.plugin_broker.pull_policy") String brokerPullPolicy,
      AgentAuthEnableEnvVarProvider authEnableEnvVarProvider,
      MachineTokenEnvVarProvider machineTokenEnvVarProvider) {
    this.cheWebsocketEndpoint = cheWebsocketEndpoint;
    this.pluginBrokerImage = pluginBrokerImage;
    this.brokerPullPolicy = brokerPullPolicy;
    this.authEnableEnvVarProvider = authEnableEnvVarProvider;
    this.machineTokenEnvVarProvider = machineTokenEnvVarProvider;
  }

  /**
   * Creates {@link KubernetesEnvironment} with everything needed to deploy Plugin broker.
   *
   * @param pluginsMeta meta info of plugins that needs to be resolved by the broker
   * @param runtimeID ID of the runtime the broker would be started
   * @return kubernetes environment (or its extension) with the Plugin broker objects
   */
  public E create(Collection<PluginMeta> pluginsMeta, RuntimeIdentity runtimeID)
      throws InfrastructureException {
    String configMapName = generateUniqueConfigMapName();
    String podName = "che-plugin-broker";
    List<Pair<String, String>> envVars =
        Arrays.asList(
            authEnableEnvVarProvider.get(runtimeID), machineTokenEnvVarProvider.get(runtimeID));
    Pod pod = newPod(podName, runtimeID, configMapName, envVars);
    String machineName = Names.machineName(pod, pod.getSpec().getContainers().get(0));
    InternalMachineConfig machine = new InternalMachineConfig();
    machine.getVolumes().put(PLUGINS_VOLUME_NAME, new VolumeImpl().withPath("/plugins"));

    ConfigMap configMap = newConfigMap(configMapName, pluginsMeta);
    return doCreate(machineName, machine, configMapName, configMap, pod);
  }

  /** Needed to implement this component in both - Kubernetes and Openshift infrastructures. */
  protected abstract E doCreate(
      String machineName,
      InternalMachineConfig machine,
      String configMapName,
      ConfigMap configMap,
      Pod pod);

  private String generateUniqueConfigMapName() {
    return NameGenerator.generate(CONFIG_MAP_NAME_SUFFIX, 6);
  }

  private Pod newPod(
      String podName,
      RuntimeIdentity runtimeId,
      String configMapName,
      List<Pair<String, String>> envVars) {
    final Container container =
        new ContainerBuilder()
            .withName(CONTAINER_NAME)
            .withImage(pluginBrokerImage)
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
            .withVolumeMounts(new VolumeMount(CONF_FOLDER + "/", BROKER_VOLUME, true, null))
            .withEnv(envVars.stream().map(this::asEnvVar).collect(toList()))
            .withNewResources()
            .endResources()
            .build();
    Containers.addRamLimit(container, "250Mi");
    Containers.addRamRequest(container, "250Mi");
    return new PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .withContainers(container)
        .withVolumes(
            new VolumeBuilder()
                .withName(BROKER_VOLUME)
                .withNewConfigMap()
                .withName(configMapName)
                .endConfigMap()
                .build())
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
}
