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
package org.eclipse.che.selenium.core.utils;

import java.lang.reflect.Field;
import org.eclipse.che.selenium.core.constant.Infrastructure;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WorkspaceDtoDeserializerTest {

  private WorkspaceDtoDeserializer deserializer;

  @BeforeMethod
  public void setUp() throws Exception {
    deserializer = new WorkspaceDtoDeserializer();
    Field f1 = WorkspaceDtoDeserializer.class.getDeclaredField("infrastructure");
    f1.setAccessible(true);
    f1.set(deserializer, Infrastructure.OPENSHIFT);
  }

  @Test
  public void shouldBeAbleToGetWorkspaceConfigFromResource() {

    Assert.assertNotNull(deserializer.deserializeWorkspaceTemplate("default.json"));
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp =
          "resource /templates/workspace/openshift/some.json relative to org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer not found.")
  public void shouldFailIfResourceIsNotFound() {
    deserializer.deserializeWorkspaceTemplate("some.json");
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp =
          "com.google.gson.stream.MalformedJsonException: Expected ':' at line 3 column 8 path \\$\\.werwerw")
  public void shouldFailIfNotAJson() {
    deserializer.deserializeWorkspaceTemplate("notAJson.json");
  }
}
