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

import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/** Qualifies a project according to registered project types */
public interface ProjectQualifier {
  ProjectTypeResolution qualify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException;

  /**
   * Qualify a project
   *
   * @param wsPath absolute workspace path of a project
   * @return
   * @throws ServerException is thrown if an error happened during operation execution
   * @throws NotFoundException is throw if there is not project located at specified path
   */
  List<ProjectTypeResolution> qualify(String wsPath) throws ServerException, NotFoundException;
}
