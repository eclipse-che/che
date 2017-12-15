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
package org.eclipse.che.ide.ext.git.client;

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;

/**
 * @author Vlad Zhukovskiy
 * @author Mykola Morhun
 */
public class GitUtil {

  public static boolean isUnderGit(Project project) {
    return isUnderGit((ProjectConfig) project);
  }

  public static boolean isUnderGit(ProjectConfig project) {
    checkArgument(project != null, "Null project occurred");

    final Map<String, List<String>> attributes = project.getAttributes();
    final List<String> values = attributes.get(VCS_PROVIDER_NAME);

    return values != null && values.contains("git");
  }

  /**
   * Returns the root project for the given file resource or null if the file isn't in a project.
   */
  @Nullable
  public static Container getRootProject(final File file) {
    Container project = file.getProject();
    Container parentProject = file.getParent();

    while (parentProject != null) {
      project = parentProject;
      parentProject = parentProject.getParent();
    }

    return project;
  }
}
