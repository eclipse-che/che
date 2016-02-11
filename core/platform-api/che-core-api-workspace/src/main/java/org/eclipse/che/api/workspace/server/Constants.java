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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.rest.permission.Operation;

/**
 * Constants for Workspace API
 *
 * @author Yevhenii Voevodin
 */
public final class Constants {

    public static final String    LINK_REL_GET_WORKSPACES       = "get workspaces";
    public static final String    LINK_REL_CREATE_WORKSPACE     = "create workspace";
    public static final String    LINK_REL_REMOVE_WORKSPACE     = "remove workspace";
    public static final int       ID_LENGTH                     = 16;
    public static final String    LINK_REL_START_WORKSPACE      = "start workspace";
    public static final String    LINK_REL_GET_RUNTIMEWORKSPACE = "get runtime workspace";
    public static final String    STOP_WORKSPACE                = "stop workspace";
    public static final String    GET_ALL_USER_WORKSPACES       = "get all user workspaces";
    public static final Operation START_WORKSPACE               = new Operation("start-workspace");

    private Constants() {
    }
}