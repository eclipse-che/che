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
package org.eclipse.che.plugin.gdb.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.gdb.server.parser.GdbContinue;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLine;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoProgram;
import org.eclipse.che.plugin.gdb.server.parser.GdbPType;
import org.eclipse.che.plugin.gdb.server.parser.GdbPrint;
import org.eclipse.che.plugin.gdb.server.parser.GdbRun;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Anatoliy Bazko */
public class GdbTest {

  private String file;
  private Path sourceDirectory;
  private Gdb gdb;
  private BlockingQueue<DebuggerEvent> events;
  private int port;

  @BeforeClass
  public void beforeClass() throws Exception {
    file = GdbTest.class.getResource("/hello").getFile();
    sourceDirectory = Paths.get(GdbTest.class.getResource("/h.cpp").getFile());
    port = Integer.parseInt(System.getProperty("debug.port"));
    events = new ArrayBlockingQueue<>(10);
  }

  @BeforeMethod
  public void setUp() throws Exception {
    gdb = Gdb.start();
    gdb.directory(sourceDirectory.getParent().toString());
  }

  @AfterMethod
  public void tearDown() throws Exception {
    gdb.stop();
  }

  @Test
  public void testInit() throws Exception {
    assertNotNull(gdb.getGdbVersion());
    assertNotNull(gdb.getGdbVersion().getName());
    assertNotNull(gdb.getGdbVersion().getVersion());
  }

  @Test
  public void testQuit() throws Exception {
    gdb.quit();
  }

  @Test
  public void testFile() throws Exception {
    gdb.file(file);
  }

  @Test
  public void testTargetRemote() throws Exception {
    gdb.file(file);
    gdb.targetRemote("localhost", port);
    try {
      gdb.breakpoint(7);

      GdbContinue gdbContinue = gdb.cont();

      Breakpoint breakpoint = gdbContinue.getBreakpoint();
      assertNotNull(breakpoint);
      assertEquals(breakpoint.getLocation().getTarget(), "h.cpp");
      assertEquals(breakpoint.getLocation().getLineNumber(), 7);
    } finally {
      gdb.finish();
    }
  }

  @Test(expectedExceptions = DebuggerException.class)
  public void testTargetRemoteFailWhenNoGdbServer() throws Exception {
    gdb.file(file);
    gdb.targetRemote("localhost", 1111);
  }

  @Test
  public void testBreakpoints() throws Exception {
    gdb.file(file);

    gdb.breakpoint(7);
    gdb.clear(7);

    gdb.breakpoint("h.cpp", 8);
    gdb.clear("h.cpp", 8);

    gdb.breakpoint(7);
    gdb.breakpoint(8);

    GdbInfoBreak gdbInfoBreak = gdb.infoBreak();
    List<Breakpoint> breakpoints = gdbInfoBreak.getBreakpoints();

    assertEquals(breakpoints.size(), 2);

    gdb.delete();

    gdbInfoBreak = gdb.infoBreak();
    breakpoints = gdbInfoBreak.getBreakpoints();

    assertTrue(breakpoints.isEmpty());
  }

  @Test
  public void testRun() throws Exception {
    gdb.file(file);
    gdb.breakpoint(7);

    GdbRun gdbRun = gdb.run();

    assertNotNull(gdbRun.getBreakpoint());
  }

  @Test
  public void testInfoLine() throws Exception {
    gdb.file(file);
    gdb.breakpoint(7);
    gdb.run();

    GdbInfoLine gdbInfoLine = gdb.infoLine();

    assertNotNull(gdbInfoLine.getLocation());
    assertEquals(gdbInfoLine.getLocation().getLineNumber(), 7);
    assertEquals(gdbInfoLine.getLocation().getTarget(), "h.cpp");
  }

  @Test
  public void testStep() throws Exception {
    gdb.file(file);
    gdb.breakpoint(7);
    gdb.run();

    GdbInfoLine gdbInfoLine = gdb.step();
    assertNotNull(gdbInfoLine.getLocation());

    gdbInfoLine = gdb.step();
    assertNotNull(gdbInfoLine.getLocation());
  }

  @Test
  public void testNext() throws Exception {
    gdb.file(file);
    gdb.breakpoint(7);
    gdb.run();

    GdbInfoLine gdbInfoLine = gdb.next();

    assertNotNull(gdbInfoLine.getLocation());
    assertEquals(gdbInfoLine.getLocation().getLineNumber(), 5);
    assertEquals(gdbInfoLine.getLocation().getTarget(), "h.cpp");

    gdbInfoLine = gdb.next();

    assertNotNull(gdbInfoLine.getLocation());
    assertEquals(gdbInfoLine.getLocation().getLineNumber(), 6);
    assertEquals(gdbInfoLine.getLocation().getTarget(), "h.cpp");
  }

  @Test
  public void testVariables() throws Exception {
    gdb.file(file);
    gdb.breakpoint(7);
    gdb.run();

    GdbPrint gdbPrint = gdb.print("i");
    assertEquals(gdbPrint.getValue(), "0");

    gdb.setVar("i", "1");

    gdbPrint = gdb.print("i");
    assertEquals(gdbPrint.getValue(), "1");

    GdbPType gdbPType = gdb.ptype("i");
    assertEquals(gdbPType.getType(), "int");
  }

  @Test
  public void testInfoProgram() throws Exception {
    gdb.file(file);

    GdbInfoProgram gdbInfoProgram = gdb.infoProgram();
    assertNull(gdbInfoProgram.getStoppedAddress());

    gdb.breakpoint(4);
    gdb.run();

    gdbInfoProgram = gdb.infoProgram();
    assertNotNull(gdbInfoProgram.getStoppedAddress());

    GdbContinue gdbContinue = gdb.cont();
    assertNull(gdbContinue.getBreakpoint());

    gdbInfoProgram = gdb.infoProgram();
    assertNull(gdbInfoProgram.getStoppedAddress());
  }
}
