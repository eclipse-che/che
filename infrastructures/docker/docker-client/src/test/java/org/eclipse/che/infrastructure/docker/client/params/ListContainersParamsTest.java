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

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.infrastructure.docker.client.json.Filters;
import org.testng.annotations.Test;

/**
 * Test for {@link ListContainersParams}
 *
 * @author Alexander Andrienko
 */
public class ListContainersParamsTest {

  private static final String TEXT = "to be or not be";
  private static final Integer LIMIT = 100;
  private static final Filters filters = new Filters().withFilter(TEXT, TEXT);

  private ListContainersParams listContainersParams;

  @Test
  public void shouldCreateParamsObjectWithRequiredParameters() {
    listContainersParams = ListContainersParams.create();

    assertNull(listContainersParams.isAll());
    assertNull(listContainersParams.isSize());
    assertNull(listContainersParams.getBefore());
    assertNull(listContainersParams.getSince());
    assertNull(listContainersParams.getLimit());
    assertNull(listContainersParams.getFilters());
  }

  @Test
  public void shouldCreateParamsObjectWithAllPossibleParameters() {
    listContainersParams =
        ListContainersParams.create()
            .withAll(true)
            .withSize(true)
            .withSince(TEXT)
            .withBefore(TEXT)
            .withFilters(filters)
            .withLimit(LIMIT);

    assertEquals(listContainersParams.isAll(), TRUE);
    assertEquals(listContainersParams.isSize(), TRUE);
    assertEquals(listContainersParams.getBefore(), TEXT);
    assertEquals(listContainersParams.getSince(), TEXT);
    assertEquals(listContainersParams.getLimit(), LIMIT);
    assertEquals(
        listContainersParams.getFilters().getFilters(), singletonMap(TEXT, singletonList(TEXT)));
  }

  @Test
  public void shouldCreateParamsObjectWithNullAllParameter() {
    listContainersParams =
        ListContainersParams.create()
            .withSize(true)
            .withSince(TEXT)
            .withBefore(TEXT)
            .withFilters(filters)
            .withLimit(LIMIT);
    assertNull(listContainersParams.isAll(), null);
  }

  @Test
  public void shouldCreateParamsObjectWithNullSizeParameter() {
    listContainersParams =
        ListContainersParams.create()
            .withAll(true)
            .withSince(TEXT)
            .withBefore(TEXT)
            .withFilters(filters)
            .withLimit(LIMIT);
    assertNull(listContainersParams.isSize(), null);
  }

  @Test
  public void shouldCreateParamsObjectWithNullSinceParameter() {
    listContainersParams =
        ListContainersParams.create()
            .withAll(true)
            .withSize(true)
            .withBefore(TEXT)
            .withFilters(filters)
            .withLimit(LIMIT);
    assertNull(listContainersParams.getSince(), null);
  }

  @Test
  public void shouldCreateParamsObjectWithNullBeforeParameter() {
    listContainersParams =
        ListContainersParams.create()
            .withAll(true)
            .withSize(true)
            .withSince(TEXT)
            .withFilters(filters)
            .withLimit(LIMIT);
    assertNull(listContainersParams.getBefore(), null);
  }

  @Test
  public void shouldCreateParamsObjectWithNullLimitParameter() {
    listContainersParams =
        ListContainersParams.create()
            .withAll(true)
            .withSize(true)
            .withSince(TEXT)
            .withBefore(TEXT)
            .withFilters(filters);
    assertNull(listContainersParams.getLimit(), null);
  }

  @Test
  public void shouldCreateParamsObjectWithNullFiltersParameter() {
    listContainersParams =
        ListContainersParams.create()
            .withAll(true)
            .withSize(true)
            .withSince(TEXT)
            .withBefore(TEXT)
            .withLimit(LIMIT);
    assertNull(listContainersParams.getFilters(), null);
  }
}
