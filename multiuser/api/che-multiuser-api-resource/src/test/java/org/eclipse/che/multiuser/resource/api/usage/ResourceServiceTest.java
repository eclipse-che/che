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
package org.eclipse.che.multiuser.resource.api.usage;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.shared.dto.ProvidedResourcesDto;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.eclipse.che.multiuser.resource.shared.dto.ResourcesDetailsDto;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourcesDetailsImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link ResourceService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ResourceServiceTest {
  private static final String RESOURCE_TYPE = "test";
  private static final Long RESOURCE_AMOUNT = 1000L;
  private static final String RESOURCE_UNIT = "mb";

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper exceptionMapper;

  @Mock ResourceImpl resource;

  @Mock private ResourceManager resourceManager;

  @InjectMocks private ResourceService service;

  @BeforeMethod
  public void setUp() throws Exception {
    when(resource.getType()).thenReturn(RESOURCE_TYPE);
    when(resource.getAmount()).thenReturn(RESOURCE_AMOUNT);
    when(resource.getUnit()).thenReturn(RESOURCE_UNIT);
  }

  @Test
  public void shouldReturnTotalResourcesForGivenAccount() throws Exception {
    doReturn(singletonList(resource)).when(resourceManager).getTotalResources(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/resource/account123");

    assertEquals(response.statusCode(), 200);
    verify(resourceManager).getTotalResources(eq("account123"));
    final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
    assertEquals(resources.size(), 1);
    final ResourceDto fetchedResource = resources.get(0);
    assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
    assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
    assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
  }

  @Test
  public void shouldReturnUsedResourcesForGivenAccount() throws Exception {
    doReturn(singletonList(resource)).when(resourceManager).getUsedResources(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/resource/account123/used");

    assertEquals(response.statusCode(), 200);
    verify(resourceManager).getUsedResources(eq("account123"));
    final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
    assertEquals(resources.size(), 1);
    final ResourceDto fetchedResource = resources.get(0);
    assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
    assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
    assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
  }

  @Test
  public void shouldReturnAvailableResourcesForGivenAccount() throws Exception {
    doReturn(singletonList(resource)).when(resourceManager).getAvailableResources(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/resource/account123/available");

    assertEquals(response.statusCode(), 200);
    verify(resourceManager).getAvailableResources(eq("account123"));
    final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
    assertEquals(resources.size(), 1);
    final ResourceDto fetchedResource = resources.get(0);
    assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
    assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
    assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
  }

  @Test
  public void testGetsResourceDetails() throws Exception {
    // given
    final ResourceDto testResource =
        DtoFactory.newDto(ResourceDto.class).withType("test").withAmount(1234).withUnit("mb");
    final ResourcesDetailsDto toFetch =
        DtoFactory.newDto(ResourcesDetailsDto.class)
            .withAccountId("account123")
            .withProvidedResources(
                singletonList(
                    DtoFactory.newDto(ProvidedResourcesDto.class)
                        .withId("resource123")
                        .withProviderId("provider")
                        .withOwner("account123")
                        .withStartTime(123L)
                        .withEndTime(321L)
                        .withResources(singletonList(testResource))))
            .withTotalResources(singletonList(testResource));

    // when
    when(resourceManager.getResourceDetails(eq("account123")))
        .thenReturn(new ResourcesDetailsImpl(toFetch));

    // then
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/resource/account123/details");

    final ResourcesDetailsDto resourceDetailsDto =
        DtoFactory.getInstance()
            .createDtoFromJson(response.body().print(), ResourcesDetailsDto.class);
    assertEquals(resourceDetailsDto, toFetch);
    verify(resourceManager).getResourceDetails("account123");
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return new ArrayList<>(
        DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass));
  }
}
