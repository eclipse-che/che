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

import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;

/**
 * Validates {@link KubernetesEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironmentValidator {
  private final KubernetesEnvironmentPodsValidator podsValidator;

  @Inject
  public KubernetesEnvironmentValidator(KubernetesEnvironmentPodsValidator podsValidator) {
    this.podsValidator = podsValidator;
  }

  /**
   * Validates {@link KubernetesEnvironment}.
   *
   * @param env environment to perform validation
   * @throws ValidationException if the specified {@link KubernetesEnvironment} is invalid
   */
  public void validate(KubernetesEnvironment env) throws ValidationException {
    podsValidator.validate(env);

    // TODO Implement validation for other Kubernetes objects
    // https://github.com/eclipse/che/issues/7381
  }

  static void checkArgument(boolean expression, String error) throws ValidationException {
    if (!expression) {
      throw new ValidationException(error);
    }
  }

  static void checkArgument(
      boolean expression, String errorMessageTemplate, Object... errorMessageParams)
      throws ValidationException {
    if (!expression) {
      throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
    }
  }
}
