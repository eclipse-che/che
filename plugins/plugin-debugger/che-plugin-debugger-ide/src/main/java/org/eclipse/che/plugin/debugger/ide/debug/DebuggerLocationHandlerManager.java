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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.debug.shared.model.Location;

/** @author Anatolii Bazko */
@Singleton
public class DebuggerLocationHandlerManager {

  private final Set<DebuggerLocationHandler> handlers;
  private final DebuggerLocationHandler defaultHandler;

  @Inject
  public DebuggerLocationHandlerManager(FileResourceLocationHandler defaultHandler) {
    this.handlers = new HashSet<>();
    this.defaultHandler = defaultHandler;
  }

  /** Returns handler for the given location. */
  public DebuggerLocationHandler getOrDefault(Location location) {
    for (DebuggerLocationHandler handler : handlers) {
      if (handler.isSuitedFor(location)) {
        return handler;
      }
    }
    return defaultHandler;
  }

  public void register(DebuggerLocationHandler handler) {
    handlers.add(handler);
  }
}
