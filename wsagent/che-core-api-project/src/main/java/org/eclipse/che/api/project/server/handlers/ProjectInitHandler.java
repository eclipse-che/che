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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

/**
 * Called after project initialized.
 *
 * @author gazarenkov
 */
public interface ProjectInitHandler extends ProjectHandler {

  /**
   * Handler to be fired after initialization of project.
   *
   * @param projectFolder base project folder
   * @throws ServerException
   * @throws ForbiddenException
   * @throws ConflictException
   * @throws NotFoundException
   */
  void onProjectInitialized(String projectFolder)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException;
}
