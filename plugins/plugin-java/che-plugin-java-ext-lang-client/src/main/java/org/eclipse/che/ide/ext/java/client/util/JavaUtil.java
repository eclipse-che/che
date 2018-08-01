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
package org.eclipse.che.ide.ext.java.client.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.shared.Constants.LANGUAGE;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.resource.Path;

/** @author Vlad Zhukovskiy */
@Beta
public class JavaUtil {

  private JavaUtil() {}

  /**
   * Returns the {@code true} if given {@code resource} is Java based file.
   *
   * <p>Java based file should has {@code .java} or {@code .class} extension.
   *
   * @param resource the resource to check
   * @return {@code true} if resource is java file, otherwise {@code false}
   * @throws IllegalArgumentException in case if given {@code resource} is null. Reason includes:
   *     <ul>
   *       <li>Null resource occurred
   *     </ul>
   *
   * @since 4.4.0
   */
  public static boolean isJavaFile(Resource resource) {
    checkArgument(resource != null, "Null resource occurred");

    if (resource.getResourceType() == FILE) {
      final String ext = ((File) resource).getExtension();

      return !isNullOrEmpty(ext) && "java".equals(ext) || "class".equals(ext);
    }

    return false;
  }

  /**
   * Returns the {@code true} if given {@code project} is Java based project.
   *
   * <p>Java based project should has {@code java} string value in {@link Constants#LANGUAGE}
   * attribute.
   *
   * @param project the project to check
   * @return {@code true} if project is java based, otherwise {@code false}
   * @throws IllegalArgumentException in case if given {@code project} is null. Reason includes:
   *     <ul>
   *       <li>Null project occurred
   *     </ul>
   *
   * @since 4.4.0
   */
  public static boolean isJavaProject(Project project) {
    checkArgument(project != null, "Null project occurred");

    final Map<String, List<String>> attributes = project.getAttributes();

    if (attributes == null || attributes.isEmpty()) {
      return false;
    }

    final List<String> languages = attributes.get(LANGUAGE);

    return languages != null && languages.contains("java");
  }

  /**
   * Resolves fully qualified name based on base on start point container and end point resource.
   *
   * <p>Usually as start point there is a source directory and as end point there is a package
   * fragment or java file.
   *
   * @param startPoint the start point container from which fqn should be resolved
   * @param endPoint the end point resource to which fqn should be resolved
   * @return the resolved fully qualified name
   * @throws IllegalArgumentException in case if given arguments are invalid. Reason includes:
   *     <ul>
   *       <li>Null source folder occurred
   *       <li>Null resource occurred
   *       <li>Given base folder is not prefix of checked resource
   *     </ul>
   *
   * @since 4.4.0
   */
  public static String resolveFQN(Container startPoint, Resource endPoint) {
    checkArgument(startPoint != null, "Null source folder occurred");
    checkArgument(endPoint != null, "Null resource occurred");
    checkArgument(
        startPoint.getLocation().isPrefixOf(endPoint.getLocation()),
        "Given base folder is not prefix of checked resource");

    Path path = endPoint.getLocation().removeFirstSegments(startPoint.getLocation().segmentCount());

    if (isJavaFile(endPoint)) {
      final String ext = ((File) endPoint).getExtension();

      if (!isNullOrEmpty(ext)) {
        final String name = endPoint.getName();
        path =
            path.removeLastSegments(1).append(name.substring(0, name.length() - ext.length() - 1));
      }
    }

    return path.toString().replace('/', '.');
  }

  public static String resolveFQN(Resource resource) {
    final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

    if (!srcFolder.isPresent()) {
      throw new IllegalStateException(
          "Fully qualified name can not be resolved for '" + resource.getLocation() + "'");
    }

    return resolveFQN((Container) srcFolder.get(), resource);
  }

  public static String resolveFQN(VirtualFile file) {
    checkArgument(file instanceof File, "Given file is not resource based");

    return resolveFQN((Resource) file);
  }
}
