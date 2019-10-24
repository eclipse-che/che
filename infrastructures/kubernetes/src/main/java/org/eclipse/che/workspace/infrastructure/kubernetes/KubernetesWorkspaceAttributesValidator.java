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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;

import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.WorkspaceAttributeValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/** @author Sergii Leshchenko */
public class KubernetesWorkspaceAttributesValidator implements WorkspaceAttributeValidator {

  private final int METADATA_NAME_MAX_LENGTH = 63;
  private final Pattern METADATA_NAME_PATTERN = Pattern.compile("[a-z0-9]([-a-z0-9]*[a-z0-9])?");

  private final Provider<KubernetesNamespaceFactory> namespaceFactoryProvider;

  @Inject
  public KubernetesWorkspaceAttributesValidator(
      Provider<KubernetesNamespaceFactory> namespaceFactoryProvider) {
    this.namespaceFactoryProvider = namespaceFactoryProvider;
  }

  @Override
  public void validate(Map<String, String> attributes) throws ValidationException {
    String namespace = attributes.get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    if (!isNullOrEmpty(namespace)) {
      if (namespace.length() > METADATA_NAME_MAX_LENGTH) {
        throw new ValidationException(
            "The specified namespace "
                + namespace
                + " is invalid: must be no more than 63 characters");
      }

      if (!METADATA_NAME_PATTERN.matcher(namespace).matches()) {
        throw new ValidationException(
            "The specified namespace "
                + namespace
                + " is invalid: a DNS-1123 label must consist of lower case alphanumeric"
                + " characters or '-', and must start and end with an"
                + " alphanumeric character (e.g. 'my-name', or '123-abc', regex used for"
                + " validation is '[a-z0-9]([-a-z0-9]*[a-z0-9])?')");
      }

      namespaceFactoryProvider.get().checkIfNamespaceIsAllowed(namespace);
    }
  }
}
