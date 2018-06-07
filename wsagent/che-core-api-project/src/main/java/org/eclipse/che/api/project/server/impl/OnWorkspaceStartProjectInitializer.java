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
package org.eclipse.che.api.project.server.impl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;

@Singleton
public class OnWorkspaceStartProjectInitializer {

  private final ExecutiveProjectManager projectManager;

  @Inject
  public OnWorkspaceStartProjectInitializer(ExecutiveProjectManager projectManager) {
    this.projectManager = projectManager;
  }

  @PostConstruct
  public void initialize()
      throws ConflictException, NotFoundException, ServerException, ForbiddenException {
    projectManager.initialize();
  }
}
