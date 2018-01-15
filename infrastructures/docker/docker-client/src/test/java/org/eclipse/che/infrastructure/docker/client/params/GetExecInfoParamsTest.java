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
