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
package org.eclipse.che.plugin.maven.client.resource;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.POM_XML;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.UNKNOWN_VALUE;

import com.google.inject.Singleton;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.ResourceInterceptor;
import org.eclipse.che.ide.api.resources.marker.PresentableTextMarker;

/**
 * Intercept java based files (.java), cut extension and adds the marker which is responsible for
 * displaying presentable text to the corresponding resource.
 *
 * @author Vlad Zhukovskiy
 * @since 4.4.0
 */
@Singleton
public class PomInterceptor implements ResourceInterceptor {

  /** {@inheritDoc} */
  @Override
  public void intercept(Resource resource) {
    if (resource.isFile() && POM_XML.equals(resource.getName())) {
      Project project = resource.getProject();

      if (project != null
          && project.isTypeOf(MAVEN_ID)
          && resource.getParent().getLocation().equals(project.getLocation())) {
        String artifact = project.getAttribute(ARTIFACT_ID);
        if (!isNullOrEmpty(artifact) && !UNKNOWN_VALUE.equals(artifact)) {
          resource.addMarker(new PresentableTextMarker(artifact));
        }
      }
    }
  }
}
