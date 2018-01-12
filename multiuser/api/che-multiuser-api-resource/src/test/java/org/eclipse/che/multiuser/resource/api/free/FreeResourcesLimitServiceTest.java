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
package org.eclipse.che.multiuser.resource.api.free;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.jayway.restassured.response.Response;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.api.DtoConverter;
import org.eclipse.che.multiuser.resource.model.FreeResourcesLimit;
import org.eclipse.che.multiuser.resource.shared.dto.FreeResourcesLimitDto;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leschenko */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class FreeResourcesLimitServiceTest {
  private static final String TEST_RESOURCE_TYPE = "test";

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper mapper;

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

  @Mock private FreeResourcesLimitManager freeResourcesLimitManager;
  @Mock private FreeResourcesLimitValidator resourcesLimitValidator;

  @InjectMocks private FreeResourcesLimitService service;

  @Test
  public void shouldReturnResourcesLimitForGivenAccount() throws Exception {
    FreeResourcesLimit resourcesLimit =
        new FreeResourcesLimitImpl(
            "account123", singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1000, "unit")));

    when(freeResourcesLimitManager.get("account123")).thenReturn(resourcesLimit);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/resource/free/account123");

    final FreeResourcesLimitDto fetchedLimit = unwrapDto(response, FreeResourcesLimitDto.class);
    assertEquals(fetchedLimit, DtoConverter.asDto(resourcesLimit));
    verify(freeResourcesLimitManager).get("account123");
  }

  @Test
  public void shouldReturnResourcesLimits() throws Exception {
    FreeResourcesLimit resourcesLimit1 =
        new FreeResourcesLimitImpl(
            "account123", singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1000, "unit")));

    FreeResourcesLimit resourcesLimit2 =
        new FreeResourcesLimitImpl(
            "account321", singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 2000, "unit")));

    doReturn(new Page<>(Arrays.asList(resourcesLimit1, resourcesLimit2), 1, 2, 2))
        .when(freeResourcesLimitManager)
        .getAll(anyInt(), anyInt());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/resource/free?skipCount=1&maxItems=5");

    final List<FreeResourcesLimitDto> freeResourcesLimits =
        unwrapDtoList(response, FreeResourcesLimitDto.class);
    assertEquals(freeResourcesLimits.size(), 2);
    assertTrue(freeResourcesLimits.contains(DtoConverter.asDto(resourcesLimit1)));
    assertTrue(freeResourcesLimits.contains(DtoConverter.asDto(resourcesLimit2)));
    verify(freeResourcesLimitManager).getAll(5, 1);
  }

  @Test
  public void shouldStoreResourcesLimit() throws Exception {
    FreeResourcesLimit toCreate =
        new FreeResourcesLimitImpl(
            "account123", singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1000, "unit")));

    FreeResourcesLimit created =
        new FreeResourcesLimitImpl(
            "account123", singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1000, "unit")));
    when(freeResourcesLimitManager.store(any())).thenReturn(created);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(DtoConverter.asDto(toCreate))
            .when()
            .expect()
            .statusCode(201)
            .post(SECURE_PATH + "/resource/free");
    final FreeResourcesLimitDto result = unwrapDto(response, FreeResourcesLimitDto.class);
    assertEquals(DtoConverter.asDto(created), result);
    verify(freeResourcesLimitManager).store(DtoConverter.asDto(toCreate));
    verify(resourcesLimitValidator).check(any());
  }

  @Test
  public void shouldRemoveResourcesLimit() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .expect()
        .statusCode(204)
        .delete(SECURE_PATH + "/resource/free/account123");

    verify(freeResourcesLimitManager).remove("account123");
  }

  private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance()
        .createListDtoFromJson(response.body().print(), dtoClass)
        .stream()
        .collect(toList());
  }
}
