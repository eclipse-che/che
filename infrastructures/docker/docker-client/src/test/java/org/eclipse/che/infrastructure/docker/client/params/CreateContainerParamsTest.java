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

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.infrastructure.docker.client.json.ContainerConfig;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class CreateContainerParamsTest {

  private static final ContainerConfig CONTAINER_CONFIG = mock(ContainerConfig.class);
  private static final String CONTAINER_NAME = "container";

  private CreateContainerParams createContainerParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    createContainerParams = CreateContainerParams.create(CONTAINER_CONFIG);

    assertEquals(createContainerParams.getContainerConfig(), CONTAINER_CONFIG);

    assertNull(createContainerParams.getContainerName());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    createContainerParams =
        CreateContainerParams.create(CONTAINER_CONFIG).withContainerName(CONTAINER_NAME);

    assertEquals(createContainerParams.getContainerConfig(), CONTAINER_CONFIG);
    assertEquals(createContainerParams.getContainerName(), CONTAINER_NAME);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerConfigRequiredParameterIsNull() {
    createContainerParams = CreateContainerParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterResetWithNull() {
    createContainerParams =
        CreateContainerParams.create(CONTAINER_CONFIG).withContainerConfig(null);
  }

  @Test
  public void containerNameParameterShouldEqualsNullIfItNotSet() {
    createContainerParams = CreateContainerParams.create(CONTAINER_CONFIG);

    assertNull(createContainerParams.getContainerName());
  }
}
