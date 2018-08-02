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
import org.eclipse.che.ide.resource.Path;

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

  /** Returns the first segment of the given Path or {@code null} if the Path is empty. */
  @Nullable
  public static String getRootPath(final Path path) {
    return path.segment(0);
  }
}
