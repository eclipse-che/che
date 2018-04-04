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
package org.eclipse.che.ide.websocket.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link UrlResolver}
 *
 * @author Dmitry Kuleshov
 */
public class UrlResolverTest {
  private UrlResolver urlResolver = new UrlResolver();

  @Test
  public void shouldResolveUrl() {
    urlResolver.setMapping("id", "url");

    final String id = urlResolver.resolve("url");

    assertEquals("id", id);
  }

  @Test
  public void shouldResolveId() {
    urlResolver.setMapping("id", "url");

    final String url = urlResolver.getUrl("id");

    assertEquals("url", url);
  }
}
