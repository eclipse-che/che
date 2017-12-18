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
package org.eclipse.che.infrastructure.docker.client.params;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class PutResourceParamsTest {

  private static final String CONTAINER = "container";
  private static final String TARGET_PATH = "/home/user/path/target";
  private static final InputStream SOURCE_STREAM = mock(InputStream.class);
  private static final boolean NO_OVERWRITE_DIR_NON_DIR = true;

  private PutResourceParams putResourceParams;

  @BeforeMethod
  private void prepare() {
    putResourceParams = PutResourceParams.create(CONTAINER, TARGET_PATH);
  }

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    putResourceParams = PutResourceParams.create(CONTAINER, TARGET_PATH);

    assertEquals(putResourceParams.getContainer(), CONTAINER);
    assertEquals(putResourceParams.getTargetPath(), TARGET_PATH);

    assertNull(putResourceParams.getSourceStream());
    assertNull(putResourceParams.isNoOverwriteDirNonDir());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    putResourceParams =
        PutResourceParams.create(CONTAINER, TARGET_PATH)
            .withSourceStream(SOURCE_STREAM)
            .withNoOverwriteDirNonDir(NO_OVERWRITE_DIR_NON_DIR);

    assertEquals(putResourceParams.getContainer(), CONTAINER);
    assertEquals(putResourceParams.getTargetPath(), TARGET_PATH);
    assertEquals(putResourceParams.getSourceStream(), SOURCE_STREAM);
    assertTrue(putResourceParams.isNoOverwriteDirNonDir() == NO_OVERWRITE_DIR_NON_DIR);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    putResourceParams = PutResourceParams.create(null, TARGET_PATH);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfTargetPathRequiredParameterIsNull() {
    putResourceParams = PutResourceParams.create(CONTAINER, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    putResourceParams.withContainer(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfTargetPathRequiredParameterResetWithNull() {
    putResourceParams.withTargetPath(null);
  }

  @Test
  public void sourceStreamParameterShouldEqualsNullIfItNotSet() {
    putResourceParams.withNoOverwriteDirNonDir(NO_OVERWRITE_DIR_NON_DIR);

    assertNull(putResourceParams.getSourceStream());
  }

  @Test
  public void isNoOverwriteDirNonDirParameterShouldEqualsNullIfItNotSet() {
    putResourceParams.withSourceStream(SOURCE_STREAM);

    assertNull(putResourceParams.isNoOverwriteDirNonDir());
  }
}
