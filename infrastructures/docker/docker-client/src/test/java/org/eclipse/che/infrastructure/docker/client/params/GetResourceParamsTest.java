/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.params;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class GetResourceParamsTest {

  private static final String CONTAINER = "container";
  private static final String SOURCE_PATH = "/home/user/path/abc.tar";

  private GetResourceParams getResourceParams;

  @BeforeMethod
  private void prepare() {
    getResourceParams = GetResourceParams.create(CONTAINER, SOURCE_PATH);
  }

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    getResourceParams = GetResourceParams.create(CONTAINER, SOURCE_PATH);

    assertEquals(getResourceParams.getContainer(), CONTAINER);
    assertEquals(getResourceParams.getSourcePath(), SOURCE_PATH);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    getResourceParams = GetResourceParams.create(null, SOURCE_PATH);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfSourcePathRequiredParameterIsNull() {
    getResourceParams = GetResourceParams.create(CONTAINER, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    getResourceParams.withContainer(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfSourcePathRequiredParameterResetWithNull() {
    getResourceParams.withSourcePath(null);
  }
}
