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
package org.eclipse.che.plugin.jdb.server.util;

import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.JavaDebugger;
import org.eclipse.che.plugin.jdb.server.JavaDebuggerFactory;

/** @author Anatolii Bazko */
public class JavaDebuggerTestUtils {

  /** Initializes and starts java debugger with adding the given breakpoint. */
  public static JavaDebugger startJavaDebugger(
      Breakpoint breakpoint, BlockingQueue<DebuggerEvent> debuggerEvents) throws Exception {

    JavaDebugger debugger = initJavaDebugger(debuggerEvents);
    debugger.start(new StartActionImpl(singletonList(breakpoint)));

    return debugger;
  }

  /** Initializes and starts java debugger. */
  public static JavaDebugger startJavaDebugger(BlockingQueue<DebuggerEvent> debuggerEvents)
      throws Exception {

    JavaDebugger debugger = initJavaDebugger(debuggerEvents);
    debugger.start(new StartActionImpl(emptyList()));

    return debugger;
  }

  /** Initializes java debugger. */
  public static JavaDebugger initJavaDebugger(BlockingQueue<DebuggerEvent> debuggerEvents)
      throws DebuggerException {

    Map<String, String> connectionProperties = new HashMap<>();
    connectionProperties.put("host", "localhost");
    connectionProperties.put("port", getProperty("debug.port"));

    JavaDebuggerFactory factory = new JavaDebuggerFactory(new JavaLanguageServerExtensionStub());
    return (JavaDebugger) factory.create(connectionProperties, debuggerEvents::add);
  }

  public static void ensureDebuggerSuspendAtLocation(
      Location desiredLocation, BlockingQueue<DebuggerEvent> debuggerEvents)
      throws InterruptedException {
    for (; ; ) {
      DebuggerEvent event = debuggerEvents.take();
      if (event instanceof SuspendEvent) {
        SuspendEvent suspendEvent = (SuspendEvent) event;
        Location location = suspendEvent.getLocation();

        if (location.getTarget().equals(desiredLocation.getTarget())
            && location.getLineNumber() == desiredLocation.getLineNumber()) {

          return;
        }
      }
    }
  }

  /**
   * Terminates Virtual Machine.
   *
   * @see VirtualMachine#exit(int)
   */
  public static void terminateVirtualMachineQuietly(JavaDebugger javaDebugger) throws Exception {
    Field vmField = JavaDebugger.class.getDeclaredField("vm");
    vmField.setAccessible(true);
    VirtualMachine vm = (VirtualMachine) vmField.get(javaDebugger);

    try {
      vm.exit(0);
    } catch (Exception ignored) {
      // quietly ignore exception, if VM has been already terminated
    }
  }

  /**
   * Iterates threads until main is found and returns its id.
   *
   * @see ThreadReference#uniqueID()
   */
  public static long findMainThreadId(JavaDebugger javaDebugger) throws DebuggerException {
    Optional<ThreadState> main =
        javaDebugger.getThreadDump().stream().filter(t -> t.getName().equals("main")).findAny();
    main.orElseThrow(() -> new DebuggerException("Main thread not found"));
    return main.get().getId();
  }
}
