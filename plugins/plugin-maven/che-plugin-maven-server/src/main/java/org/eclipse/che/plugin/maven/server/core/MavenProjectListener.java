/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core;

import java.util.List;
import java.util.Map;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.che.plugin.maven.server.core.project.MavenProjectModifications;

/** @author Evgen Vidolob */
public interface MavenProjectListener {

  void projectResolved(MavenProject project, MavenProjectModifications modifications);

  void projectUpdated(
      Map<MavenProject, MavenProjectModifications> updated, List<MavenProject> removed);
}
