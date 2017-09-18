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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/** @author Anatolii Bazko */
@Singleton
public class DebuggerResourceHandlerFactory {

  private final Map<String, DebuggerResourceHandler> handlers;
  private final DebuggerResourceHandler defaultHandler;

  @Inject
  public DebuggerResourceHandlerFactory(DefaultDebuggerResourceHandler defaultHandler) {
    this.handlers = new HashMap<>();
    this.defaultHandler = defaultHandler;
  }

  /**
   * Returns {@link DebuggerResourceHandler} by its type or {@link DefaultDebuggerResourceHandler}.
   */
  public DebuggerResourceHandler getOrDefault(String debuggerType) {
    return handlers.getOrDefault(debuggerType, defaultHandler);
  }

  /** Registers {@link DebuggerResourceHandler} per its type. */
  public void register(String debuggerType, DebuggerResourceHandler handler) {
    handlers.put(debuggerType, handler);
  }
}
