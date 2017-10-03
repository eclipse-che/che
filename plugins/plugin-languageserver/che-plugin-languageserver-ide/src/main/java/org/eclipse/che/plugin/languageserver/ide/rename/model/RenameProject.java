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

package org.eclipse.che.plugin.languageserver.ide.rename.model;

import java.util.List;
import org.eclipse.che.ide.api.resources.Project;

/** */
public class RenameProject {
  private final Project project;
  private final List<RenameFolder> folders;

  public RenameProject(Project project, List<RenameFolder> folders) {
    this.project = project;
    this.folders = folders;
  }

  public String getName() {
    return project.getName();
  }

  public List<RenameFolder> getFolders() {
    return folders;
  }
}
