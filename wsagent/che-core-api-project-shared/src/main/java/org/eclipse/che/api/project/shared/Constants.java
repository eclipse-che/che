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
package org.eclipse.che.api.project.shared;

/**
 * @author Vitalii Parfonov
 */
public class Constants {

    public static final String BLANK_ID                       = "blank";
    public static final String ZIP_IMPORTER_ID                = "zip";
    public static final String VCS_PROVIDER_NAME              = "vcs.provider.name";
    // rels for known project links
    public static final String LINK_REL_GET_PROJECTS          = "get projects";
    public static final String LINK_REL_CREATE_PROJECT        = "create project";
    public static final String LINK_REL_CREATE_BATCH_PROJECTS = "create batch of projects";
    public static final String LINK_REL_UPDATE_PROJECT        = "update project";
    public static final String LINK_REL_EXPORT_ZIP            = "zipball sources";
    public static final String LINK_REL_CHILDREN              = "children";
    public static final String LINK_REL_TREE                  = "tree";
    public static final String LINK_REL_DELETE                = "delete";
    public static final String LINK_REL_GET_CONTENT           = "get content";
    public static final String LINK_REL_UPDATE_CONTENT        = "update content";

    public static final String LINK_REL_PROJECT_TYPES = "project types";

    public static final String CODENVY_DIR = ".codenvy";

    private Constants() {
    }
}
