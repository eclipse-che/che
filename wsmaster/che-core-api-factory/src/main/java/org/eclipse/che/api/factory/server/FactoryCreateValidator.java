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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;

/**
 * Interface for validations of factory creation stage.
 *
 * @author Alexander Garagatyi
 */
public interface FactoryCreateValidator {

  /**
   * Validates factory object on creation stage. Implementation should throw exception if factory
   * object is invalid.
   *
   * @param factory factory object to validate
   * @throws BadRequestException in case if factory is not valid
   * @throws ServerException when any server error occurs
   * @throws ForbiddenException when user have no access rights for factory creation
   */
  void validateOnCreate(FactoryDto factory)
      throws BadRequestException, ServerException, ForbiddenException, NotFoundException;
}
