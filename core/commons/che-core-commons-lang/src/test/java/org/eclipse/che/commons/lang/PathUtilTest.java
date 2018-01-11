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
package org.eclipse.che.commons.lang;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

/** Tests for {@link PathUtil} */
public class PathUtilTest {

  @Test
  public void testCanonicalPath() throws Exception {
    String path = PathUtil.toCanonicalPath("/foo/../bar", false);
    assertNotNull(path);
    assertFalse(path.isEmpty());
    assertEquals(path, "/bar");
  }

  @Test
  public void testRemoveLastSlash() throws Exception {
    String path = PathUtil.toCanonicalPath("/foo/bar/", true);
    assertNotNull(path);
    assertFalse(path.isEmpty());
    assertEquals(path, "/foo/bar");
  }

  @Test
  public void testEliminationDot() throws Exception {
    String path = PathUtil.toCanonicalPath("./bar", false);
    assertNotNull(path);
    assertFalse(path.isEmpty());
    assertEquals(path, "bar");
  }

  @Test
  public void testCanonicalPathWithFile() throws Exception {
    String path = PathUtil.toCanonicalPath("/foo/../bar/pom.xml", false);
    assertNotNull(path);
    assertFalse(path.isEmpty());
    assertEquals(path, "/bar/pom.xml");
  }
}
