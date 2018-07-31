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
package org.eclipse.che.api.debugger.server.exceptions;

import org.eclipse.che.api.core.ServerException;

/**
 * A general debugger exception.
 *
 * @author Anatoliy Bazko
 */
public class DebuggerException extends ServerException {
  public DebuggerException(String message) {
    super(message);
  }

  public DebuggerException(String message, Exception cause) {
    super(message, cause);
  }
}
