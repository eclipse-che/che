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
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.slf4j.Logger;

/**
 * Deploys Che plugin broker in a workspace, calls next {@link BrokerPhase} and removes deployment
 * after next phase completes.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class DeployBroker extends BrokerPhase {

  private static final Logger LOG = getLogger(DeployBroker.class);

  private final String workspaceId;
  private final String cheWebsocketEndpoint;
  private final String confFolder;
  private final String configFile;
  private final String pvcClaimProjects;
  private final String brokerVolume;
  private final String configMapName;
  private final String pluginBrokerImage;
  private final KubernetesNamespace kubernetesNamespace;
  private final List<EnvVar> envVars;

  public DeployBroker(
      KubernetesNamespace kubernetesNamespace,
      String workspaceId,
      String cheWebsocketEndpoint,
      String confFolder,
      String configFile,
      String pvcClaimProjects,
      String brokerVolume,
      String configMapName,
      String pluginBrokerImage,
      List<Pair<String, String>> envVars) {
    this.kubernetesNamespace = kubernetesNamespace;
    this.workspaceId = workspaceId;
    this.cheWebsocketEndpoint = cheWebsocketEndpoint;
    this.confFolder = confFolder;
    this.configFile = configFile;
    this.pvcClaimProjects = pvcClaimProjects;
    this.brokerVolume = brokerVolume;
    this.configMapName = configMapName;
    this.pluginBrokerImage = pluginBrokerImage;
    this.envVars = envVars.stream().map(this::asEnvVar).collect(toList());
  }

  @Override
  public List<ChePlugin> execute() throws InfrastructureException {
    final String podName = "che-plugin-broker-" + workspaceId;
    final Pod pod = newPod(podName, workspaceId);
    KubernetesDeployments deployments = kubernetesNamespace.deployments();
    try {
      deployments.create(pod);

      return nextPhase.execute();
    } finally {
      try {
        deployments.delete(podName);
      } catch (InfrastructureException e) {
        LOG.error("Broker deployment removal failed. Error: " + e.getLocalizedMessage(), e);
      }
    }
  }

  private Pod newPod(String podName, String workspaceId) {
    final Container container =
        new ContainerBuilder()
            .withName(podName)
            .withImage(pluginBrokerImage)
            .withArgs(
                "-metas",
                confFolder + "/" + configFile,
                "-push-endpoint",
                cheWebsocketEndpoint,
                "-workspace-id",
                workspaceId)
            .withImagePullPolicy("Always")
            .withVolumeMounts(
                newVolumeMount(pvcClaimProjects, "/plugins", workspaceId + "/plugins"),
                new VolumeMount(confFolder + "/", brokerVolume, true, null))
            .withEnv(envVars)
            .withNewResources()
            .withLimits(singletonMap("memory", new Quantity("250Mi")))
            .endResources()
            .build();
    return new PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .withContainers(container)
        .withVolumes(
            newVolume(pvcClaimProjects, pvcClaimProjects),
            new VolumeBuilder()
                .withName(brokerVolume)
                .withNewConfigMap()
                .withName(configMapName)
                .endConfigMap()
                .build())
        .withRestartPolicy("Never")
        .endSpec()
        .build();
  }

  private EnvVar asEnvVar(Pair<String, String> envVar) {
    return new EnvVarBuilder().withName(envVar.first).withValue(envVar.second).build();
  }
}
