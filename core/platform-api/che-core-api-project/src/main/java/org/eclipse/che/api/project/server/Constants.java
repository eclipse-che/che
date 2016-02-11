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
package org.eclipse.che.api.project.server;

/** @author andrew00x */
public class Constants {
    // rels for known project links
    public static final String LINK_REL_GET_PROJECTS   = "get projects";
    public static final String LINK_REL_CREATE_PROJECT = "create project";
    public static final String LINK_REL_UPDATE_PROJECT = "update project";
    public static final String LINK_REL_EXPORT_ZIP     = "zipball sources";
    public static final String LINK_REL_CHILDREN       = "children";
    public static final String LINK_REL_TREE           = "tree";
    public static final String LINK_REL_MODULES        = "modules";
    public static final String LINK_REL_DELETE         = "delete";
    public static final String LINK_REL_GET_CONTENT    = "get content";
    public static final String LINK_REL_UPDATE_CONTENT = "update content";

    public static final String LINK_REL_PROJECT_TYPES = "project types";

    public static final String CODENVY_DIR                        = ".codenvy";
    public static final String CODENVY_MISC_FILE                  = "misc.xml";

    public static final String CODENVY_MISC_FILE_RELATIVE_PATH    = CODENVY_DIR + "/" + CODENVY_MISC_FILE;

    private Constants() {
    }
}
