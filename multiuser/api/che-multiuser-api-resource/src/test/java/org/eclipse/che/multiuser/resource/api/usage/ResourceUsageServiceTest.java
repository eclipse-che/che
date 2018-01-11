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
package org.eclipse.che.multiuser.resource.api.usage;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
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
import java.util.List;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.usage.ResourceUsageService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ResourceUsageServiceTest {
  private static final String RESOURCE_TYPE = "test";
  private static final Long RESOURCE_AMOUNT = 1000L;
  private static final String RESOURCE_UNIT = "mb";

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper exceptionMapper;

  @Mock ResourceImpl resource;

  @Mock private ResourceUsageManager resourceUsageManager;

  @InjectMocks private ResourceUsageService service;

  @BeforeMethod
  public void setUp() throws Exception {
    when(resource.getType()).thenReturn(RESOURCE_TYPE);
    when(resource.getAmount()).thenReturn(RESOURCE_AMOUNT);
    when(resource.getUnit()).thenReturn(RESOURCE_UNIT);
  }

  @Test
  public void shouldReturnTotalResourcesForGivenAccount() throws Exception {
    doReturn(singletonList(resource)).when(resourceUsageManager).getTotalResources(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/resource/account123");

    assertEquals(response.statusCode(), 200);
    verify(resourceUsageManager).getTotalResources(eq("account123"));
    final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
    assertEquals(resources.size(), 1);
    final ResourceDto fetchedResource = resources.get(0);
    assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
    assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
    assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
  }

  @Test
  public void shouldReturnUsedResourcesForGivenAccount() throws Exception {
    doReturn(singletonList(resource)).when(resourceUsageManager).getUsedResources(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/resource/account123/used");

    assertEquals(response.statusCode(), 200);
    verify(resourceUsageManager).getUsedResources(eq("account123"));
    final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
    assertEquals(resources.size(), 1);
    final ResourceDto fetchedResource = resources.get(0);
    assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
    assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
    assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
  }

  @Test
  public void shouldReturnAvailableResourcesForGivenAccount() throws Exception {
    doReturn(singletonList(resource)).when(resourceUsageManager).getAvailableResources(any());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .get(SECURE_PATH + "/resource/account123/available");

    assertEquals(response.statusCode(), 200);
    verify(resourceUsageManager).getAvailableResources(eq("account123"));
    final List<ResourceDto> resources = unwrapDtoList(response, ResourceDto.class);
    assertEquals(resources.size(), 1);
    final ResourceDto fetchedResource = resources.get(0);
    assertEquals(fetchedResource.getType(), RESOURCE_TYPE);
    assertEquals(new Long(fetchedResource.getAmount()), RESOURCE_AMOUNT);
    assertEquals(fetchedResource.getUnit(), RESOURCE_UNIT);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance()
        .createListDtoFromJson(response.body().print(), dtoClass)
        .stream()
        .collect(toList());
  }
}
