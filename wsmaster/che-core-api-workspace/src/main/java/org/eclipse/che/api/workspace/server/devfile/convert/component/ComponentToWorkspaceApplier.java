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
package org.eclipse.che.api.workspace.server.devfile.convert.component;

import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;

/**
 * Applies changes on workspace config according to the specified component. Different
 * implementations are specialized on the concrete component type.
 *
 * @author Sergii Leshchenko
 */
public interface ComponentToWorkspaceApplier {

  /**
   * Applies changes on workspace config according to the specified component.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param component component that should be applied
   * @param contentProvider optional content provider that may be used for external component
   *     resource fetching
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   * @throws DevfileException if content provider is null while the specified component requires
   *     external file content
   * @throws DevfileException if any exception occurs during content retrieving
   */
  void apply(
      WorkspaceConfigImpl workspaceConfig,
      ComponentImpl component,
      FileContentProvider contentProvider)
      throws DevfileException;
}
