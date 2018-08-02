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
package org.eclipse.che.commons.lang.os;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class WindowsPathEscaperTest {
  /**
   * E.g from
   * https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
   *
   * <p>Users should be /Users /Users should be /Users c/Users should be /c/Users /c/Users should be
   * /c/Users c:/Users should be /c/Users
   */
  @Test(dataProvider = "pathProvider")
  public void shouldStaticallyEscapePathForWindowsHost(String windowsPath, String expectedPath) {
    assertEquals(WindowsPathEscaper.escapePathStatic(windowsPath), expectedPath);
  }

  /**
   * E.g from
   * https://github.com/boot2docker/boot2docker/blob/master/README.md#virtualbox-guest-additions
   *
   * <p>Users should be /Users /Users should be /Users c/Users should be /c/Users /c/Users should be
   * /c/Users c:/Users should be /c/Users
   */
  @Test(dataProvider = "pathProvider")
  public void shouldNonStaticallyEscapePathForWindowsHost(String windowsPath, String expectedPath) {
    WindowsPathEscaper windowsPathEscaper = new WindowsPathEscaper();
    assertEquals(windowsPathEscaper.escapePath(windowsPath), expectedPath);
  }

  @DataProvider(name = "pathProvider")
  public static Object[][] pathProvider() {
    return new Object[][] {
      {"Users", "/Users"},
      {"/Users", "/Users"},
      {"c/Users", "/c/Users"},
      {"/c/Users", "/c/Users"},
      {"c:/Users", "/c/Users"},
      {"C:/Users", "/c/Users"},
      {
        "C:/Users/path/dir/from/host:/name/of/dir/in/container",
        "/c/Users/path/dir/from/host:/name/of/dir/in/container"
      }
    };
  }
}
