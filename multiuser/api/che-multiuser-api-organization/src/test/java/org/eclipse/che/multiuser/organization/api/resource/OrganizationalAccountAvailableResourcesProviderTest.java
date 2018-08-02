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
package org.eclipse.che.multiuser.organization.api.resource;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import javax.inject.Provider;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.usage.ResourceManager;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Test for {@link OrganizationalAccountAvailableResourcesProvider} */
@Listeners(MockitoTestNGListener.class)
public class OrganizationalAccountAvailableResourcesProviderTest {
  private static final String ROOT_ORG_NAME = "root";
  private static final String ROOT_ORG_ID = "organization123";
  private static final String SUBORG_ID = "organization321";
  private static final String SUBSUBORG_ID = "organization231";

  @Mock private Provider<ResourceManager> resourceManagerProvider;
  @Mock private ResourceManager resourceManager;
  @Mock private ResourceAggregator resourceAggregator;
  @Mock private OrganizationManager organizationManager;

  @InjectMocks @Spy
  private OrganizationalAccountAvailableResourcesProvider availableResourcesProvider;

  private Organization rootOrganization;
  private Organization suborganization;
  private Organization subsuborganization;

  @BeforeMethod
  public void setUp() throws Exception {
    when(resourceManagerProvider.get()).thenReturn(resourceManager);

    rootOrganization = new OrganizationImpl(ROOT_ORG_ID, ROOT_ORG_NAME, null);
    suborganization = new OrganizationImpl(SUBORG_ID, "root/suborg", ROOT_ORG_ID);
    subsuborganization = new OrganizationImpl(SUBSUBORG_ID, "root/suborg/subsuborg", SUBORG_ID);

    when(organizationManager.getById(ROOT_ORG_ID)).thenReturn(rootOrganization);
    when(organizationManager.getById(SUBORG_ID)).thenReturn(suborganization);
    when(organizationManager.getById(SUBSUBORG_ID)).thenReturn(subsuborganization);
  }

  @Test
  public void shouldReturnAvailableResourcesForRootOrganization() throws Exception {
    // given
    ResourceImpl availableResource = new ResourceImpl("test", 5000, "unit");
    doReturn(singletonList(availableResource))
        .when(availableResourcesProvider)
        .getAvailableOrganizationResources(any());

    // when
    List<? extends Resource> availableResources =
        availableResourcesProvider.getAvailableResources(ROOT_ORG_ID);

    // then
    assertEquals(availableResources.size(), 1);
    assertEquals(availableResources.get(0), availableResource);
    verify(availableResourcesProvider).getAvailableResources(ROOT_ORG_ID);
  }

  @Test
  public void shouldReturnAvailableResourcesForSuborganization() throws Exception {
    // given
    ResourceImpl parentAvailableResource = new ResourceImpl("test", 3000, "unit");
    prepareAvailableResource(ROOT_ORG_ID, parentAvailableResource);
    ResourceImpl suborgAvailableResource = new ResourceImpl("test", 5000, "unit");
    prepareAvailableResource(SUBORG_ID, suborgAvailableResource);
    doReturn(asList(parentAvailableResource, suborgAvailableResource))
        .when(resourceAggregator)
        .intersection(anyList(), anyList());
    doReturn(singletonList(parentAvailableResource)).when(resourceAggregator).min(anyList());

    // when
    List<? extends Resource> availableResources =
        availableResourcesProvider.getAvailableResources(SUBORG_ID);

    // then
    assertEquals(availableResources.size(), 1);
    assertEquals(availableResources.get(0), parentAvailableResource);
    verify(availableResourcesProvider).getAvailableOrganizationResources(rootOrganization);
    verify(availableResourcesProvider).getAvailableOrganizationResources(suborganization);
    verify(resourceAggregator)
        .intersection(
            singletonList(parentAvailableResource), singletonList(suborgAvailableResource));
    verify(resourceAggregator).min(asList(parentAvailableResource, suborgAvailableResource));
  }

