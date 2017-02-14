/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.shared;

/**
 * @author Eugene Voevodin
 */
public class Constants {

    public static final String LINK_REL_REMOVE_RECIPE              = "remove recipe";
    public static final String LINK_REL_GET_RECIPE_SCRIPT          = "get recipe script";
    public static final String LINK_REL_CREATE_RECIPE              = "create recipe";
    public static final String LINK_REL_SEARCH_RECIPES             = "search recipes";
    public static final String LINK_REL_UPDATE_RECIPE              = "update recipe";

    public static final String LINK_REL_SELF                       = "self link";
    public static final String LINK_REL_GET_MACHINES               = "get machines";
    public static final String LINK_REL_DESTROY_MACHINE            = "destroy machine";
    public static final String LINK_REL_EXECUTE_COMMAND            = "execute command";
    public static final String LINK_REL_GET_PROCESSES              = "get processes";
    public static final String LINK_REL_STOP_PROCESS               = "stop process";
    public static final String LINK_REL_GET_MACHINE_LOGS           = "get machine logs";
    public static final String LINK_REL_GET_PROCESS_LOGS           = "get process logs";

    public static final String WSAGENT_REFERENCE                   = "wsagent";
    public static final String WSAGENT_WEBSOCKET_REFERENCE         = "wsagent.websocket";
    public static final String WSAGENT_DEBUG_REFERENCE             = "wsagent.debug";

    public static final String LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL = "environment.output_channel";
    public static final String ENVIRONMENT_OUTPUT_CHANNEL_TEMPLATE = "workspace:%s:environment_output";
    public static final String LINK_REL_ENVIRONMENT_STATUS_CHANNEL = "environment.status_channel";
    public static final String ENVIRONMENT_STATUS_CHANNEL_TEMPLATE = "workspace:%s:machines_statuses";

    public static final String TERMINAL_REFERENCE = "terminal";
    public static final String EXEC_AGENT_REFERENCE = "exec-agent";

    public static final String WS_AGENT_PORT = "4401/tcp";

    public static final String WS_MACHINE_NAME = "default";

    private Constants() {
    }
}
