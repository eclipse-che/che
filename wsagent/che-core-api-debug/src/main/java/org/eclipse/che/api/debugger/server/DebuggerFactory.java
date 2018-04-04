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
package org.eclipse.che.api.debugger.server;

import java.util.Map;
import org.eclipse.che.api.debugger.server.Debugger.DebuggerCallback;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;

/**
 * Responsibility to create a new {@link Debugger} instance of specific type every time client
 * starts a new debugger session.
 *
 * @author Anatoliy Bazko
 */
public interface DebuggerFactory {

  /** Returns a debugger type. */
  String getType();

  /**
   * Creates a new {@link Debugger} instance with given properties.
   *
   * @param properties the specific properties to create debugger with
   * @param debuggerCallback is used to sent any events back to the client
   * @return {@link Debugger}
   * @throws DebuggerException if any exception occurred
   */
  Debugger create(Map<String, String> properties, DebuggerCallback debuggerCallback)
      throws DebuggerException;
}
