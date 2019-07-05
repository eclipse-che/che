/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.exceptions;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Simple timeout exception.
 *
 * @author Bartlomiej Laczkowski
 */
@SuppressWarnings("serial")
public class ZendDbgTimeoutException extends DebuggerException {

  public ZendDbgTimeoutException(int timeout, TimeUnit unit) {
    super(MessageFormat.format("Response timeout ({0} {1}) occurred.", timeout, unit.name()));
  }
}
