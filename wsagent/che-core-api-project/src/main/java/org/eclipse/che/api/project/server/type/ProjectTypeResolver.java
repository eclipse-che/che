/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
