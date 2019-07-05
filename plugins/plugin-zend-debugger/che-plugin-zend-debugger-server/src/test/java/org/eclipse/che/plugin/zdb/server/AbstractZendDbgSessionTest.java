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
package org.eclipse.che.plugin.zdb.server;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.event.BreakpointActivatedEvent;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.event.SuspendEvent;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.plugin.zdb.server.connection.ZendDbgSettings;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Abstract Zend Debugger session test base.
 *
 * @author Bartlomiej Laczkowski
 */
public abstract class AbstractZendDbgSessionTest {

  protected static final String DEFAULT_HOST = "127.0.0.1";
  protected static final int DEFAULT_PORT = 10137;
  private static final String QUERY_SSL =
      "QUERY_STRING=start_debug=1&debug_host=127.0.0.1&debug_port=10137&use_ssl=1";
  private static final String QUERY_NO_SSL =
      "QUERY_STRING=start_debug=1&debug_host=127.0.0.1&debug_port=10137";
  protected ZendDebugger debugger;
  protected BlockingQueue<DebuggerEvent> dbgEvents;
  private Process dbgEngineProcess;

  @BeforeMethod
  public void setUp() throws Exception {
    dbgEvents = new ArrayBlockingQueue<>(10);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    debugger.disconnect();
    if (!dbgEngineProcess.waitFor(5, TimeUnit.SECONDS)) {
      dbgEngineProcess.destroyForcibly();
    }
  }

  protected ZendDbgSettings getDbgSettings(boolean breakAtFirstLine, boolean useSsslEncryption) {
    return new ZendDbgSettings(DEFAULT_PORT, DEFAULT_HOST, breakAtFirstLine, useSsslEncryption);
  }

  protected void awaitSuspend(String dbgFile, int lineNumber) throws Exception {
    DebuggerEvent debuggerEvent = dbgEvents.poll(5, TimeUnit.SECONDS);
    if (debuggerEvent == null) {
      throw new Exception("Suspend event timeout occurred.");
    }
    assertTrue(debuggerEvent instanceof SuspendEvent);
    SuspendEvent suspendEvent = (SuspendEvent) debuggerEvent;
    assertEquals(suspendEvent.getLocation().getTarget(), dbgFile);
    assertEquals(suspendEvent.getLocation().getLineNumber(), lineNumber);
  }

  protected void awaitBreakpointActivated(Breakpoint breakpoint) throws Exception {
    DebuggerEvent debuggerEvent = dbgEvents.poll(5, TimeUnit.SECONDS);
    if (debuggerEvent == null) {
      throw new Exception("Breakpoint activated event timeout occurred.");
    }
    assertTrue(debuggerEvent instanceof BreakpointActivatedEvent);
    BreakpointActivatedEvent bpActivatedEvent = (BreakpointActivatedEvent) debuggerEvent;
    assertEquals(bpActivatedEvent.getBreakpoint(), breakpoint);
  }

  protected void triggerSession(String dbgFile, ZendDbgSettings dbgSettings, FsManager fsManager)
      throws Exception {
    triggerSession(dbgFile, dbgSettings, Collections.emptyList(), fsManager);
  }

  protected void triggerSession(
      String dbgFile,
      ZendDbgSettings dbgSettings,
      List<Breakpoint> dbgBreakpoints,
      FsManager fsManager)
      throws Exception {
    ZendDbgLocationHandler dbgLocationMapper = mock(ZendDbgLocationHandler.class);
    // No need to convert between VFS and DBG for test purposes
    when(dbgLocationMapper.convertToVFS(anyObject())).then(returnsFirstArg());
    when(dbgLocationMapper.convertToDBG(anyObject())).then(returnsFirstArg());
    debugger = new ZendDebugger(dbgSettings, dbgLocationMapper, dbgEvents::add, fsManager);
    debugger.start(new StartActionImpl(dbgBreakpoints));
    dbgEngineProcess =
        Runtime.getRuntime()
            .exec(
                "php " + dbgFile,
                new String[] {dbgSettings.isUseSsslEncryption() ? QUERY_SSL : QUERY_NO_SSL});
  }
}
