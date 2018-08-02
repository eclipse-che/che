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
package org.eclipse.che.api.user.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;

/**
 * Validates token.
 *
 * @author Eugene Voevodin
 * @see UserService
 */
public interface TokenValidator {

  /**
   * Validates {@code token}.
   *
   * @return user email
   * @throws ConflictException when token is not valid
   */
  User validateToken(String token) throws ConflictException;
}
