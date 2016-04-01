/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local.filters;

import com.google.inject.Singleton;

import javax.servlet.ServletRequest;

/**
 * The filter contains business logic which allows extract workspace id and sets it to environment context. Workspace id
 * has defined place in URL. The filter mapped for all requests to ws agent API.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class WsAgentEnvironmentInitializationFilter extends AbstractEnvironmentInitializationFilter {

    @Override
    protected String getWorkspaceId(ServletRequest request) {
        return System.getenv("CHE_WORKSPACE_ID");
    }
}
