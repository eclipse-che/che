/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.shared;

/** @author Vitalii Parfonov */
public class Constants {

  public static final String BLANK_ID = "blank";
  public static final String ZIP_IMPORTER_ID = "zip";
  public static final String VCS_PROVIDER_NAME = "vcs.provider.name";
  // rels for known project links
  public static final String LINK_REL_GET_PROJECTS = "get projects";
  public static final String LINK_REL_CREATE_PROJECT = "create project";
  public static final String LINK_REL_CREATE_BATCH_PROJECTS = "create batch of projects";
  public static final String LINK_REL_UPDATE_PROJECT = "update project";
  public static final String LINK_REL_EXPORT_ZIP = "zipball sources";
  public static final String LINK_REL_CHILDREN = "children";
  public static final String LINK_REL_TREE = "tree";
  public static final String LINK_REL_DELETE = "delete";
  public static final String LINK_REL_GET_CONTENT = "get content";
  public static final String LINK_REL_UPDATE_CONTENT = "update content";

  public static final String LINK_REL_PROJECT_TYPES = "project types";

  public static final String CHE_DIR = ".che";

  public static final String COMMANDS_ATTRIBUTE_NAME = "commands";
  public static final String COMMANDS_ATTRIBUTE_DESCRIPTION = "Project-related commands";

  public static final String EVENT_IMPORT_OUTPUT_PROGRESS = "importProject/progress";

  public static final String WS_PATH_STRICT = "WS_PATH_STRICT";

  private Constants() {}

  public static class Services {

    public static final String PROJECT_GET = "project/get";
    public static final String PROJECT_CREATE = "project/create";
    public static final String PROJECTS_BATCH = "projects/batch";
    public static final String PROJECT_UPDATE = "project/update";
    public static final String PROJECT_DELETE = "project/delete";
    public static final String PROJECT_RECOGNIZE = "project/recognize";
    public static final String PROJECT_VERIFY = "project/verify";
    public static final String PROJECT_IMPORT = "project/import";

    public static final int NOT_FOUND = -27100;
    public static final int BAD_REQUEST = -27101;
    public static final int CONFLICT = -27102;
    public static final int FORBIDDEN = -27103;
    public static final int SERVER_ERROR = -27104;
    public static final int UNAUTHORIZED = -27105;

    private Services() {}
  }
}
