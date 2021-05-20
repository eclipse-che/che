/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.type;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.type.AbstractExhaustibleResource}
 *
 * @author Sergii Leschenko
 */
public class AbstractExhaustibleResourceTest {
  private AbstractExhaustibleResource resourceType;

  @BeforeMethod
  public void setUp() throws Exception {
    resourceType = new TestResourceType();
  }

  @Test
  public void shouldFindSumResourcesAmountsOnResourcesAggregation() throws Exception {
    final Resource aggregate =
        resourceType.aggregate(
            new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
            new ResourceImpl(TestResourceType.ID, 500, TestResourceType.UNIT));

    assertEquals(aggregate.getType(), TestResourceType.ID);
    assertEquals(aggregate.getAmount(), 1500);
    assertEquals(aggregate.getUnit(), TestResourceType.UNIT);
  }

  @Test
  public void
      shouldReturnResourceWithMinusOneAmountWhenFirstResourceHasMinusOneAmountOnResourcesDeduction()
          throws Exception {
    final Resource aggregate =
        resourceType.aggregate(
            new ResourceImpl(TestResourceType.ID, -1, TestResourceType.UNIT),
            new ResourceImpl(TestResourceType.ID, 500, TestResourceType.UNIT));

    assertEquals(aggregate.getType(), TestResourceType.ID);
    assertEquals(aggregate.getAmount(), -1);
    assertEquals(aggregate.getUnit(), TestResourceType.UNIT);
  }

  @Test
  public void
      shouldReturnResourceWithMinusOneAmountWhenSecondResourceHasMinusOneAmountOnResourcesDeduction()
          throws Exception {
    final Resource aggregate =
        resourceType.aggregate(
            new ResourceImpl(TestResourceType.ID, 2000, TestResourceType.UNIT),
            new ResourceImpl(TestResourceType.ID, -1, TestResourceType.UNIT));

    assertEquals(aggregate.getType(), TestResourceType.ID);
    assertEquals(aggregate.getAmount(), -1);
    assertEquals(aggregate.getUnit(), TestResourceType.UNIT);
  }

  @Test
  public void shouldFindDifferenceResourcesAmountsOnResourcesDeduction() throws Exception {
    final Resource deducted =
        resourceType.deduct(
            new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
            new ResourceImpl(TestResourceType.ID, 500, TestResourceType.UNIT));

    assertEquals(deducted.getType(), TestResourceType.ID);
    assertEquals(deducted.getAmount(), 500);
    assertEquals(deducted.getUnit(), TestResourceType.UNIT);
  }

  @Test
  public void
      shouldReturnResourceWithMinusOneAmountWhenTotalResourceHasMinusOneOnResourcesDeduction()
          throws Exception {
    final Resource deducted =
        resourceType.deduct(
            new ResourceImpl(TestResourceType.ID, -1, TestResourceType.UNIT),
            new ResourceImpl(TestResourceType.ID, 500, TestResourceType.UNIT));

    assertEquals(deducted.getType(), TestResourceType.ID);
    assertEquals(deducted.getAmount(), -1);
    assertEquals(deducted.getUnit(), TestResourceType.UNIT);
  }

  @Test(expectedExceptions = NoEnoughResourcesException.class)
  public void
      shouldReturnResourceWithMinusOneAmountWhenDeductionResourceHasMinusOneOnResourcesDeduction()
          throws Exception {
    resourceType.deduct(
        new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
        new ResourceImpl(TestResourceType.ID, -1, TestResourceType.UNIT));
  }

  @Test
  public void
      shouldThrowConflictExceptionWhenDeductionAmountMoreThanTotalAmountOnResourcesDeduction()
          throws Exception {
    try {
      resourceType.deduct(
          new ResourceImpl(TestResourceType.ID, 300, TestResourceType.UNIT),
          new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT));
    } catch (NoEnoughResourcesException e) {
      assertEquals(
          e.getMissingResources(),
          Collections.singletonList(
              new ResourceImpl(TestResourceType.ID, 700, TestResourceType.UNIT)));
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "resources")
  public void
      shouldThrowIllegalArgumentExceptionWhenOneOfResourcesHasUnsupportedTypeOrUnitOnResourcesAggregation(
          ResourceImpl resourceA, ResourceImpl resourceB) {
    resourceType.aggregate(resourceA, resourceB);
  }

  @Test(expectedExceptions = IllegalArgumentException.class, dataProvider = "resources")
  public void
      shouldThrowIllegalArgumentExceptionWhenOneOfResourcesHasUnsupportedTypeOrUnitOnResourcesDeduction(
          ResourceImpl resourceA, ResourceImpl resourceB) {
    resourceType.aggregate(resourceA, resourceB);
  }

  @DataProvider(name = "resources")
  public Object[][] getResources() {
    return new Object[][] {
      {
        new ResourceImpl("unsupported", 1000, TestResourceType.UNIT),
        new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT)
      },
      {
        new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
        new ResourceImpl("unsupported", 1000, TestResourceType.UNIT)
      },
      {
        new ResourceImpl(TestResourceType.ID, 1000, "unsupported"),
        new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT)
      },
      {
        new ResourceImpl(TestResourceType.ID, 1000, TestResourceType.UNIT),
        new ResourceImpl(TestResourceType.ID, 1000, "unsupported")
      }
    };
  }

  private static class TestResourceType extends AbstractExhaustibleResource {
    private static final String ID = "testResource";
    private static final String UNIT = "testUnit";

    @Override
    public String getId() {
      return ID;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Set<String> getSupportedUnits() {
      return ImmutableSet.of(UNIT);
    }

    @Override
    public String getDefaultUnit() {
      return UNIT;
    }
  }
}
