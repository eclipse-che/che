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
package org.eclipse.che.api.workspace.shared;

import org.eclipse.che.api.core.rest.permission.Operation;

/**
 * Constants for Workspace API
 *
 * @author Yevhenii Voevodin
 */
public final class Constants {

    public static final String LINK_REL_GET_WORKSPACES        = "get workspaces";
    public static final String LINK_REL_CREATE_WORKSPACE      = "create workspace";
    public static final String LINK_REL_REMOVE_WORKSPACE      = "remove workspace";
    public static final String LINK_REL_START_WORKSPACE       = "start workspace";
    public static final String LINK_REL_GET_RUNTIME_WORKSPACE = "get runtime workspace";
    public static final String LINK_REL_STOP_WORKSPACE        = "stop workspace";
    public static final String GET_ALL_USER_WORKSPACES        = "get all user workspaces";
    public static final String LINK_REL_GET_SNAPSHOT          = "get workspace snapshot";
    public static final String LINK_REL_SELF                  = "self link";
    public static final String LINK_REL_IDE_URL               = "ide url";
    public static final String LIN_REL_GET_WORKSPACE          = "get workspace";


    public static final Operation START_WORKSPACE = new Operation("start-workspace");

    public static final String LINK_REL_CREATE_STACK          = "create stack";
    public static final String LINK_REL_UPDATE_STACK          = "update stack";
    public static final String LINK_REL_REMOVE_STACK          = "remove stack";
    public static final String LINK_REL_GET_STACK_BY_ID       = "get stack by id";
    public static final String LINK_REL_GET_STACKS_BY_CREATOR = "get stacks by creator";
    public static final String LINK_REL_SEARCH_STACKS         = "search stacks";

    public static final String LINK_REL_GET_ICON    = "get icon link";
    public static final String LINK_REL_UPLOAD_ICON = "upload icon link";
    public static final String LINK_REL_DELETE_ICON = "delete icon link";

    public static final String LINK_REL_GET_WORKSPACE_EVENTS_CHANNEL = "get workspace events channel";

    private Constants() {
    }
}
