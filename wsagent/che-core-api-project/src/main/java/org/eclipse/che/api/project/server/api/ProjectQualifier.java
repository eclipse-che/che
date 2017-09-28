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
package org.eclipse.che.api.project.server.api;

import java.util.List;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;

public interface ProjectQualifier {
  ProjectTypeResolution qualify(String path, String projectTypeId)
      throws ServerException, NotFoundException;

  List<ProjectTypeResolution> qualify(String path) throws ServerException, NotFoundException;
}
