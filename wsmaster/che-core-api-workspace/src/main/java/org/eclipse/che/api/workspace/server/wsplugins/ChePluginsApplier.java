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
package org.eclipse.che.api.workspace.server.wsplugins;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;

/**
 * Applies Che plugins tooling configuration to an internal runtime object that represents workspace
 * runtime configuration on an infrastructure level.
 *
 * @author Oleksander Garagatyi
 */
@Beta
public interface ChePluginsApplier {

  /**
   * Applies Che plugins tooling configuration to internal environment.
   *
   * @param runtimeIdentity the runtime identity of the workspace that the plugins are being applied
   *     to
   * @param internalEnvironment infrastructure specific representation of workspace runtime
   *     environment
   * @param chePlugins Che plugins tooling configuration to apply to {@code internalEnvironment}
   * @throws InfrastructureException when applying Che plugins tooling fails
   */
  void apply(
      RuntimeIdentity runtimeIdentity,
      InternalEnvironment internalEnvironment,
      Collection<ChePlugin> chePlugins)
      throws InfrastructureException;
}
