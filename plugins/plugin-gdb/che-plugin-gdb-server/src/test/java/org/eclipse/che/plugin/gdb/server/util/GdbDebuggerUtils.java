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
package org.eclipse.che.plugin.gdb.server.util;

import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.plugin.gdb.server.GdbDebugger;
import org.eclipse.che.plugin.gdb.server.GdbDebuggerFactory;

/** @author Mykola Morhun */
public class GdbDebuggerUtils {

  public static GdbDebugger connectToGdb(BlockingQueue<DebuggerEvent> debuggerEvents)
      throws Exception {
    Map<String, String> connectionProperties =
        ImmutableMap.of("host", "localhost", "port", getProperty("debug.port"));
    GdbDebuggerFactory factory = new GdbDebuggerFactory();
    GdbDebugger debugger = (GdbDebugger) factory.create(connectionProperties, debuggerEvents::add);
    debugger.start(new StartActionImpl(emptyList()));
    return debugger;
  }
}
