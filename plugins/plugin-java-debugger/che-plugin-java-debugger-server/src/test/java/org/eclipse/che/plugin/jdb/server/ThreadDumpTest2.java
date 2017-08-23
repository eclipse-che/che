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
package org.eclipse.che.plugin.jdb.server;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.plugin.jdb.server.util.JavaDebuggerUtils.terminateVirtualMachineQuietly;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.eclipse.che.api.debug.shared.dto.ThreadStateDto;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.event.DebuggerEvent;
import org.eclipse.che.api.debug.shared.model.impl.action.StartActionImpl;
import org.eclipse.che.api.debugger.server.DtoConverter;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.plugin.jdb.server.util.ProjectApiUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test thread dump feature when none of threads are suspended.
 *
 * @author Anatolii Bazko
 */
public class ThreadDumpTest2 {
  private JavaDebugger debugger;
  private BlockingQueue<DebuggerEvent> events = new ArrayBlockingQueue<>(10);

  @BeforeClass
  public void setUp() throws Exception {
    ProjectApiUtils.ensure();
    initJavaDebugger();
  }

  @AfterClass
  public void tearDown() throws Exception {
    terminateVirtualMachineQuietly(debugger);
  }

  @Test
  public void shouldGetThreadDump() throws Exception {
    List<ThreadStateDto> threads =
        debugger.getThreadDump().stream().map(DtoConverter::asDto).collect(toList());

    for (ThreadState t : threads) {
      assertFalse(t.isSuspended());
      assertTrue(t.getFrames().isEmpty());
    }
  }

  private void initJavaDebugger() throws DebuggerException, InterruptedException {
    debugger = new JavaDebugger("localhost", parseInt(getProperty("debug.port")), events::add);
    debugger.start(new StartActionImpl(Collections.emptyList()));
  }
}
