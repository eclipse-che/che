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
package org.eclipse.che.infrastructure.docker.client.params;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class RemoveImageParamsTest {

  private static final String IMAGE = "image";
  private static final Boolean FORCE = Boolean.FALSE;

  private RemoveImageParams removeImageParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    removeImageParams = RemoveImageParams.create(IMAGE);

    assertEquals(removeImageParams.getImage(), IMAGE);

    assertNull(removeImageParams.isForce());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    removeImageParams = RemoveImageParams.create(IMAGE).withForce(FORCE);

    assertEquals(removeImageParams.getImage(), IMAGE);
    assertEquals(removeImageParams.isForce(), FORCE);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
    removeImageParams = RemoveImageParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
    removeImageParams = RemoveImageParams.create(IMAGE).withImage(null);
  }
}
