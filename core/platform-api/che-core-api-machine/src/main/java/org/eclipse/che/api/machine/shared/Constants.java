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
package org.eclipse.che.api.machine.shared;

/**
 * @author Eugene Voevodin
 */
public class Constants {

    public static final String LINK_REL_REMOVE_RECIPE          = "remove recipe";
    public static final String LINK_REL_GET_RECIPE_SCRIPT      = "get recipe script";
    public static final String LINK_REL_CREATE_RECIPE          = "create recipe";
    public static final String LINK_REL_GET_RECIPES_BY_CREATOR = "get created recipes";
    public static final String LINK_REL_SEARCH_RECIPES         = "search recipes";
    public static final String LINK_REL_UPDATE_RECIPE          = "update recipe";

    public static final String LINK_REL_GET_MACHINE                = "get machine";
    public static final String LINK_REL_GET_MACHINES               = "get machines";
    public static final String LINK_REL_DESTROY_MACHINE            = "destroy machine";
    public static final String LINK_REL_GET_SNAPSHOTS              = "get snapshots";
    public static final String LINK_REL_SAVE_SNAPSHOT              = "save snapshot";
    public static final String LINK_REL_REMOVE_SNAPSHOT            = "remove snapshot";
    public static final String LINK_REL_EXECUTE_COMMAND            = "execute command";
    public static final String LINK_REL_GET_PROCESSES              = "get processes";
    public static final String LINK_REL_STOP_PROCESS               = "stop process";
    public static final String LINK_REL_GET_MACHINE_LOGS           = "get machine logs";
    public static final String LINK_REL_GET_PROCESS_LOGS           = "get process logs";
    public static final String LINK_REL_GET_MACHINE_LOGS_CHANNEL   = "get machine logs channel";
    public static final String LINK_REL_GET_MACHINE_STATUS_CHANNEL = "get machine status channel";

    public static final String WS_AGENT_SERVER_REFERENCE = "extensions";

    private Constants() {
    }
}
