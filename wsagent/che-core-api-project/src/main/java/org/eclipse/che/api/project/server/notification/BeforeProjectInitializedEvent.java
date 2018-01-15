/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
