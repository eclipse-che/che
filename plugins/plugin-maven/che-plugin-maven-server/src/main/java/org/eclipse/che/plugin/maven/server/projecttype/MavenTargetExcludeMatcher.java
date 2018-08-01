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
package org.eclipse.che.plugin.maven.server.projecttype;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

@Singleton
public class MavenTargetExcludeMatcher implements PathMatcher {

  private final ProjectManager projectManager;
  private final PathTransformer pathTransformer;

  @Inject
  public MavenTargetExcludeMatcher(ProjectManager projectManager, PathTransformer pathTransformer) {
    this.projectManager = projectManager;
    this.pathTransformer = pathTransformer;
  }

  @Override
  public boolean matches(Path fsPath) {
    String wsPath = pathTransformer.transform(fsPath);
    ProjectConfig project = projectManager.getClosestOrNull(wsPath);
    if (project == null) {
      return false;
    }

    if (MavenAttributes.MAVEN_ID.equals(project.getType())) {
      String mavenTarget = project.getPath() + "/target";
      return wsPath.startsWith(mavenTarget);
    }
    return false;
  }
}
