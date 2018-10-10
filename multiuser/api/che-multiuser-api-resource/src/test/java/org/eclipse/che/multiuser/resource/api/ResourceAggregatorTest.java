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
package org.eclipse.che.multiuser.resource.api;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.type.ResourceType;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.ResourceAggregator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourceAggregatorTest {
  private static final String A_RESOURCE_TYPE = "resourceA";
  private static final String B_RESOURCE_TYPE = "resourceB";
  private static final String C_RESOURCE_TYPE = "resourceC";

  @Mock private ResourceType aResourceType;
  @Mock private ResourceType bResourceType;
  @Mock private ResourceType cResourceType;

  private ResourceAggregator resourceAggregator;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(aResourceType.getId()).thenReturn(A_RESOURCE_TYPE);
    lenient().when(bResourceType.getId()).thenReturn(B_RESOURCE_TYPE);
    lenient().when(cResourceType.getId()).thenReturn(C_RESOURCE_TYPE);

    resourceAggregator =
        new ResourceAggregator(ImmutableSet.of(aResourceType, bResourceType, cResourceType));
  }

  @Test
  public void shouldTestResourcesAggregationByTypes() throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl anotherBResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");
    final ResourceImpl aggregatedBResources = new ResourceImpl(B_RESOURCE_TYPE, 444, "unit");
    when(bResourceType.aggregate(any(), any())).thenReturn(aggregatedBResources);

    // when
    final Map<String, Resource> aggregatedResources =
        resourceAggregator.aggregateByType(asList(aResource, bResource, anotherBResource));

    // then
    verify(bResourceType).aggregate(eq(bResource), eq(anotherBResource));
    verify(aResourceType, never()).aggregate(any(), any());

    assertEquals(aggregatedResources.size(), 2);

    assertTrue(aggregatedResources.containsKey(A_RESOURCE_TYPE));
    assertTrue(aggregatedResources.containsValue(aResource));

    assertTrue(aggregatedResources.containsKey(B_RESOURCE_TYPE));
    assertTrue(aggregatedResources.containsValue(aggregatedBResources));
  }

  @Test
  public void shouldTestResourcesDeduction() throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl anotherBResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");
    final ResourceImpl aggregatedBResources = new ResourceImpl(A_RESOURCE_TYPE, 444, "unit");
    when(bResourceType.deduct(any(), any())).thenReturn(aggregatedBResources);

    // when
    final List<? extends Resource> deductedResources =
        resourceAggregator.deduct(asList(aResource, bResource), singletonList(anotherBResource));

    // then
    verify(bResourceType).deduct(eq(bResource), eq(anotherBResource));
    verify(aResourceType, never()).deduct(any(), any());

    assertEquals(deductedResources.size(), 2);
    assertTrue(deductedResources.contains(aResource));
    assertTrue(deductedResources.contains(aggregatedBResources));
  }

  @Test(expectedExceptions = NoEnoughResourcesException.class)
  public void shouldThrowConflictExceptionWhenTotalResourcesDoNotHaveEnoughAmountToDeduct()
      throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 111, "unit");
    final ResourceImpl anotherAResource = new ResourceImpl(A_RESOURCE_TYPE, 333, "unit");
    when(aResourceType.deduct(any(), any()))
        .thenThrow(
            new NoEnoughResourcesException(
                singletonList(aResource),
                singletonList(anotherAResource),
                singletonList(new ResourceImpl(A_RESOURCE_TYPE, 222, "unit"))));

    // when
    resourceAggregator.deduct(singletonList(aResource), singletonList(anotherAResource));
  }

  @Test(expectedExceptions = NoEnoughResourcesException.class)
  public void shouldThrowConflictExceptionWhenTotalResourcesDoNotContainsRequiredResourcesAtAll()
      throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");

    // when
    resourceAggregator.deduct(singletonList(aResource), singletonList(bResource));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenTryingToAggregateNotSupportedResource()
      throws Exception {
    // given
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.aggregateByType(singletonList(dResource));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenTryingToAggregateNotSupportedResources()
      throws Exception {
    // given
    final ResourceImpl dResource = mock(ResourceImpl.class);
    final ResourceImpl anotherDResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");
    when(anotherDResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.aggregateByType(asList(dResource, anotherDResource));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenTotalResourcesListContainsNotSupportedResourceOnResourcesDeduction()
          throws Exception {
    // given
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.deduct(emptyList(), singletonList(dResource));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenResourcesToDeductListContainsNotSupportedResourceOnResourcesDeduction()
          throws Exception {
    // given
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.deduct(singletonList(dResource), emptyList());
  }

  @Test
  public void shouldReturnResourceAsExcessiveWhenDeductedAmountGreaterThan0() throws Exception {
    // given
    final ResourceImpl sourceAResource = new ResourceImpl(A_RESOURCE_TYPE, 5, "unit");
    final ResourceImpl toCompareAResource = new ResourceImpl(A_RESOURCE_TYPE, 3, "unit");
    final ResourceImpl excessiveAResource = new ResourceImpl(A_RESOURCE_TYPE, 2, "unit");
    when(aResourceType.deduct(any(), any())).thenReturn(excessiveAResource);

    // when
    List<? extends Resource> excess =
        resourceAggregator.excess(
            singletonList(sourceAResource), singletonList(toCompareAResource));

    // then
    assertEquals(excess.size(), 1);
    assertTrue(excess.contains(excessiveAResource));
    verify(aResourceType).deduct(sourceAResource, toCompareAResource);
  }

  @Test
  public void
      shouldReturnResourceAsExcessiveWhenToSourceListContainsResourceButToCompareListDoesNotContainItAtAll()
          throws Exception {
    // given
    final ResourceImpl sourceAResource = new ResourceImpl(A_RESOURCE_TYPE, 5, "unit");

    // when
    List<? extends Resource> excess =
        resourceAggregator.excess(singletonList(sourceAResource), emptyList());

    // then
    assertEquals(excess.size(), 1);
    assertTrue(excess.contains(sourceAResource));
  }

  @Test
  public void
      shouldNotReturnResourceAsExcessiveWhenToCompareListContainsResourceButSourceDoesNotContainItAtAll()
          throws Exception {
    // given
    final ResourceImpl toCompareAResource = new ResourceImpl(A_RESOURCE_TYPE, 5, "unit");

    // when
    List<? extends Resource> excess =
        resourceAggregator.excess(emptyList(), singletonList(toCompareAResource));

    // then
    assertTrue(excess.isEmpty());
  }

  @Test
  public void shouldNotReturnResourceAsExcessiveWhenResourcesHaveTheSameAmount() throws Exception {
    // given
    final ResourceImpl sourceAResource = new ResourceImpl(A_RESOURCE_TYPE, 5, "unit");
    final ResourceImpl toCompareAResource = new ResourceImpl(A_RESOURCE_TYPE, 5, "unit");

    // when
    List<? extends Resource> excess =
        resourceAggregator.excess(
            singletonList(sourceAResource), singletonList(toCompareAResource));

    // then
    assertTrue(excess.isEmpty());
    verify(aResourceType, never()).deduct(any(), any());
  }

  @Test
  public void shouldNotReturnResourceAsExcessiveWhenToCompareResourceIsGreaterThanSource()
      throws Exception {
    // given
    final ResourceImpl sourceAResource = new ResourceImpl(A_RESOURCE_TYPE, 5, "unit");
    final ResourceImpl toCompareAResource = new ResourceImpl(A_RESOURCE_TYPE, 10, "unit");
    doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), emptyList()))
        .when(aResourceType)
        .deduct(any(), any());

    // when
    List<? extends Resource> excess =
        resourceAggregator.excess(
            singletonList(sourceAResource), singletonList(toCompareAResource));

    // then
    assertTrue(excess.isEmpty());
    verify(aResourceType).deduct(sourceAResource, toCompareAResource);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenFirstListContainsResourceWithUnsupportedTypeOnExcessCalculation()
          throws Exception {
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.excess(singletonList(dResource), emptyList());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenSecondListContainsResourceWithUnsupportedTypeExcessCalculation()
          throws Exception {
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.excess(emptyList(), singletonList(dResource));
  }

  @Test
  public void shouldReturnIntersectionOfTwoResourcesLists() throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 123, "unit");
    final ResourceImpl anotherBResource = new ResourceImpl(B_RESOURCE_TYPE, 321, "unit");
    final ResourceImpl cResource = new ResourceImpl(C_RESOURCE_TYPE, 321, "unit");

    // when
    List<? extends Resource> intersection =
        resourceAggregator.intersection(
            asList(aResource, bResource), asList(anotherBResource, cResource));

    // then
    assertEquals(intersection.size(), 2);
    assertTrue(intersection.contains(bResource));
    assertTrue(intersection.contains(anotherBResource));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenFirstListContainsResourceWithUnsupportedTypeOnIntersection()
          throws Exception {
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.intersection(singletonList(dResource), emptyList());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenSecondListContainsResourceWithUnsupportedTypeOnIntersection()
          throws Exception {
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.intersection(emptyList(), singletonList(dResource));
  }

  @Test
  public void shouldReturnMinResources() throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, 100, "unit");
    final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, 1000, "unit");
    final ResourceImpl minBResource = new ResourceImpl(B_RESOURCE_TYPE, 500, "unit");
    final ResourceImpl anotherBResource = new ResourceImpl(B_RESOURCE_TYPE, 2000, "unit");

    // when
    List<? extends Resource> min =
        resourceAggregator.min(asList(aResource, bResource, minBResource, anotherBResource));

    // then
    assertEquals(min.size(), 2);
    assertTrue(min.contains(aResource));
    assertTrue(min.contains(minBResource));
  }

  @Test
  public void shouldReturnMinResourcesWhenTheyContainsMinusOneValue() throws Exception {
    // given
    final ResourceImpl aResource = new ResourceImpl(A_RESOURCE_TYPE, -1, "unit");
    final ResourceImpl bResource = new ResourceImpl(B_RESOURCE_TYPE, -1, "unit");
    final ResourceImpl minAResource = new ResourceImpl(A_RESOURCE_TYPE, 250, "unit");
    final ResourceImpl minBResource = new ResourceImpl(B_RESOURCE_TYPE, 500, "unit");

    // when
    List<? extends Resource> min =
        resourceAggregator.min(asList(aResource, minAResource, minBResource, bResource));

    // then
    assertEquals(min.size(), 2);
    assertTrue(min.contains(minAResource));
    assertTrue(min.contains(minBResource));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void
      shouldThrowIllegalArgumentExceptionWhenListContainsResourceWithUnsupportedTypeOnMinFinding()
          throws Exception {
    final ResourceImpl dResource = mock(ResourceImpl.class);
    when(dResource.getType()).thenReturn("resourceD");

    // when
    resourceAggregator.min(singletonList(dResource));
  }
}
