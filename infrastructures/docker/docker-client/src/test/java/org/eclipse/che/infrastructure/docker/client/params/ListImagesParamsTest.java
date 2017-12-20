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
