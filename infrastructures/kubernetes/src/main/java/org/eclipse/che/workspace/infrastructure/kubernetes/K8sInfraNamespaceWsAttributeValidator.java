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
import static java.lang.String.format;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.WorkspaceAttributeValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.NamespaceNameValidator;

/**
 * Validates the values of {@link
 * org.eclipse.che.api.workspace.shared.Constants#WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE}
 * before workspace creation and updating.
 *
 * @author Sergii Leshchenko
 */
public class K8sInfraNamespaceWsAttributeValidator implements WorkspaceAttributeValidator {

  private final Provider<KubernetesNamespaceFactory> namespaceFactoryProvider;

  @Inject
  public K8sInfraNamespaceWsAttributeValidator(
      Provider<KubernetesNamespaceFactory> namespaceFactoryProvider) {
    this.namespaceFactoryProvider = namespaceFactoryProvider;
  }

  /**
   * Validates value of {@link
   * org.eclipse.che.api.workspace.shared.Constants#WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE}.
   *
   * @param attributes workspace attributes to validate
   * @throws ValidationException when infra namespace value has wrong format
   * @throws ValidationException when the current user is not permitted to use the specified infra
   *     namespace
   */
  @Override
  public void validate(Map<String, String> attributes) throws ValidationException {
    String namespace = attributes.get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    if (!isNullOrEmpty(namespace)) {
      NamespaceNameValidator.validate(namespace);
      namespaceFactoryProvider.get().checkIfNamespaceIsAllowed(namespace);
    }
  }

  /**
   * Validates value of {@link
   * org.eclipse.che.api.workspace.shared.Constants#WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE}
   * before updating.
   *
   * @param existing actual workspace attributes
   * @param update new workspace attributes to validate
   * @throws ValidationException when new attributes removes or updates infra namespace value
   */
  @Override
  public void validateUpdate(Map<String, String> existing, Map<String, String> update)
      throws ValidationException {
    String existingNamespace = existing.get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    String updateNamespace = update.get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);

    if (isNullOrEmpty(updateNamespace)) {
      if (isNullOrEmpty(existingNamespace)) {
        // this workspace was created before we start storing namespace info
        // namespace info will be stored during the next workspace start
        return;
      }

      throw new ValidationException(
          format(
              "The namespace information must not be updated or "
                  + "deleted. You must provide \"%s\" attribute with \"%s\" as a value",
              WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, existingNamespace));
    }

    if (isNullOrEmpty(existingNamespace)) {
      // this would mean that the user made an update to the workspace without it having the
      // namespace attribute stored. This is very, very unlikely, because the setting of attributes
      // happens during the creation process. But let's just cover this case anyway, just to be
      // sure.
      validate(update);

      // everything is fine. We allow to change infra namespace in such case.
      return;
    }

    if (!updateNamespace.equals(existingNamespace)) {
      throw new ValidationException(
          format(
              "The namespace from the provided object \"%s\" does "
                  + "not match the actual namespace \"%s\"",
              updateNamespace, existingNamespace));
    }
  }
}
