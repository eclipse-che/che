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

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Publish when a project is updated.
 *
 * @author Anatolii Bazko
 */
@EventOrigin("project")
public class ProjectUpdatedEvent {

  private String projectPath;

  public ProjectUpdatedEvent(String projectPath) {
    this.projectPath = projectPath;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }

  @Override
  public String toString() {
    return "ProjectUpdatedEvent{" + "projectPath='" + projectPath + '\'' + '}';
  }
}
