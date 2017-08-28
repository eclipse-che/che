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
package org.eclipse.che.plugin.jdb.server.exceptions;

import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/** @author Anatoliy Bazko */
public class DebuggerAbsentInformationException extends DebuggerException {
  public DebuggerAbsentInformationException(String message, Exception cause) {
    super(message, cause);
  }
}
