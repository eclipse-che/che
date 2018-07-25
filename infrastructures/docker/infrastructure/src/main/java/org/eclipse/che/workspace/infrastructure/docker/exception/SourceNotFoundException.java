/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.exception;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/** @author Alexander Garagatyi */
public class SourceNotFoundException extends InfrastructureException {
  public SourceNotFoundException(String message) {
    super(message);
  }

  public SourceNotFoundException(Exception e) {
    super(e);
  }

  public SourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
