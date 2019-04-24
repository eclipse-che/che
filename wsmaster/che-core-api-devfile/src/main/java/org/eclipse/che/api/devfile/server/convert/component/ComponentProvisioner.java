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
package org.eclipse.che.api.devfile.server.convert.component;

import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;

/**
 * Provision component in {@link DevfileImpl} according to the specified {@link
 * WorkspaceConfigImpl}. Different implementations process different part of {@link
 * WorkspaceConfigImpl} and provision different components types.
 *
 * @author Sergii Leshchenko
 */
public interface ComponentProvisioner {

  /**
   * Applies needed changes on {@link DevfileImpl} according to the specified {@link
   * WorkspaceConfigImpl}.
   *
   * @param devfile devfile that should be updated with the specified workspace config
   * @param workspaceConfig workspace config that should be applied
   * @throws IllegalArgumentException if the specified workspace config or devfile is null
   * @throws WorkspaceExportException if the specified workspace config can not be applied on
   *     devfile
   */
  void provision(DevfileImpl devfile, WorkspaceConfigImpl workspaceConfig)
      throws WorkspaceExportException;
}
