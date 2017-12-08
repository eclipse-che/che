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
package org.eclipse.che.plugin.jdb.server;

import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.ensureDebuggerSuspendAtLocation;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ThreadDump when all threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class EvaluateExpressionTest {

  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> debuggerEvents = new ArrayBlockingQueue<>(10);;

  @BeforeClass
  public void setUp() throws Exception {
    Location location =
        new LocationImpl("/test/src/org/eclipse/EvaluateExpressionTest.java", 22, "/test");
    debugger = startJavaDebugger(new BreakpointImpl(location), debuggerEvents);
    ensureDebuggerSuspendAtLocation(location, debuggerEvents);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (debugger != null) {
      terminateVirtualMachineQuietly(debugger);
    }
  }

  @Test(dataProvider = "evaluateExpression")
  public void shouldEvaluateExpression(String expression, String expectedResult, int frameIndex)
      throws Exception {
    Optional<ThreadState> main =
        debugger.getThreadDump().stream().filter(t -> t.getName().equals("main")).findAny();
    assertTrue(main.isPresent());

    ThreadState mainThread = main.get();

    String actualResult = debugger.evaluate(expression, mainThread.getId(), frameIndex);
    assertEquals(actualResult, expectedResult);
  }

  @DataProvider(name = "evaluateExpression")
  public static Object[][] getEvaluateExpression() {
    return new Object[][] {
      {"i+1", "3", 0},
      {"2+2", "4", 0},
      {"\"hello\"+\"world\"", "\"helloworld\"", 0},
      {"i+1", "2", 1}
    };
  }
}
