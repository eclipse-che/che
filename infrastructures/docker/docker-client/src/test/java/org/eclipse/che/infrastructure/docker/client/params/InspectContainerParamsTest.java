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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class InspectContainerParamsTest {

  private static final String CONTAINER = "container";
  private static final boolean RETURN_CONTAINER_SIZE = true;

  private InspectContainerParams inspectContainerParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    inspectContainerParams = InspectContainerParams.create(CONTAINER);

    assertEquals(inspectContainerParams.getContainer(), CONTAINER);

    assertNull(inspectContainerParams.isReturnContainerSize());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    inspectContainerParams =
        InspectContainerParams.create(CONTAINER).withReturnContainerSize(RETURN_CONTAINER_SIZE);

    assertEquals(inspectContainerParams.getContainer(), CONTAINER);
    assertTrue(inspectContainerParams.isReturnContainerSize() == RETURN_CONTAINER_SIZE);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    inspectContainerParams = InspectContainerParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    inspectContainerParams.withContainer(null);
  }
}
