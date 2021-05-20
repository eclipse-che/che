/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;

/** Interface for validations of factory urls on accept stage. */
public interface FactoryAcceptValidator {

  /**
   * Validates factory object on accept stage. Implementation should throw {@link
   * BadRequestException} if factory object is invalid.
   *
   * @param factory factory object to validate
   * @throws BadRequestException in case if factory is not valid
   */
  void validateOnAccept(FactoryMetaDto factory) throws BadRequestException;
}
