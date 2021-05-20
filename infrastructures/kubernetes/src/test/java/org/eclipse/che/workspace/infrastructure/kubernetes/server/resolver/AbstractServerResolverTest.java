/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.resolver;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.commons.annotation.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AbstractServerResolverTest {

  @Test(dataProvider = "buildPathFragments")
  public void testBuildPath(String fragment1, @Nullable String fragment2, String expectedResult) {
    assertEquals(AbstractServerResolver.buildPath(fragment1, fragment2), expectedResult);
  }

  @DataProvider
  public static Object[][] buildPathFragments() {
    return new Object[][] {
      new Object[] {"/", null, "/"},
      new Object[] {"/a", null, "/a/"},
      new Object[] {"/a/", null, "/a/"},
      new Object[] {"/a", "", "/a/"},
      new Object[] {"/a", "", "/a/"},
      new Object[] {"/a", "/", "/a/"},
      new Object[] {"/a", "b", "/a/b/"},
      new Object[] {"/a", "/b", "/a/b/"},
      new Object[] {"/a", "b/", "/a/b/"},
      new Object[] {"/a", "/b/", "/a/b/"},
    };
  }
}
