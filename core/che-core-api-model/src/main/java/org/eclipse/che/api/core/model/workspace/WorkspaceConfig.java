/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.model.workspace;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Defines workspace configuration.
 *
 * @author gazarenkov
 * @author Yevhenii Voevodin
 */
public interface WorkspaceConfig {

  /** Returns the name of the current workspace instance. Workspace name is unique per namespace. */
  String getName();

  /** Returns description of workspace. */
  @Nullable
  String getDescription();

  /**
   * Returns default environment name. It is mandatory, implementation should guarantee that
   * environment with returned name exists for current workspace config.
   */
  @Nullable
  String getDefaultEnv();

  /**
   * Returns commands which are related to workspace, when workspace doesn't contain commands
   * returns empty list. It is optional, workspace may contain 0 or N commands.
   */
  List<? extends Command> getCommands();

  /**
   * Returns project configurations which are related to workspace, when workspace doesn't contain
   * projects returns empty list. It is optional, workspace may contain 0 or N project
   * configurations.
   */
  List<? extends ProjectConfig> getProjects();

  /**
   * Returns mapping of environment names to environment configurations. Workspace must contain at
   * least 1 default environment and may contain N environments.
   */
  Map<String, ? extends Environment> getEnvironments();

  /**
   * Returns workspace config attributes. Workspace config attributes must not contain null keys or
   * values.
   */
  Map<String, String> getAttributes();

  /** Returns devfile that was to generating workspace config, null otherwise. */
  Devfile getDevfile();
}