  @Test
  public void shouldReturnAvailableResourcesAsTotalMinusUsedByItselfAndItsSuborganizations()
      throws Exception {
    // given
    ResourceImpl totalResource = new ResourceImpl("test", 9000, "unit");
    doReturn(singletonList(totalResource)).when(resourceManager).getTotalResources(anyString());

    ResourceImpl usedResource = new ResourceImpl("test", 3000, "unit");
    doReturn(singletonList(usedResource)).when(resourceManager).getUsedResources(anyString());

    ResourceImpl usedBySuborgResource = new ResourceImpl("test", 1500, "unit");
    ResourceImpl usedBySubsuborgResource = new ResourceImpl("test", 2000, "unit");
    doReturn(asList(usedBySuborgResource, usedBySubsuborgResource))
        .when(availableResourcesProvider)
        .getUsedResourcesBySuborganizations(anyString());

    ResourceImpl availableResource = new ResourceImpl("test", 2500, "unit");
    doReturn(singletonList(availableResource))
        .when(resourceAggregator)
        .deduct(anyList(), anyList());

    // when
    List<? extends Resource> availableResources =
        availableResourcesProvider.getAvailableOrganizationResources(rootOrganization);

    // then
    assertEquals(availableResources.size(), 1);
    assertEquals(availableResources.get(0), availableResource);
    verify(resourceManager).getTotalResources(ROOT_ORG_ID);
    verify(resourceManager).getUsedResources(ROOT_ORG_ID);
    verify(availableResourcesProvider).getUsedResourcesBySuborganizations(ROOT_ORG_NAME);
    verify(resourceAggregator)
        .deduct(
            singletonList(totalResource),
            asList(usedResource, usedBySuborgResource, usedBySubsuborgResource));
  }

  @Test
  public void shouldReturnExcessiveResourcesWhenUsedResourceAreGreaterThanTotal() throws Exception {
    // given
    ResourceImpl totalResource = new ResourceImpl("test", 9000, "unit");
    ResourceImpl excessiveTotalResource = new ResourceImpl("test1", 1000, "unit");
    doReturn(asList(totalResource, excessiveTotalResource))
        .when(resourceManager)
        .getTotalResources(anyString());

    ResourceImpl usedResource = new ResourceImpl("test", 10000, "unit");
    doReturn(singletonList(usedResource)).when(resourceManager).getUsedResources(anyString());

    doReturn(emptyList())
        .when(availableResourcesProvider)
        .getUsedResourcesBySuborganizations(anyString());

    doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), emptyList()))
        .when(resourceAggregator)
        .deduct(anyList(), anyList());
    doReturn(singletonList(excessiveTotalResource))
        .when(resourceAggregator)
        .excess(anyList(), anyList());

    // when
    List<? extends Resource> availableResources =
        availableResourcesProvider.getAvailableOrganizationResources(rootOrganization);

    // then
    assertEquals(availableResources.size(), 1);
    assertEquals(availableResources.get(0), excessiveTotalResource);
    verify(resourceManager).getTotalResources(ROOT_ORG_ID);
    verify(resourceManager).getUsedResources(ROOT_ORG_ID);
    verify(availableResourcesProvider).getUsedResourcesBySuborganizations(ROOT_ORG_NAME);
    verify(resourceAggregator)
        .deduct(asList(totalResource, excessiveTotalResource), singletonList(usedResource));
    verify(resourceAggregator)
        .excess(asList(totalResource, excessiveTotalResource), singletonList(usedResource));
  }

  @Test
  public void shouldCalculateUsedResourceBySuborganizations() throws Exception {
    // given
    doReturn(new Page<>(singletonList(suborganization), 0, 1, 2))
        .doReturn(new Page<>(singletonList(subsuborganization), 1, 1, 2))
        .when(organizationManager)
        .getSuborganizations(anyString(), anyInt(), anyLong());
    ResourceImpl usedBySuborgResource = new ResourceImpl("test", 1500, "unit");
    doReturn(singletonList(usedBySuborgResource)).when(resourceManager).getUsedResources(SUBORG_ID);
    ResourceImpl usedBySubsuborgResource = new ResourceImpl("test", 2000, "unit");
    doReturn(singletonList(usedBySubsuborgResource))
        .when(resourceManager)
        .getUsedResources(SUBSUBORG_ID);

    // when
    List<? extends Resource> usedResources =
        availableResourcesProvider.getUsedResourcesBySuborganizations(ROOT_ORG_NAME);

    // then
    assertEquals(usedResources.size(), 2);
    assertTrue(usedResources.contains(usedBySuborgResource));
    assertTrue(usedResources.contains(usedBySubsuborgResource));
    verify(organizationManager, times(2))
        .getSuborganizations(eq(ROOT_ORG_NAME), anyInt(), anyLong());
    verify(resourceManager).getUsedResources(SUBORG_ID);
    verify(resourceManager).getUsedResources(SUBSUBORG_ID);
  }

  private void prepareAvailableResource(String organizationId, ResourceImpl availableResource)
      throws NotFoundException, ServerException {
    doReturn(singletonList(availableResource))
        .when(availableResourcesProvider)
        .getAvailableOrganizationResources(
            argThat(argument -> organizationId.equals(((Organization) argument).getId())));
  }
}
