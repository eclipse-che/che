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

import org.eclipse.che.api.installer.server.impl.InstallerFqn;

/**
 * Is thrown when installer with the same {@link InstallerFqn} already exists.
 *
 * @author Anatolii Bazko
 */
public class InstallerAlreadyExistsException extends InstallerException {
  public InstallerAlreadyExistsException(String message) {
    super(message);
  }

  public InstallerAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
