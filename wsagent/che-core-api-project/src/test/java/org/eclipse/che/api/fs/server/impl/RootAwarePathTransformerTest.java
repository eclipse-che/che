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
package org.eclipse.che.api.fs.server.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests for {@link RootAwarePathTransformer} */
public class RootAwarePathTransformerTest {

  private RootAwarePathTransformer rootAwarePathTransformer;

  @BeforeMethod
  public void setUp() throws Exception {
    rootAwarePathTransformer = new RootAwarePathTransformer(Paths.get("/").toFile());
  }

  @Test
  public void shouldReturnFsRootWhenWsRootIsTransformed() throws Exception {
    Path expected = Paths.get("/");

    Path actual = rootAwarePathTransformer.transform(WsPathUtils.ROOT);

    assertEquals(actual, expected);
  }

  @Test
  public void shouldReturnWsRootWhenFsRootIsTransformed() throws Exception {
    String expected = WsPathUtils.ROOT;

    String actual = rootAwarePathTransformer.transform(Paths.get("/"));

    assertEquals(actual, expected);
  }

  @Test
  public void shouldTransformAbsoluteWsPath() throws Exception {
    Path expected = Paths.get("/a/b/c");

    Path actual = rootAwarePathTransformer.transform("/a/b/c");

    assertEquals(actual, expected);
  }

  @Test
  public void shouldTransformRelativeWsPath() throws Exception {
    Path expected = Paths.get("/a/b/c");

    Path actual = rootAwarePathTransformer.transform("a/b/c");

    assertEquals(actual, expected);
  }

  @Test
  public void shouldTransformWsPathToAbsoluteFsPath() throws Exception {
    String wsPath = "a/b/c";

    Path fsPath = rootAwarePathTransformer.transform(wsPath);

    assertFalse(wsPath.startsWith(WsPathUtils.ROOT));
    assertTrue(fsPath.isAbsolute());
  }

  @Test
  public void shouldTransformFsPathToAbsoluteWsPath() throws Exception {
    Path fsPath = Paths.get("a/b/c");

    String wsPath = rootAwarePathTransformer.transform(fsPath);

    assertFalse(fsPath.isAbsolute());
    assertTrue(wsPath.startsWith(WsPathUtils.ROOT));
  }
}
