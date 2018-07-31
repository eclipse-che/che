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
public class CreateExecParamsTest {

  private static final String CONTAINER = "container";
  private static final boolean DETACH = false;
  private static final String[] CMD = {"command", "arg1", "arg2"};
  private static final String USER = "user:user";

  private CreateExecParams createExecParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    createExecParams = CreateExecParams.create(CONTAINER, CMD);

    assertEquals(createExecParams.getContainer(), CONTAINER);
    assertEquals(createExecParams.getCmd(), CMD);

    assertNull(createExecParams.isDetach());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    createExecParams = CreateExecParams.create(CONTAINER, CMD).withDetach(DETACH).withUser(USER);

    assertEquals(createExecParams.getContainer(), CONTAINER);
    assertEquals(createExecParams.getCmd(), CMD);
    assertTrue(createExecParams.isDetach() == DETACH);
    assertEquals(createExecParams.getUser(), USER);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    createExecParams = CreateExecParams.create(null, CMD);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowAnNullPointerExceptionIfCmdRequiredParameterIsNull() {
    createExecParams = CreateExecParams.create(CONTAINER, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionIfSetEmptyArray() {
    createExecParams = CreateExecParams.create(CONTAINER, CMD);

    String[] cmd = new String[0];
    createExecParams.withCmd(cmd);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionIfSetEmptyCommand() {
    createExecParams = CreateExecParams.create(CONTAINER, CMD);

    String[] cmd = {"", "arg"};
    createExecParams.withCmd(cmd);
  }

  @Test
  public void detachParameterShouldEqualsNullIfItNotSet() {
    createExecParams = CreateExecParams.create(CONTAINER, CMD);

    assertNull(createExecParams.isDetach());
  }
}
