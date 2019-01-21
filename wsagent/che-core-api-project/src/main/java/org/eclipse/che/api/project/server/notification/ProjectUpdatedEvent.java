/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.notification;

import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Publish when a project is updated.
 *
 * @author Anatolii Bazko
 */
@EventOrigin("project")
public class ProjectUpdatedEvent {

  private String projectPath;
  private ProjectConfig oldConfig;

  public ProjectUpdatedEvent(String projectPath, ProjectConfig oldConfig) {
    this.projectPath = projectPath;
    this.oldConfig = oldConfig;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public ProjectConfig getOldConfig() {
    return oldConfig;
  }

  @Override
  public String toString() {
    return "ProjectUpdatedEvent{" + "projectPath='" + projectPath + '\'' + '}';
  }
}
