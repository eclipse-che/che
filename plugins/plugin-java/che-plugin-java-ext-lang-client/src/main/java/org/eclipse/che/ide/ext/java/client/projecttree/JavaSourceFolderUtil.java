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
package org.eclipse.che.ide.ext.java.client.projecttree;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.resources.Project;

/**
 * @author Vladyslav Zhukovskii
 * @author Anatoliy Bazko
 */
public class JavaSourceFolderUtil {

  /**
   * Returns source folders list of the project. Every path in the returned list starts and ends
   * with separator char /.
   */
  @NotNull
  public static List<String> getSourceFolders(@NotNull Project project) {
    String projectBuilder = getProjectBuilder(project.getType());

    return doGetSourceFolders(project, projectBuilder);
  }

  @NotNull
  private static List<String> doGetSourceFolders(Project projectConfig, String projectBuilder) {
    List<String> allSourceFolders = new LinkedList<>();

    String projectPath = removeEndingPathSeparator(projectConfig.getPath());
    Map<String, List<String>> attributes = projectConfig.getAttributes();

    List<String> sourceFolders = attributes.get(SOURCE_FOLDER);
    if (sourceFolders != null) {
      for (String sourceFolder : sourceFolders) {
        allSourceFolders.add(projectPath + addStartingPathSeparator(sourceFolder) + '/');
      }
    }

    List<String> testSourceFolders = attributes.get(projectBuilder + ".test.source.folder");
    if (testSourceFolders != null) {
      for (String testSourceFolder : testSourceFolders) {
        allSourceFolders.add(projectPath + addStartingPathSeparator(testSourceFolder) + '/');
      }
    }

    return allSourceFolders;
  }

  private static String removeEndingPathSeparator(String path) {
    if (path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }

    return path;
  }

  private static String addStartingPathSeparator(String path) {
    if (!path.startsWith("/")) {
      return "/" + path;
    }

    return path;
  }

  @Nullable
  public static String getProjectBuilder(@Nullable String projectType) {
    if (projectType == null) {
      return null;
    }

    switch (projectType) {
      case "maven":
      case "ant":
        return projectType;
      default:
        return null;
    }
  }
}
