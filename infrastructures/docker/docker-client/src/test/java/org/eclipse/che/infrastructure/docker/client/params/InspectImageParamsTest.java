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

import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class InspectImageParamsTest {

  private static final String IMAGE = "image";

  private InspectImageParams inspectImageParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    inspectImageParams = InspectImageParams.create(IMAGE);

    assertEquals(inspectImageParams.getImage(), IMAGE);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterIsNull() {
    inspectImageParams = InspectImageParams.create(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfImageRequiredParameterResetWithNull() {
    inspectImageParams = InspectImageParams.create(IMAGE).withImage(null);
  }
}
