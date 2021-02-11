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
package org.eclipse.che.api.factory.server.impl;

import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.FactoryAcceptValidator;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;

/** Factory accept stage validator. */
@Singleton
public class FactoryAcceptValidatorImpl extends FactoryBaseValidator
    implements FactoryAcceptValidator {

  @Override
  public void validateOnAccept(FactoryMetaDto factory) throws BadRequestException {
    validateCurrentTimeBetweenSinceUntil(factory);
    validateProjectActions(factory);
  }
}
