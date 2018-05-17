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
package org.eclipse.che.api.workspace.shared;

import org.eclipse.che.api.core.model.workspace.Workspace;

/**
 * Constants for Workspace API
 *
 * @author Yevhenii Voevodin
 */
public final class Constants {

  public static final String LINK_REL_IDE_URL = "ide";
  public static final String LINK_REL_SELF = "self";
  public static final String LINK_REL_ENVIRONMENT_OUTPUT_CHANNEL = "environment/outputChannel";
  public static final String LINK_REL_ENVIRONMENT_STATUS_CHANNEL = "environment/statusChannel";

  public static final String WORKSPACE_STOPPED_BY = "stopped_by";

  public static final String LINK_REL_CREATE_STACK = "create stack";
  public static final String LINK_REL_UPDATE_STACK = "update stack";
  public static final String LINK_REL_REMOVE_STACK = "remove stack";
  public static final String LINK_REL_GET_STACK_BY_ID = "get stack by id";
  public static final String LINK_REL_GET_STACKS_BY_CREATOR = "get stacks by creator";
  public static final String LINK_REL_SEARCH_STACKS = "search stacks";

  public static final String LINK_REL_GET_ICON = "get icon link";
  public static final String LINK_REL_UPLOAD_ICON = "upload icon link";
  public static final String LINK_REL_DELETE_ICON = "delete icon link";

  public static final String WS_AGENT_PROCESS_NAME = "CheWsAgent";

  public static final String CHE_WORKSPACE_AUTO_START = "che.workspace.auto_start";

  /**
   * Describes time when workspace was created. Should be set/read from {@link
   * Workspace#getAttributes}
   */
  public static final String CREATED_ATTRIBUTE_NAME = "created";
  /**
   * Describes time when workspace was last updated or started. Should be set/read from {@link
   * Workspace#getAttributes}
   */
  public static final String UPDATED_ATTRIBUTE_NAME = "updated";
  /**
   * Describes time when workspace was last stopped. Should be set/read from {@link
   * Workspace#getAttributes}
   */
  public static final String STOPPED_ATTRIBUTE_NAME = "stopped";
  /**
   * Indicates that last workspace stop was abnormal. Should be set/read from {@link
   * Workspace#getAttributes}
   */
  public static final String STOPPED_ABNORMALLY_ATTRIBUTE_NAME = "stoppedAbnormally";
  /**
   * Describes latest workspace runtime error message. Should be set/read from {@link
   * Workspace#getAttributes}
   */
  public static final String ERROR_MESSAGE_ATTRIBUTE_NAME = "errorMessage";
  /**
   * Describes workspace runtimes which perform start/stop of this workspace. Should be set/read
   * from {@link Workspace#getAttributes}
   */
  public static final String WORKSPACE_RUNTIMES_ID_ATTRIBUTE = "org.eclipse.che.runtimes_id";

  public static final String COMMAND_PREVIEW_URL_ATTRIBUTE_NAME = "previewUrl";
  public static final String COMMAND_GOAL_ATTRIBUTE_NAME = "goal";

  public static final String WORKSPACE_STATUS_CHANGED_METHOD = "workspace/statusChanged";
  public static final String MACHINE_STATUS_CHANGED_METHOD = "machine/statusChanged";
  public static final String SERVER_STATUS_CHANGED_METHOD = "server/statusChanged";
  public static final String MACHINE_LOG_METHOD = "machine/log";
  public static final String INSTALLER_LOG_METHOD = "installer/log";
  public static final String INSTALLER_STATUS_CHANGED_METHOD = "installer/statusChanged";
  public static final String BOOTSTRAPPER_STATUS_CHANGED_METHOD = "bootstrapper/statusChanged";

  public static final String SERVER_WS_AGENT_HTTP_REFERENCE = "wsagent/http";
  public static final String SERVER_WS_AGENT_WEBSOCKET_REFERENCE = "wsagent/ws";
  public static final String SERVER_TERMINAL_REFERENCE = "terminal";
  public static final String SERVER_SSH_REFERENCE = "ssh";
  public static final String SERVER_EXEC_AGENT_HTTP_REFERENCE = "exec-agent/http";
  public static final String SERVER_EXEC_AGENT_WEBSOCKET_REFERENCE = "exec-agent/ws";

  public static final String WS_AGENT_PORT = "4401/tcp";

  public static final String WS_MACHINE_NAME = "default";

  public static final String SUPPORTED_RECIPE_TYPES = "supportedRecipeTypes";

  private Constants() {}
}
