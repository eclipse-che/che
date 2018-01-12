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
package org.eclipse.che.api.workspace.server.hc;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class TerminalHttpConnectionServerCheckerTest {
  private String MACHINE_NAME = "mach1";
  private String SERVER_REF = "ref1";

  @Mock private Timer timer;
  @Mock private HttpURLConnection conn;

  private TerminalHttpConnectionServerChecker checker;

  @BeforeMethod
  public void setUp() throws Exception {
    checker =
        new TerminalHttpConnectionServerChecker(
            new URL("http://localhost"), MACHINE_NAME, SERVER_REF, 1, 10, TimeUnit.SECONDS, timer);
  }

  @Test
  public void shouldConfirmConnectionSuccessIfResponseCodeIs404() throws Exception {
    when(conn.getResponseCode()).thenReturn(404);

    assertTrue(checker.isConnectionSuccessful(conn));
  }

  @Test
  public void shouldNotConfirmConnectionSuccessIfResponseCodeIsNot404() throws Exception {
    when(conn.getResponseCode()).thenReturn(200);

    assertFalse(checker.isConnectionSuccessful(conn));
  }
}
