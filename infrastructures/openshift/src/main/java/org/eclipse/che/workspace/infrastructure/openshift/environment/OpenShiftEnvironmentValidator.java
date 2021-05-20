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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.openshift.api.model.Route;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentPodsValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentValidator;

/**
 * Adds additional OpenShift-specific validation to {@link KubernetesEnvironmentValidator}
 *
 * @author Angel Misevski
 */
public class OpenShiftEnvironmentValidator extends KubernetesEnvironmentValidator {

  private static final String SERVICE_KIND = "Service";

  @Inject
  public OpenShiftEnvironmentValidator(KubernetesEnvironmentPodsValidator podsValidator) {
    super(podsValidator);
  }

  public void validate(OpenShiftEnvironment env) throws ValidationException {
    super.validate(env);
    validateRoutesMatchServices(env);
  }

  private void validateRoutesMatchServices(OpenShiftEnvironment env) throws ValidationException {
    Set<String> recipeServices =
        env.getServices()
            .values()
            .stream()
            .map(s -> s.getMetadata().getName())
            .collect(Collectors.toSet());
    for (Route route : env.getRoutes().values()) {
      if (route.getSpec() == null
          || route.getSpec().getTo() == null
          || !route.getSpec().getTo().getKind().equals(SERVICE_KIND)) {
        continue;
      }
      String serviceName = route.getSpec().getTo().getName();
      if (!recipeServices.contains(serviceName)) {
        throw new ValidationException(
            String.format(
                "Route '%s' refers to Service '%s'. Routes must refer to Services included in recipe",
                route.getMetadata().getName(), serviceName));
      }
    }
  }
}
