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
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class TopParamsTest {

  private static final String CONTAINER = "container";
  private static final String[] PS_ARGS = {"arg1", "arg2"};

  private TopParams topParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    topParams = TopParams.create(CONTAINER);

    assertEquals(topParams.getContainer(), CONTAINER);

    assertNull(topParams.getPsArgs());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    topParams = TopParams.create(CONTAINER).withPsArgs(PS_ARGS);

    assertEquals(topParams.getContainer(), CONTAINER);
    assertEquals(topParams.getPsArgs(), PS_ARGS);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    topParams = TopParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    topParams = TopParams.create(CONTAINER).withContainer(null);
  }
}
