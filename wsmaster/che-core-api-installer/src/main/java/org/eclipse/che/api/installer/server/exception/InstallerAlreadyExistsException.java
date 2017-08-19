/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
