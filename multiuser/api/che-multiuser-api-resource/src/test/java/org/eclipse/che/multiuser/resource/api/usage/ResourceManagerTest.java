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
package org.eclipse.che.multiuser.resource.api.usage;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.resource.api.AvailableResourcesProvider;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.ResourceUsageTracker;
import org.eclipse.che.multiuser.resource.api.ResourcesProvider;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.model.ResourcesDetails;
import org.eclipse.che.multiuser.resource.spi.impl.ProvidedResourcesImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link ResourceManager}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class ResourceManagerTest {

  private static final String ACCOUNT_ID = "testOrg359";

  @Mock private ResourceAggregator resourceAggregator;
  @Mock private ResourcesProvider resourcesProvider;
  @Mock private ResourceUsageTracker usageTrackers;
  @Mock private AccountManager accountManager;
  @Mock private AvailableResourcesProvider accountTypeToAvailableResourcesProvider;
  @Mock private DefaultAvailableResourcesProvider defaultAvailableResourcesProvider;
  @Mock private ProvidedResources providedResources;
  @Mock private Account account;

  private ResourceManager resourceManager;

  @BeforeMethod
  public void setup() throws Exception {
    resourceManager =
        new ResourceManager(
            resourceAggregator,
            Collections.singleton(resourcesProvider),
            Collections.singleton(usageTrackers),
            accountManager,
            Collections.singletonMap("organizational", accountTypeToAvailableResourcesProvider),
            defaultAvailableResourcesProvider);

    when(resourcesProvider.getResources(ACCOUNT_ID)).thenReturn(singletonList(providedResources));
    when(resourceAggregator.aggregateByType(anyList()))
        .then(
            (Answer<Map<String, Resource>>)
                invocationOnMock -> {
                  final List<Resource> argument = invocationOnMock.getArgument(0);
                  return argument.stream().collect(toMap(Resource::getType, identity()));
                });
    when(account.getId()).thenReturn(ACCOUNT_ID);
    when(account.getType()).thenReturn("organizational");
    when(accountManager.getById(ACCOUNT_ID)).thenReturn(account);
  }

  @Test
  public void testGetsAccountTotalResources() throws Exception {
    final List<Resource> res =
        ImmutableList.of(
            new ResourceImpl("RAM", 2048, "mb"), new ResourceImpl("timeout", 15, "minutes"));
    doReturn(res).when(providedResources).getResources();

    List<? extends Resource> actual = resourceManager.getTotalResources(ACCOUNT_ID);

    assertEquals(actual.size(), res.size());
    assertTrue(actual.containsAll(res));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testThrowsNotFoundExceptionWhenResourceForGivenAccountNotFound() throws Exception {
    doThrow(NotFoundException.class).when(resourcesProvider).getResources(ACCOUNT_ID);

    resourceManager.getTotalResources(ACCOUNT_ID);
  }

  @Test(expectedExceptions = ServerException.class)
  public void doThrowServerExceptionWhenErrorOccursWhileFetchingResources() throws Exception {
    doThrow(ServerException.class).when(resourcesProvider).getResources(ACCOUNT_ID);

    resourceManager.getTotalResources(ACCOUNT_ID);
  }

  @Test
  public void testGetsAvailableResources() throws Exception {
    final List<ResourceImpl> availableResources =
        ImmutableList.of(new ResourceImpl("RAM", 2048, "mb"));
    doReturn(availableResources)
        .when(accountTypeToAvailableResourcesProvider)
        .getAvailableResources(ACCOUNT_ID);

    List<? extends Resource> actual = resourceManager.getAvailableResources(ACCOUNT_ID);

    assertEquals(actual.size(), availableResources.size());
    assertTrue(actual.containsAll(availableResources));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testThrowsNotFoundExceptionWhenAccountWithGivenIdNotFound() throws Exception {
    doThrow(NotFoundException.class).when(accountManager).getById(ACCOUNT_ID);

    resourceManager.getAvailableResources(ACCOUNT_ID);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testThrowsNotFoundExceptionWhenAvailableResourcesForGivenAccountNotFound()
      throws Exception {
    doThrow(NotFoundException.class)
        .when(accountTypeToAvailableResourcesProvider)
        .getAvailableResources(ACCOUNT_ID);

    resourceManager.getAvailableResources(ACCOUNT_ID);
  }

  @Test(expectedExceptions = ServerException.class)
  public void testThrowsServerExceptionWhenErrorOccursWhileGettingAvailableResources()
      throws Exception {
    doThrow(ServerException.class).when(accountManager).getById(ACCOUNT_ID);

    resourceManager.getAvailableResources(ACCOUNT_ID);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Account with specified id was not found"
  )
  public void testThrowsNotFoundExceptionWhenAccountWithGivenIdWasNotFound() throws Exception {
    when(resourcesProvider.getResources(eq(ACCOUNT_ID)))
        .thenThrow(new NotFoundException("Account with specified id was not found"));

    resourceManager.getResourceDetails(ACCOUNT_ID);
  }

  @Test
  public void testReturnsResourceDetailsForGivenAccount() throws Exception {
    final ResourceImpl testResource = new ResourceImpl("RAM", 1000, "mb");
    final ResourceImpl reducedResource = new ResourceImpl("timeout", 2000, "m");
    final ProvidedResourcesImpl providedResource =
        new ProvidedResourcesImpl(
            "test", null, ACCOUNT_ID, 123L, 321L, singletonList(testResource));

    when(resourcesProvider.getResources(eq(ACCOUNT_ID)))
        .thenReturn(singletonList(providedResource));
    when(resourceAggregator.aggregateByType(any()))
        .thenReturn(ImmutableMap.of(reducedResource.getType(), reducedResource));

    final ResourcesDetails resourcesDetails = resourceManager.getResourceDetails(ACCOUNT_ID);

    verify(resourcesProvider).getResources(eq(ACCOUNT_ID));
    verify(resourceAggregator).aggregateByType(eq(singletonList(testResource)));

    assertEquals(resourcesDetails.getAccountId(), ACCOUNT_ID);
    assertEquals(resourcesDetails.getProvidedResources().size(), 1);
    assertEquals(resourcesDetails.getProvidedResources().get(0), providedResource);

    assertEquals(resourcesDetails.getTotalResources().size(), 1);
    assertEquals(resourcesDetails.getTotalResources().get(0), reducedResource);
  }
}
