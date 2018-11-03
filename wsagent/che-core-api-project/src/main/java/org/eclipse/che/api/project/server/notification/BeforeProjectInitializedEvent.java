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
 * Publish before project initialization.
 *
 * @author Valeriy Svydenko
 */
@EventOrigin("project")
public class BeforeProjectInitializedEvent {

  private ProjectConfig projectConfig;

  public BeforeProjectInitializedEvent(ProjectConfig projectConfig) {
    this.projectConfig = projectConfig;
  }

  public ProjectConfig getProjectConfig() {
    return projectConfig;
  }

  public void setProjectConfig(ProjectConfig projectConfig) {
    this.projectConfig = projectConfig;
  }

  @Override
  public String toString() {
    return "BeforeProjectInitializedEvent{" + "projectConfig='" + projectConfig + '\'' + '}';
  }
}
