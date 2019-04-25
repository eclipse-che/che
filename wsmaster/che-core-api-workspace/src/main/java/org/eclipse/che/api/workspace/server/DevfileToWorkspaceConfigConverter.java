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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;

/**
 * Converts Devfile to WorkspaceConfig.
 *
 * @author Sergii Leshchenko
 */
public interface DevfileToWorkspaceConfigConverter {

  /**
   * Converts Devfile to workspace config.
   *
   * <p>Converted workspace config should be used for Workspace start only and should not be
   * persisted.
   *
   * @param devfile the devfile to convert
   * @return converted workspace config
   * @throws ServerException if the specified devfile can not be converted to workspace config for
   *     some reasons
   */
  WorkspaceConfig convert(Devfile devfile) throws ServerException;
}
