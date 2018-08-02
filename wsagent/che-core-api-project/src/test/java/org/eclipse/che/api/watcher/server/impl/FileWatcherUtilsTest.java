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
package org.eclipse.che.api.watcher.server.impl;

import static java.util.Collections.singleton;
import static org.eclipse.che.api.watcher.server.impl.FileWatcherUtils.isExcluded;
import static org.eclipse.che.api.watcher.server.impl.FileWatcherUtils.toInternalPath;
import static org.eclipse.che.api.watcher.server.impl.FileWatcherUtils.toNormalPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for {@link FileWatcherUtils} */
public class FileWatcherUtilsTest {
  Path root;

  @Before
  public void setUp() throws Exception {
    root = Paths.get("/", "projects");
  }

  @Test
  public void shouldGetInternalPath() throws Exception {
    Path path = Paths.get("/", "projects", "che");
    String expected = Paths.get("/", "che").toString();

    String actual = toInternalPath(root, path);

    assertEquals(expected, actual);
  }

  @Test
  public void shouldGetNormalPath() throws Exception {
    String path = Paths.get("/", "che").toString();
    Path expected = Paths.get("/", "projects", "che");

    Path actual = toNormalPath(root, path);

    assertEquals(expected, actual);
  }

  @Test
  public void shouldBeExcluded() throws Exception {
    PathMatcher matcher = Mockito.mock(PathMatcher.class);
    Path path = Mockito.mock(Path.class);
    when(matcher.matches(path)).thenReturn(true);

    boolean condition = isExcluded(singleton(matcher), path);

    assertTrue(condition);
  }

  @Test
  public void shouldNotBeExcluded() throws Exception {
    PathMatcher matcher = Mockito.mock(PathMatcher.class);
    Path path = Mockito.mock(Path.class);
    when(matcher.matches(path)).thenReturn(false);

    boolean condition = isExcluded(singleton(matcher), path);

    assertFalse(condition);
  }
}
