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
package org.eclipse.che.plugin.maven.server.projecttype;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
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
    RegisteredProject project = projectManager.getClosestOrNull(wsPath);
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
