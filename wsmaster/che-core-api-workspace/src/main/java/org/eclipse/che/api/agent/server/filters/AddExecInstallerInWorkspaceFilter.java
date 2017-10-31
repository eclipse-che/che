/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.agent.server.filters;

import javax.ws.rs.Path;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

/**
 * Adds exec installer into each environment in workspace config where terminal installer is
 * present. It is needed for backward compatibility of application behavior after separation of
 * these agents.
 *
 * @author Alexander Garagatyi
 */
@Filter
@Path("/workspace{path:(/.*)?}")
public class AddExecInstallerInWorkspaceFilter extends CheMethodInvokerFilter {
  @Override
  protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments)
      throws ApiException {
    final String methodName = genericMethodResource.getMethod().getName();

    switch (methodName) {
      case "create":
      case "startFromConfig":
        {
          WorkspaceConfigDto workspaceConfig = (WorkspaceConfigDto) arguments[0];
          AddExecInstallerInEnvironmentUtil.addExecInstaller(workspaceConfig);
        }
    }
  }
}
