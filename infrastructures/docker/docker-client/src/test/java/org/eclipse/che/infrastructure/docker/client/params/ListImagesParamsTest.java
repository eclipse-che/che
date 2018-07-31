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
import static org.testng.Assert.assertTrue;

import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
public class ListImagesParamsTest {

  private static final boolean WITH_ALL = true;
  private static final boolean WITH_DIGEST = true;
  private static final Filters FILTERS = new Filters().withFilter("reference", "imageName");

  private ListImagesParams listImagesParams;

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    listImagesParams =
        ListImagesParams.create().withAll(WITH_ALL).withDigestst(WITH_DIGEST).withFilters(FILTERS);

    assertTrue(listImagesParams.getAll() == WITH_ALL);
    assertTrue(listImagesParams.getDigests() == WITH_DIGEST);
    assertEquals(listImagesParams.getFilters(), FILTERS);
  }
}
