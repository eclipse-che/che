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

import java.util.concurrent.TimeUnit;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class StopContainerParamsTest {

  private static final String CONTAINER = "container";
  private static final Long TIMEOUT = 2L;
  private static final TimeUnit TIMEUNIT = TimeUnit.MINUTES;

  private StopContainerParams stopContainerParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    stopContainerParams = StopContainerParams.create(CONTAINER);

    assertEquals(stopContainerParams.getContainer(), CONTAINER);

    assertNull(stopContainerParams.getTimeout());
    assertNull(stopContainerParams.getTimeunit());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    stopContainerParams = StopContainerParams.create(CONTAINER).withTimeout(TIMEOUT, TIMEUNIT);

    assertEquals(stopContainerParams.getContainer(), CONTAINER);
    assertEquals(stopContainerParams.getTimeout(), TIMEOUT);
    assertEquals(stopContainerParams.getTimeunit(), TIMEUNIT);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    stopContainerParams = StopContainerParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    stopContainerParams = StopContainerParams.create(CONTAINER).withContainer(null);
  }

  @Test
  public void timeoutParameterShouldEqualsNullIfItNotSet() {
    stopContainerParams = StopContainerParams.create(CONTAINER);

    assertNull(stopContainerParams.getTimeout());
  }

  @Test
  public void timeUnitParameterShouldBeInSecondsIfNotSet() {
    stopContainerParams = StopContainerParams.create(CONTAINER).withTimeout(TIMEOUT);

    assertEquals(stopContainerParams.getTimeunit(), TimeUnit.SECONDS);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfTimeUnitParameterIsNull() {
    stopContainerParams = StopContainerParams.create(CONTAINER).withTimeout(TIMEOUT, null);
  }
}
