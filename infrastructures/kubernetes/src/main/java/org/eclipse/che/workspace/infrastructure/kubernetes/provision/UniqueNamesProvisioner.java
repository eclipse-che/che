/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapEnvSource;
import io.fabric8.kubernetes.api.model.ConfigMapKeySelector;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSource;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Makes names of Kubernetes pods, ingresses and config maps unique whole namespace by {@link
 * Names}.
 *
 * <p>Original names will be stored in {@link Constants#CHE_ORIGINAL_NAME_LABEL} label of renamed
 * object.
 *
 * @author Anton Korneta
 * @see Names#uniqueResourceName(String, String)
 * @see Names#generateName(String)
 */
@Singleton
public class UniqueNamesProvisioner<T extends KubernetesEnvironment>
    implements ConfigurationProvisioner<T> {

  @Override
  @Traced
  public void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();

    TracingTags.WORKSPACE_ID.set(workspaceId);

    final Map<String, ConfigMap> configMaps = k8sEnv.getConfigMaps();
    Map<String, String> configMapNameTranslation = new HashMap<>();
    for (ConfigMap configMap : configMaps.values()) {
      final String originalName = configMap.getMetadata().getName();
      putLabel(configMap.getMetadata(), Constants.CHE_ORIGINAL_NAME_LABEL, originalName);
      final String uniqueName = Names.uniqueResourceName(originalName, workspaceId);
      configMap.getMetadata().setName(uniqueName);
      configMapNameTranslation.put(originalName, uniqueName);
    }

    final Collection<PodData> podData = k8sEnv.getPodsData().values();
    for (PodData pod : podData) {
      final ObjectMeta podMeta = pod.getMetadata();
      putLabel(podMeta, Constants.CHE_ORIGINAL_NAME_LABEL, podMeta.getName());
      final String podName = Names.uniqueResourceName(podMeta.getName(), workspaceId);
      podMeta.setName(podName);
      if (configMapNameTranslation.size() > 0) {
        rewriteConfigMapNames(pod, configMapNameTranslation);
      }
    }

    // We explicitly need to modify the deployments in the environment to provision unique names
    // for them.
    final Collection<Deployment> deployments = k8sEnv.getDeploymentsCopy().values();
    for (Deployment deployment : deployments) {
      final ObjectMeta deploymentMeta = deployment.getMetadata();
      final String originalName = deploymentMeta.getName();
      putLabel(deploymentMeta, Constants.CHE_ORIGINAL_NAME_LABEL, originalName);
      final String deploymentName = Names.uniqueResourceName(originalName, workspaceId);
      deploymentMeta.setName(deploymentName);
    }

    final Set<Ingress> ingresses = new HashSet<>(k8sEnv.getIngresses().values());
    k8sEnv.getIngresses().clear();
    for (Ingress ingress : ingresses) {
      final ObjectMeta ingressMeta = ingress.getMetadata();
      putLabel(ingress, Constants.CHE_ORIGINAL_NAME_LABEL, ingressMeta.getName());
      final String ingressName = Names.generateName("ingress");
      ingressMeta.setName(ingressName);
      k8sEnv.getIngresses().put(ingressName, ingress);
    }
  }

  private void rewriteConfigMapNames(PodData pod, Map<String, String> configMapNameTranslation) {
    // First update any env vars that reference configMaps.
    for (Container container : pod.getSpec().getContainers()) {
      // Can set env vars to key/value pairs in configmap
      if (container.getEnv() != null) {
        container
            .getEnv()
            .stream()
            .filter(
                env ->
                    env.getValueFrom() != null && env.getValueFrom().getConfigMapKeyRef() != null)
            .forEach(
                env -> {
                  ConfigMapKeySelector configMap = env.getValueFrom().getConfigMapKeyRef();
                  String originalName = configMap.getName();
                  // Since pods can reference configmaps that don't exist, we only change the name
                  // if the configmap does exist to aid debugging recipes (otherwise message is just
                  // null).
                  if (configMapNameTranslation.containsKey(originalName)) {
                    configMap.setName(configMapNameTranslation.get(originalName));
                  }
                });
      }
      if (container.getEnvFrom() != null) {
        // Can use all entries in configMap as env vars
        container
            .getEnvFrom()
            .stream()
            .filter(envFrom -> envFrom.getConfigMapRef() != null)
            .forEach(
                envFrom -> {
                  ConfigMapEnvSource configMapRef = envFrom.getConfigMapRef();
                  String originalName = configMapRef.getName();
                  if (configMapNameTranslation.containsKey(originalName)) {
                    configMapRef.setName(configMapNameTranslation.get(originalName));
                  }
                });
      }
    }
    // Next update any mounted configMaps
    List<Volume> volumes = pod.getSpec().getVolumes();
    if (pod.getSpec().getVolumes() != null) {
      volumes
          .stream()
          .filter(vol -> vol.getConfigMap() != null)
          .forEach(
              volume -> {
                ConfigMapVolumeSource configMapVolume = volume.getConfigMap();
                String originalName = configMapVolume.getName();
                if (configMapNameTranslation.containsKey(originalName)) {
                  configMapVolume.setName(configMapNameTranslation.get(originalName));
                }
              });
    }
  }
}
