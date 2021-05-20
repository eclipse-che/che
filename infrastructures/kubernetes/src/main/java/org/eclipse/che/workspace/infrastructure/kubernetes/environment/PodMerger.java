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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putAnnotations;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabels;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Helps to merge multiple pods to a single deployment.
 *
 * @author Sergii Leshchenko
 */
public class PodMerger {
  /** Key of pod template metadata that contains deployment name. */
  public static final String DEPLOYMENT_NAME_LABEL = "deployment";

  private static final String DEFAULT_DEPLOYMENT_NAME = "workspace";

  /**
   * Creates a new deployments that contains all pods from the specified pods data lists.
   *
   * <p>If multiple pods have labels, annotations, additionalProperties with the same key then
   * override will happen and result pod template will have only the last value. You may need to
   * reconfigure objects that rely on these value, like selector field in Service. You can use all
   * of pod template labels for reconfiguring or single {@link #DEFAULT_DEPLOYMENT_NAME} label that
   * contains deployment name;
   *
   * @param podsData the pods data to be merged
   * @return a new Deployment that have pod template that is result of merging the specified lists
   * @throws ValidationException if pods can not be merged because of critical names collisions like
   *     volumes names
   */
  public Deployment merge(List<PodData> podsData) throws ValidationException {
    Deployment baseDeployment = createEmptyDeployment(DEFAULT_DEPLOYMENT_NAME);
    PodTemplateSpec basePodTemplate = baseDeployment.getSpec().getTemplate();
    ObjectMeta basePodMeta = basePodTemplate.getMetadata();
    PodSpec baseSpec = basePodTemplate.getSpec();

    Set<String> containerNames = new HashSet<>();
    Set<String> initContainerNames = new HashSet<>();
    Set<String> volumes = new HashSet<>();
    Set<String> pullSecrets = new HashSet<>();
    for (PodData podData : podsData) {
      // if there are entries with such keys then values will be overridden
      ObjectMeta podMeta = podData.getMetadata();
      putLabels(basePodMeta, podMeta.getLabels());
      putAnnotations(basePodMeta, podMeta.getAnnotations());

      basePodMeta.getAdditionalProperties().putAll(podMeta.getAdditionalProperties());

      for (Container container : podData.getSpec().getContainers()) {
        String containerName = container.getName();

        // generate container name to avoid collisions
        while (!containerNames.add(container.getName())) {
          containerName = NameGenerator.generate(container.getName(), 4);
          container.setName(containerName);
        }

        // store original recipe machine name
        Names.putMachineName(basePodMeta, containerName, Names.machineName(podMeta, container));

        baseSpec.getContainers().add(container);
      }

      for (Container initContainer : podData.getSpec().getInitContainers()) {
        // generate container name to avoid collisions
        while (!initContainerNames.add(initContainer.getName())) {
          initContainer.setName(NameGenerator.generate(initContainer.getName(), 4));
        }
        // store original recipe machine name
        Names.putMachineName(
            basePodMeta, initContainer.getName(), Names.machineName(podMeta, initContainer));
        baseSpec.getInitContainers().add(initContainer);
      }

      for (Volume volume : podData.getSpec().getVolumes()) {
        if (!volumes.add(volume.getName())) {
          if (volume.getName().equals(PROJECTS_VOLUME_NAME)) {
            // project volume already added, can be skipped
            continue;
          }
          throw new ValidationException(
              format(
                  "Pods have to have volumes with unique names but there are multiple `%s` volumes",
                  volume.getName()));
        }
        baseSpec.getVolumes().add(volume);
      }

      for (LocalObjectReference pullSecret : podData.getSpec().getImagePullSecrets()) {
        if (pullSecrets.add(pullSecret.getName())) {
          // add pull secret only if it is not present yet
          baseSpec.getImagePullSecrets().add(pullSecret);
        }
      }
      if (podData.getSpec().getTerminationGracePeriodSeconds() != null) {
        if (baseSpec.getTerminationGracePeriodSeconds() != null) {
          baseSpec.setTerminationGracePeriodSeconds(
              Long.max(
                  baseSpec.getTerminationGracePeriodSeconds(),
                  podData.getSpec().getTerminationGracePeriodSeconds()));
        } else {
          baseSpec.setTerminationGracePeriodSeconds(
              podData.getSpec().getTerminationGracePeriodSeconds());
        }
      }

      baseSpec.setSecurityContext(
          mergeSecurityContexts(
              baseSpec.getSecurityContext(), podData.getSpec().getSecurityContext()));

      baseSpec.setServiceAccount(
          mergeServiceAccount(baseSpec.getServiceAccount(), podData.getSpec().getServiceAccount()));

      baseSpec.setServiceAccountName(
          mergeServiceAccountName(
              baseSpec.getServiceAccountName(), podData.getSpec().getServiceAccountName()));

      baseSpec.setNodeSelector(
          mergeNodeSelector(baseSpec.getNodeSelector(), podData.getSpec().getNodeSelector()));

      // if there are entries with such keys then values will be overridden
      baseSpec.getAdditionalProperties().putAll(podData.getSpec().getAdditionalProperties());
      // add tolerations to baseSpec if any
      baseSpec.getTolerations().addAll(podData.getSpec().getTolerations());
    }

    Map<String, String> matchLabels = new HashMap<>();
    matchLabels.put(DEPLOYMENT_NAME_LABEL, baseDeployment.getMetadata().getName());
    putLabels(basePodMeta, matchLabels);
    baseDeployment.getSpec().getSelector().setMatchLabels(matchLabels);

    return baseDeployment;
  }

  private Deployment createEmptyDeployment(String name) {
    return new DeploymentBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(name).build())
        .withNewSpec()
        .withNewSelector()
        .endSelector()
        .withReplicas(1)
        .withNewTemplate()
        .withNewMetadata()
        .endMetadata()
        .withSpec(new PodSpec())
        .endTemplate()
        .endSpec()
        .build();
  }

  private PodSecurityContext mergeSecurityContexts(
      @Nullable PodSecurityContext a, @Nullable PodSecurityContext b) throws ValidationException {
    return nonNullOrEqual(a, b, "Cannot merge pods with different security contexts: %s, %s");
  }

  private String mergeServiceAccount(@Nullable String a, @Nullable String b)
      throws ValidationException {
    return nonNullOrEqual(a, b, "Cannot merge pods with different service accounts: %s, %s");
  }

  private String mergeServiceAccountName(@Nullable String a, @Nullable String b)
      throws ValidationException {
    return nonNullOrEqual(a, b, "Cannot merge pods with different service account names: %s, %s");
  }

  @Nullable
  private static <T> T nonNullOrEqual(@Nullable T a, @Nullable T b, String errorMessageTemplate)
      throws ValidationException {
    if (a == null) {
      return b;
    } else if (b == null || a.equals(b)) {
      return a;
    } else {
      throw new ValidationException(String.format(errorMessageTemplate, a, b));
    }
  }

  private Map<String, String> mergeNodeSelector(
      @Nullable Map<String, String> base, @Nullable Map<String, String> nodeSelector) {
    Map<String, String> mergedMap = base;
    if (nodeSelector != null && !nodeSelector.isEmpty()) {
      if (mergedMap == null) {
        mergedMap = new HashMap<>();
      }
      mergedMap.putAll(nodeSelector);
    }
    return mergedMap;
  }
}
