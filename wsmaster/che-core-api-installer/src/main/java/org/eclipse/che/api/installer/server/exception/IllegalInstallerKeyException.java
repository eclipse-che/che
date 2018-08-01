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
package org.eclipse.che.api.installer.server.exception;

import org.eclipse.che.api.core.BadRequestException;

/**
 * Is thrown when installer key has a wrong format
 *
 * @see org.eclipse.che.api.installer.server.impl.InstallerFqn#parse(String)
 * @author Sergii Leshchenko
 */
public class IllegalInstallerKeyException extends InstallerException {
  public IllegalInstallerKeyException(String message) {
    super(message);
  }

  public IllegalInstallerKeyException(String message, BadRequestException cause) {
    super(message, cause);
  }
}
