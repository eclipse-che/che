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
package org.eclipse.che.ide.ext.java.client.resource;

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;
import static org.eclipse.che.ide.ext.java.shared.ContentRoot.SOURCE;

import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.ide.ext.java.shared.ContentRoot;
import org.eclipse.che.ide.resource.Path;

/**
 * Intercepts given resource and sets source folder marker if current resource is folder and its
 * path equals to configured in project.
 *
 * @author Vlad Zhukovskiy
 */
public class SourceFolderInterceptor implements ResourceInterceptor {

  @Override
  public final void intercept(Resource resource) {
    checkArgument(resource != null, "Null resource occurred");

    if (resource.isFolder()) {
      final Optional<Project> project = resource.getRelatedProject();

      if (project.isPresent() && isJavaProject(project.get())) {
        final Path resourcePath = resource.getLocation();

        for (Path path : getPaths(project.get(), getAttribute())) {
          if (path.equals(resourcePath)) {
            resource.addMarker(new SourceFolderMarker(getContentRoot()));
            return;
          }
        }
      }
    }
  }

  protected ContentRoot getContentRoot() {
    return SOURCE;
  }

  protected String getAttribute() {
    return Constants.SOURCE_FOLDER;
  }

  protected final Path[] getPaths(Project project, String srcType) {
    final List<String> srcFolders = project.getAttributes().get(srcType);

    if (srcFolders == null || srcFolders.isEmpty()) {
      return new Path[0];
    }

    Path[] paths = new Path[0];

    for (String srcFolder : srcFolders) {
      final int index = paths.length;
      paths = Arrays.copyOf(paths, index + 1);
      paths[index] = project.getLocation().append(srcFolder);
    }

    return paths;
  }
}
