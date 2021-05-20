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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static org.eclipse.che.api.workspace.shared.Constants.ASYNC_PERSIST_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.EphemeralWorkspaceUtility.isEphemeral;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.WorkspaceAttributeValidator;
import org.eclipse.che.commons.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the workspace attributes before workspace creation and updating if async storage
 * configure.
 *
 * <p>To be valid for async storage workspace MUST have attributes:
 *
 * <ul>
 *   <li>{@link org.eclipse.che.api.workspace.shared.Constants#ASYNC_PERSIST_ATTRIBUTE} = 'true'
 *   <li>{@link org.eclipse.che.api.workspace.shared.Constants#PERSIST_VOLUMES_ATTRIBUTE} = 'false'
 * </ul>
 *
 * <p>If set only {@link org.eclipse.che.api.workspace.shared.Constants#ASYNC_PERSIST_ATTRIBUTE} =
 * 'true', {@link ValidationException} is thrown.
 *
 * <p>If system is configured with other value of properties than below {@link ValidationException},
 * is thrown.
 *
 * <ul>
 *   <li>che.infra.kubernetes.namespace.default=<username>-che
 *   <li>che.infra.kubernetes.pvc.strategy=common
 *   <li>che.limits.user.workspaces.run.count=1
 * </ul>
 */
public class AsyncStorageModeValidator implements WorkspaceAttributeValidator {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncStorageModeValidator.class);

  private final String pvcStrategy;
  private final int runtimesPerUser;
  private final boolean isNamespaceStrategyNotValid;
  private final boolean isPvcStrategyNotValid;
  private final boolean singleRuntimeAllowed;

  @Inject
  public AsyncStorageModeValidator(
      @Named("che.infra.kubernetes.pvc.strategy") String pvcStrategy,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.limits.user.workspaces.run.count") int runtimesPerUser) {

    this.pvcStrategy = pvcStrategy;
    this.runtimesPerUser = runtimesPerUser;

    this.isPvcStrategyNotValid = !COMMON_STRATEGY.equals(pvcStrategy);
    this.singleRuntimeAllowed = runtimesPerUser == 1;
    this.isNamespaceStrategyNotValid =
        isNullOrEmpty(defaultNamespaceName) || !defaultNamespaceName.contains("<username>");
  }

  @Override
  public void validate(Map<String, String> attributes) throws ValidationException {
    if (parseBoolean(attributes.get(ASYNC_PERSIST_ATTRIBUTE))) {
      isEphemeralAttributeValidation(attributes);
      pvcStrategyValidation();
      nameSpaceStrategyValidation();
      runtimesPerUserValidation();
    }
  }

  @Override
  public void validateUpdate(Map<String, String> existing, Map<String, String> update)
      throws ValidationException {
    if (parseBoolean(update.get(ASYNC_PERSIST_ATTRIBUTE))) {
      if (isEphemeral(existing) || isEphemeral(update)) {
        pvcStrategyValidation();
        nameSpaceStrategyValidation();
        runtimesPerUserValidation();
      } else {
        String message =
            "Workspace configuration not valid: Asynchronous storage available only for NOT persistent storage";
        LOG.warn(message);
        throw new ValidationException(message);
      }
    }
  }

  private void isEphemeralAttributeValidation(Map<String, String> attributes)
      throws ValidationException {
    if (!isEphemeral(attributes)) {
      String message =
          "Workspace configuration not valid: Asynchronous storage available only for NOT persistent storage";
      LOG.warn(message);
      throw new ValidationException(message);
    }
  }

  private void runtimesPerUserValidation() throws ValidationException {
    if (!singleRuntimeAllowed) {
      String message =
          format(
              "Workspace configuration not valid: Asynchronous storage available only if 'che.limits.user.workspaces.run.count' set to 1, but got %s",
              runtimesPerUser);
      LOG.warn(message);
      throw new ValidationException(message);
    }
  }

  private void nameSpaceStrategyValidation() throws ValidationException {
    if (isNamespaceStrategyNotValid) {
      String message =
          "Workspace configuration not valid: Asynchronous storage available only for 'per-user' namespace strategy";
      LOG.warn(message);
      throw new ValidationException(message);
    }
  }

  private void pvcStrategyValidation() throws ValidationException {
    if (isPvcStrategyNotValid) {
      String message =
          format(
              "Workspace configuration not valid: Asynchronous storage available only for 'common' PVC strategy, but got %s",
              pvcStrategy);
      LOG.warn(message);
      throw new ValidationException(message);
    }
  }
}
