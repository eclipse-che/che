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
package org.eclipse.che.api.devfile.server.convert.tool;

import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/**
 * Applies changes on workspace config according to the specified tool. Different implementations
 * are specialized on the concrete tool type.
 *
 * @author Sergii Leshchenko
 */
public interface ToolToWorkspaceApplier {

  /**
   * Applies changes on workspace config according to the specified tool.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param tool tool that should be applied
   * @param contentProvider optional content provider that may be used for external tool resource
   *     fetching
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   * @throws DevfileException if content provider is null while the specified tool requires external
   *     file content
   * @throws DevfileException if any exception occurs during content retrieving
   */
  void apply(WorkspaceConfigImpl workspaceConfig, Tool tool, FileContentProvider contentProvider)
      throws DevfileException;
}
