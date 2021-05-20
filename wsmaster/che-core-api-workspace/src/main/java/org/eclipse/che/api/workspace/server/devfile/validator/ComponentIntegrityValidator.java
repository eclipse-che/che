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
package org.eclipse.che.api.workspace.server.devfile.validator;

import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;

/**
 * There are several types of components that are handled in an infrastructure-specific way. This
 * interface provides the devfile validator with component-specific validators.
 */
public interface ComponentIntegrityValidator {

  /**
   * Validates the component. The component is guaranteed to be of the type that can be handled by
   * this validator.
   *
   * @param component the devfile component to validate
   * @param contentProvider content provider that can be used to resolve references
   */
  void validateComponent(Component component, FileContentProvider contentProvider)
      throws DevfileFormatException;

  final class NoopComponentIntegrityValidator implements ComponentIntegrityValidator {

    @Override
    public void validateComponent(Component component, FileContentProvider contentProvider) {}
  }
}
