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
package org.eclipse.che.api.project.server.notification;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Is published before project is deleted.
 *
 * @author Anatolii Bazko
 */
@EventOrigin("project")
public class PreProjectDeletedEvent {

  private String projectPath;

  public PreProjectDeletedEvent(String projectPath) {
    this.projectPath = projectPath;
  }

  public String getProjectPath() {
    return projectPath;
  }

  @Override
  public String toString() {
    return "PreProjectDeletedEvent{" + "projectPath='" + projectPath + '\'' + '}';
  }
}
