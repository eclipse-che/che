/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;

/**
 * Applies Workspace.Next configuration to an internal runtime object that represents workspace
 * runtime configuration on an infrastructure level.
 *
 * @author Oleksander Garagatyi
 */
@Beta
public interface WorkspaceNextApplier {

  /**
   * Applies Workspace.Next configuration to internal environment.
   *
   * @param internalEnvironment infrastructure specific representation of workspace runtime
   *     environment
   * @param chePlugins Workspace.Next configuration to apply to {@code internalEnvironment}
   * @throws InfrastructureException when applying Workspace.Next objects fails
   */
  void apply(InternalEnvironment internalEnvironment, Collection<ChePlugin> chePlugins)
      throws InfrastructureException;
}
