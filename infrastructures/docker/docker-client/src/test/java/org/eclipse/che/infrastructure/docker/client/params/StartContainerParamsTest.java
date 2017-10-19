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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class StartContainerParamsTest {

  private static final String CONTAINER = "container";

  private StartContainerParams startContainerParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    startContainerParams = StartContainerParams.create(CONTAINER);

    assertEquals(startContainerParams.getContainer(), CONTAINER);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    startContainerParams = StartContainerParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    startContainerParams = StartContainerParams.create(CONTAINER).withContainer(null);
  }
}
