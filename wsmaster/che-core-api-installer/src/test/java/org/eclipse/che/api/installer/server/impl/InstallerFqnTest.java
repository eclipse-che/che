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
package org.eclipse.che.api.installer.server.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKeyException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test for {@link InstallerFqn}.
 *
 * @author Sergii Leshchenko
 */
public class InstallerFqnTest {
  @Test
  public void testInstallerFqnWithIdAndVersion() throws Exception {
    InstallerFqn installerFqn = InstallerFqn.parse("id:1");

    assertEquals(installerFqn.getId(), "id");
    assertEquals(installerFqn.getVersion(), "1");
  }

  @Test
  public void testParseInstallerFqnWithId() throws Exception {
    InstallerFqn agentKey = InstallerFqn.parse("id");

    assertEquals(agentKey.getId(), "id");
    assertEquals(agentKey.getVersion(), "latest");
  }

  @Test(expectedExceptions = IllegalInstallerKeyException.class)
  public void testParseInstallerFqnFails() throws Exception {
    InstallerFqn.parse("id:1:2");
  }

  @Test(dataProvider = "inListData")
  public void shouldBeInList(String id, List<String> installerIds) throws Exception {
    assertTrue(InstallerFqn.idInKeyList(id, installerIds));
  }

  @Test(dataProvider = "notInListData")
  public void shouldNotBeInList(String id, List<String> installerIds) throws Exception {
    assertFalse(InstallerFqn.idInKeyList(id, installerIds));
  }

  @DataProvider(name = "inListData")
  public static Object[][] getInListData() {
    return new Object[][] {{"a", ImmutableList.of("a")}, {"a", ImmutableList.of("a:1.0.0")}};
  }

  @DataProvider(name = "notInListData")
  public static Object[][] getNotInListData() {
    return new Object[][] {{"a", ImmutableList.of("b")}, {"a:1.0.0", ImmutableList.of("a:1.0.1")}};
  }
}
