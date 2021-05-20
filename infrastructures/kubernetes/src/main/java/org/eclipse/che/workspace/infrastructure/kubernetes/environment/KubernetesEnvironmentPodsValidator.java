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

import static org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentValidator.checkArgument;

import com.google.common.base.Joiner;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Validates {@link KubernetesEnvironment#getPodsData()}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironmentPodsValidator {

  public void validate(KubernetesEnvironment env) throws ValidationException {
    checkArgument(
        !env.getPodsData().isEmpty(), "Environment should contain at least 1 pod or deployment");

    ensureHasMetaAndSpec(env.getPodsData());

    ensureConfiguredMachinesHaveContainers(env);

    validatePodVolumes(env);
  }

  private void ensureHasMetaAndSpec(Map<String, PodData> podsData) throws ValidationException {
    for (PodData podData : podsData.values()) {
      if (podData.getMetadata() == null) {
        throw new ValidationException("Environment contains pod with missing metadata");
      }

      if (podData.getSpec() == null) {
        throw new ValidationException(
            String.format("Pod '%s' with missing metadata", podData.getMetadata().getName()));
      }
    }
  }

  private void validatePodVolumes(KubernetesEnvironment env) throws ValidationException {
    Set<String> pvcsNames = env.getPersistentVolumeClaims().keySet();
    for (PodData pod : env.getPodsData().values()) {
      Set<String> volumesNames = new HashSet<>();
      for (Volume volume : pod.getSpec().getVolumes()) {
        volumesNames.add(volume.getName());

        PersistentVolumeClaimVolumeSource pvcSource = volume.getPersistentVolumeClaim();
        if (pvcSource != null && !pvcsNames.contains(pvcSource.getClaimName())) {
          throw new ValidationException(
              String.format(
                  "Pod '%s' contains volume '%s' with PVC sources that references missing PVC '%s'",
                  pod.getMetadata().getName(), volume.getName(), pvcSource.getClaimName()));
        }
      }

      List<Container> containers = new ArrayList<>();
      containers.addAll(pod.getSpec().getContainers());
      containers.addAll(pod.getSpec().getInitContainers());
      for (Container container : containers) {
        for (VolumeMount volumeMount : container.getVolumeMounts()) {
          if (!volumesNames.contains(volumeMount.getName())) {
            throw new ValidationException(
                String.format(
                    "Container '%s' in pod '%s' contains volume mount that references missing volume '%s'",
                    container.getName(), pod.getMetadata().getName(), volumeMount.getName()));
          }
        }
      }
    }
  }

  private void ensureConfiguredMachinesHaveContainers(KubernetesEnvironment env)
      throws ValidationException {
    Set<String> missingMachines = new HashSet<>(env.getMachines().keySet());
    for (PodData pod : env.getPodsData().values()) {
      if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
        for (Container container : pod.getSpec().getContainers()) {
          missingMachines.remove(Names.machineName(pod, container));
        }
        for (Container container : pod.getSpec().getInitContainers()) {
          missingMachines.remove(Names.machineName(pod, container));
        }
      }
    }
    checkArgument(
        missingMachines.isEmpty(),
        "Environment contains machines that are missing in recipe: %s",
        Joiner.on(", ").join(missingMachines));
  }
}
