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
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.findMainThreadId;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.startJavaDebugger;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerTestUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointImpl;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.debug.shared.model.impl.SimpleValueImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariableImpl;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test ThreadDump when all threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class GetValueTest {

  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> debuggerEvents = new ArrayBlockingQueue<>(10);;
  private long mainThreadId;

  @BeforeClass
  public void setUp() throws Exception {
    Location location = new LocationImpl("/test/src/org/eclipse/GetValueTest.java", 27, "/test");
    debugger = startJavaDebugger(new BreakpointImpl(location), debuggerEvents);
    ensureDebuggerSuspendAtLocation(location, debuggerEvents);

    mainThreadId = findMainThreadId(debugger);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (debugger != null) {
      terminateVirtualMachineQuietly(debugger);
    }
  }

  @Test(dataProvider = "getVariablePaths")
  public void shouldGetValue(List<String> path, int frameIndex, String value) throws Exception {
    SimpleValue debuggerValue =
        debugger.getValue(new VariablePathImpl(path), mainThreadId, frameIndex);

    if (debuggerValue == null) {
      assertNull(value);
    } else {
      assertEquals(debuggerValue.getString(), value);
    }
  }

  @DataProvider(name = "getVariablePaths")
  public static Object[][] getVariablePaths() {
    return new Object[][] {
      {ImmutableList.of("i"), 0, "2"},
      {ImmutableList.of("i"), 1, null},
      {ImmutableList.of("var1"), 0, "\"var1\""},
      {ImmutableList.of("var1"), 1, null},
      {ImmutableList.of("var2"), 0, "\"var2\""},
      {ImmutableList.of("static", "var2"), 0, "\"field2\""},
      {ImmutableList.of("static", "var2"), 1, "\"field2\""},
      {ImmutableList.of("args"), 0, null}
    };
  }

  @Test
  public void shouldGetNestedVariables() throws Exception {
    SimpleValue debuggerValue =
        debugger.getValue(new VariablePathImpl(ImmutableList.of("var1")), mainThreadId, 0);

    assertEquals(debuggerValue.getString(), "\"var1\"");

    Variable hashVar =
        debuggerValue
            .getVariables()
            .stream()
            .filter(v -> v.getName().equals("value"))
            .findAny()
            .get();
    assertNotNull(hashVar);
    assertEquals(hashVar.getType(), "char[]");
    assertEquals(hashVar.getName(), "value");
    assertTrue(hashVar.getValue().getString().contains("instance of char[4]"));
    assertEquals(hashVar.getVariablePath().getPath(), ImmutableList.of("var1", "value"));

    List<? extends Variable> valueVariables = hashVar.getValue().getVariables();
    for (int i = 0; i < 4; i++) {
      Variable variable = valueVariables.get(i);
      assertEquals(
          variable.getVariablePath().getPath(), ImmutableList.of("var1", "value", "[" + i + "]"));
      assertEquals(variable.getValue().getString(), "var1".substring(i, i + 1));
    }
  }

  @Test(dataProvider = "setVariable")
  public void shouldSetValue(List<String> path, String newValue, int frameIndex) throws Exception {
    final VariablePathImpl variablePath = new VariablePathImpl(path);

    debugger.setValue(
        new VariableImpl(new SimpleValueImpl(newValue), variablePath), mainThreadId, frameIndex);

    SimpleValue debuggerValue = debugger.getValue(variablePath, mainThreadId, frameIndex);
    assertEquals(debuggerValue.getString(), newValue);
  }

  @DataProvider(name = "setVariable")
  public static Object[][] getSetVariable() {
    return new Object[][] {{ImmutableList.of("i"), "3", 0}};
  }
}
