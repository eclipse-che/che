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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.model.project.type.ProjectType;

/** Resolves project type */
public interface ProjectTypeResolver {

  /**
   * Resolve type for specified project
   *
   * @param type project type
   * @param wsPath absolute workspace path of a project
   * @return
   */
  ProjectTypeResolution resolve(ProjectType type, String wsPath);
}
