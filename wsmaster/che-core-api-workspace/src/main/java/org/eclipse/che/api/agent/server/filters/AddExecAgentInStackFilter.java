/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.agent.server.filters;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

/**
 * Adds exec agent into each environment in stack where terminal agent is present.
 * It is needed for backward compatibility of application behavior after separation of these agents.
 *
 * @author Alexander Garagatyi
 */
@Filter
@Path("/stack")
public class AddExecAgentInStackFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {
        final String methodName = genericMethodResource.getMethod().getName();

        switch (methodName) {
            case "createStack": {
                StackDto stack = (StackDto)arguments[0];
                AddExecAgentInEnvironmentUtil.addExecAgent(stack.getWorkspaceConfig());
            }
        }
    }
}
