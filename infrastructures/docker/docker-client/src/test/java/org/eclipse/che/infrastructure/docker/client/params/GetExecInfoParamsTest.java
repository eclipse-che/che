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
public class GetExecInfoParamsTest {

  private static final String EXEC_ID = "exec_id";

  private GetExecInfoParams getExecInfoParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    getExecInfoParams = GetExecInfoParams.create(EXEC_ID);

    assertEquals(getExecInfoParams.getExecId(), EXEC_ID);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfExecIdRequiredParameterIsNull() {
    getExecInfoParams = GetExecInfoParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfExecIdRequiredParameterResetWithNull() {
    getExecInfoParams = GetExecInfoParams.create(EXEC_ID).withExecId(null);
  }
}
