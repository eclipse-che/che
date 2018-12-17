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

import static java.lang.String.format;

import com.google.common.base.Joiner;
import io.fabric8.kubernetes.api.model.Container;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Validates {@link KubernetesEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironmentValidator {

  /**
   * Validates {@link KubernetesEnvironment}.
   *
   * @param env environment to perform validation
   * @throws ValidationException if the specified {@link KubernetesEnvironment} is invalid
   */
  public void validate(KubernetesEnvironment env) throws ValidationException {
    checkArgument(!env.getPodsData().isEmpty(), "Environment should contain at least 1 pod");

    Set<String> missingMachines = new HashSet<>(env.getMachines().keySet());
    for (PodData pod : env.getPodsData().values()) {
      if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
        for (Container container : pod.getSpec().getContainers()) {
          missingMachines.remove(Names.machineName(pod, container));
        }
      }
    }
    checkArgument(
        missingMachines.isEmpty(),
        "Environment contains machines that are missing in recipe: %s",
        Joiner.on(", ").join(missingMachines));
    // TODO Implement validation Kubernetes objects https://github.com/eclipse/che/issues/7381
  }

  private static void checkArgument(boolean expression, String error) throws ValidationException {
    if (!expression) {
      throw new ValidationException(error);
    }
  }

  private static void checkArgument(
      boolean expression, String errorMessageTemplate, Object... errorMessageParams)
      throws ValidationException {
    if (!expression) {
      throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
    }
  }
}
