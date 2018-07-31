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
package org.eclipse.che.multiuser.organization.api.resource;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.jayway.restassured.response.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDistributedResourcesDto;
import org.eclipse.che.multiuser.resource.api.free.ResourceValidator;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.resource.OrganizationResourcesDistributionService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class OrganizationResourcesDistributionServiceTest {
  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper mapper;

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

  @Mock private OrganizationResourcesDistributor organizationResourcesManager;
  @Mock private ResourceValidator resourceValidator;

  @InjectMocks private OrganizationResourcesDistributionService service;

  @Test
  public void shouldCapOrganizationResources() throws Exception {
    ResourceDto resource =
        DtoFactory.newDto(ResourceDto.class).withType("test").withAmount(1020).withUnit("unit");
    List<ResourceDto> resources = singletonList(resource);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .body(resources)
        .when()
        .expect()
        .statusCode(204)
        .post(SECURE_PATH + "/organization/resource/organization123/cap");

    verify(organizationResourcesManager).capResources("organization123", resources);
    verify(resourceValidator).validate(resource);
  }

  @Test
  public void
      shouldReturn400WhenBodyContainTwoResourcesWithTheSameTypeOnDistributingOrganizationResources()
          throws Exception {
    List<ResourceDto> resources =
        Arrays.asList(
            DtoFactory.newDto(ResourceDto.class).withType("test"),
            DtoFactory.newDto(ResourceDto.class).withType("test"));

    String response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(resources)
            .when()
            .expect()
            .statusCode(400)
            .post(SECURE_PATH + "/organization/resource/organization123/cap")
            .print();

    String errorMessage =
        DtoFactory.getInstance().createDtoFromJson(response, ServiceError.class).getMessage();
    assertEquals(errorMessage, "Resources to cap must contain only one resource with type 'test'.");
  }

  @Test
  public void shouldReturnResourcesCapForSuborganization() throws Exception {
    final ResourceDto resourcesCap =
        DtoFactory.newDto(ResourceDto.class).withType("test").withAmount(1020).withUnit("unit");
    final List<ResourceDto> toFetch = singletonList(resourcesCap);
    doReturn(toFetch).when(organizationResourcesManager).getResourcesCaps(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization/resource/organization123/cap");

    final List<ResourceDto> fetched = unwrapDtoList(response, ResourceDto.class);
    assertEquals(fetched.size(), 1);
    assertTrue(fetched.contains(resourcesCap));
    verify(organizationResourcesManager).getResourcesCaps("organization123");
  }

  @Test
  public void shouldReturnOrganizationDistributedResources() throws Exception {
    final OrganizationDistributedResourcesDto distributedResources =
        createOrganizationDistributedResources();
    final List<OrganizationDistributedResourcesDto> toFetch = singletonList(distributedResources);
    doReturn(new Page<>(toFetch, 1, 1, 3))
        .when(organizationResourcesManager)
        .getByParent(any(), anyInt(), anyLong());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization/resource/organization123?maxItems=1&skipCount=1");

    final List<OrganizationDistributedResourcesDto> fetched =
        unwrapDtoList(response, OrganizationDistributedResourcesDto.class);
    assertEquals(fetched.size(), 1);
    assertTrue(fetched.contains(distributedResources));
    verify(organizationResourcesManager).getByParent("organization123", 1, 1L);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance()
        .createListDtoFromJson(response.body().print(), dtoClass)
        .stream()
        .collect(Collectors.toList());
  }

  private OrganizationDistributedResourcesDto createOrganizationDistributedResources() {
    return DtoFactory.newDto(OrganizationDistributedResourcesDto.class)
        .withOrganizationId("organization123")
        .withResourcesCap(
            singletonList(
                DtoFactory.newDto(ResourceDto.class)
                    .withType("test")
                    .withAmount(1020)
                    .withUnit("unit")));
  }
}
