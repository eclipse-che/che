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
import javax.servlet.http.HttpServletRequest;

/**
 * The filter contains business logic which extracts workspace id and sets it to environment context. Workspace id
 * has defined place in URL. The filter mapped for all requests to master API.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class WsMasterEnvironmentInitializationFilter extends AbstractEnvironmentInitializationFilter {

    /**
     * The index of workspace id inside request uri.
     */
    private static final int WORKSPACE_ID_INDEX = 4;

    @Override
    protected String getWorkspaceId(ServletRequest request) {
        String requestUri = ((HttpServletRequest)request).getRequestURI();
        String[] uriParts = requestUri.split("/");

        if (uriParts.length >= 5) {
            String workspaceId = uriParts[WORKSPACE_ID_INDEX];

            if (workspaceId.startsWith("workspace")) {
                return workspaceId;
            }
        }

        return "";
    }
}
