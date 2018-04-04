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
package org.eclipse.che.api.factory.server.impl;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.factory.server.FactoryCreateValidator;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;

/** Factory creation stage validator. */
@Singleton
public class FactoryCreateValidatorImpl extends FactoryBaseValidator
    implements FactoryCreateValidator {
  private WorkspaceValidator workspaceConfigValidator;

  @Inject
  public FactoryCreateValidatorImpl(WorkspaceValidator workspaceConfigValidator) {
    this.workspaceConfigValidator = workspaceConfigValidator;
  }

  @Override
  public void validateOnCreate(FactoryDto factory)
      throws BadRequestException, ServerException, ForbiddenException, NotFoundException {
    validateProjects(factory);
    validateCurrentTimeAfterSinceUntil(factory);
    validateProjectActions(factory);
    try {
      workspaceConfigValidator.validateConfig(factory.getWorkspace());
    } catch (ValidationException x) {
      throw new BadRequestException(x.getMessage());
    }
  }
}
