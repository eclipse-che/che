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
package org.eclipse.che.ide.websocket.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test for {@link WebSocketPropertyManager}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketPropertyManagerTest {
  @InjectMocks private WebSocketPropertyManager propertyManager;

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void shouldInitializeDefaultDelayOnInitialize() {
    propertyManager.initializeConnection("url");

    final int delay = propertyManager.getConnectionDelay("url");
    assertEquals(0, delay);
  }

  @Test
  public void shouldInitializeDefaultAttemptsOnInitialize() {
    propertyManager.initializeConnection("url");

    final int attempts = propertyManager.getReConnectionAttempts("url");

    assertEquals(0, attempts);
  }

  @Test
  public void shouldInitializeDefaultUrlOnInitialize() {
    propertyManager.initializeConnection("url");

    final String url = propertyManager.getUrl("url");

    assertEquals("url", url);
  }

  @Test
  public void shouldInitializeDefaultSustainerStatusOnInitialize() {
    propertyManager.initializeConnection("url");

    final boolean sustainerEnabled = propertyManager.sustainerEnabled("url");

    assertTrue(sustainerEnabled);
  }
}
