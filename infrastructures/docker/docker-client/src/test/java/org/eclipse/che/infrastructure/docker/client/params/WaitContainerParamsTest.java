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
package org.eclipse.che.infrastructure.docker.client.params;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class WaitContainerParamsTest {

  private static final String CONTAINER = "container";

  private WaitContainerParams waitContainerParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    waitContainerParams = WaitContainerParams.create(CONTAINER);

    assertEquals(waitContainerParams.getContainer(), CONTAINER);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    waitContainerParams = WaitContainerParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    waitContainerParams = WaitContainerParams.create(CONTAINER).withContainer(null);
  }
}
