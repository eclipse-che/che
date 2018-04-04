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
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.factory.Factory;

/**
 * This validator ensures that a factory can be edited by a user that has the associated rights
 * (author or account owner)
 *
 * @author Florent Benoit
 */
public interface FactoryEditValidator {

  /**
   * Validates given factory by checking the current user is granted to edit the factory.
   *
   * @param factory factory object to validate
   * @throws ForbiddenException when the current user is not granted to edit the factory
   * @throws ServerException when any other error occurs
   */
  void validate(Factory factory) throws ForbiddenException, ServerException;
}
