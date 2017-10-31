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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class AttachContainerParamsTest {

  private static final String CONTAINER = "container";
  private static final boolean STREAM = true;

  private AttachContainerParams attachContainerParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    attachContainerParams = AttachContainerParams.create(CONTAINER);

    assertEquals(attachContainerParams.getContainer(), CONTAINER);

    assertNull(attachContainerParams.isStream());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    attachContainerParams = AttachContainerParams.create(CONTAINER).withStream(STREAM);

    assertEquals(attachContainerParams.getContainer(), CONTAINER);
    assertTrue(attachContainerParams.isStream() == STREAM);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfContainerRequiredParameterIsNull() {
    attachContainerParams = AttachContainerParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfRepositoryRequiredParameterResetWithNull() {
    attachContainerParams = AttachContainerParams.create(CONTAINER).withContainer(null);
  }

  @Test
  public void streamParameterShouldEqualsNullIfItNotSet() {
    attachContainerParams = AttachContainerParams.create(CONTAINER);

    assertNull(attachContainerParams.isStream());
  }
}
