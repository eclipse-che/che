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
package org.eclipse.che.multiuser.organization.api;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.HashSet;
import java.util.List;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.organization.api.OrganizationService}.
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class OrganizationServiceTest {

  private static final String CURRENT_USER_ID = "user123";

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper mapper;

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private EnvironmentFilter filter;

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

  @Mock private OrganizationManager orgManager;

  @Mock private OrganizationLinksInjector linksInjector;

  @Mock private OrganizationValidator validator;

  @InjectMocks private OrganizationService service;

  @BeforeMethod
  public void setUp() throws Exception {
    when(linksInjector.injectLinks(any(), any()))
        .thenAnswer(invocation -> invocation.getArguments()[0]);
  }

  @Test
  public void shouldCreateOrganization() throws Exception {
    when(orgManager.create(any()))
        .thenAnswer(
            invocationOnMock ->
                new OrganizationImpl((Organization) invocationOnMock.getArguments()[0]));

    final OrganizationDto toCreate = createOrganization();

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(toCreate)
            .when()
            .expect()
            .statusCode(201)
            .post(SECURE_PATH + "/organization");

    final OrganizationDto createdOrganization = unwrapDto(response, OrganizationDto.class);
    assertEquals(createdOrganization, toCreate);
    verify(linksInjector).injectLinks(any(), any());
    verify(orgManager).create(eq(toCreate));
  }

  @Test
  public void shouldThrowBadRequestWhenCreatingNonValidOrganization() throws Exception {
    doThrow(new BadRequestException("non valid organization"))
        .when(validator)
        .checkOrganization(any());

    final OrganizationDto toCreate = createOrganization();

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(toCreate)
            .when()
            .expect()
            .statusCode(400)
            .post(SECURE_PATH + "/organization");

    final ServiceError error = unwrapDto(response, ServiceError.class);
    assertEquals(error.getMessage(), "non valid organization");
    verify(validator).checkOrganization(toCreate);
  }

  @Test
  public void shouldUpdateOrganization() throws Exception {
    when(orgManager.update(anyString(), any()))
        .thenAnswer(
            invocationOnMock ->
                new OrganizationImpl((Organization) invocationOnMock.getArguments()[1]));

    final OrganizationDto toUpdate = createOrganization();

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(toUpdate)
            .when()
            .expect()
            .statusCode(200)
            .post(SECURE_PATH + "/organization/organization123");

    final OrganizationDto createdOrganization = unwrapDto(response, OrganizationDto.class);
    assertEquals(createdOrganization, toUpdate);
    verify(linksInjector).injectLinks(any(), any());
    verify(orgManager).update(eq("organization123"), eq(toUpdate));
  }

  @Test
  public void shouldThrowBadRequestWhenUpdatingNonValidOrganization() throws Exception {
    doThrow(new BadRequestException("non valid organization"))
        .when(validator)
        .checkOrganization(any());

    final OrganizationDto toUpdate = createOrganization();

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(toUpdate)
            .when()
            .expect()
            .statusCode(400)
            .post(SECURE_PATH + "/organization/organization123");

    final ServiceError error = unwrapDto(response, ServiceError.class);
    assertEquals(error.getMessage(), "non valid organization");
    verify(validator).checkOrganization(toUpdate);
  }

  @Test
  public void shouldRemoveOrganization() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .expect()
        .statusCode(204)
        .delete(SECURE_PATH + "/organization/organization123");

    verify(orgManager).remove(eq("organization123"));
  }

  @Test
  public void shouldGetOrganizationById() throws Exception {
    final OrganizationDto toFetch = createOrganization();

    when(orgManager.getById(eq("organization123"))).thenReturn(toFetch);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization/organization123");

    final OrganizationDto fetchedOrganization = unwrapDto(response, OrganizationDto.class);
    assertEquals(fetchedOrganization, toFetch);
    verify(orgManager).getById(eq("organization123"));
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldFindOrganizationByName() throws Exception {
    final OrganizationDto toFetch = createOrganization();

    when(orgManager.getByName(eq("subOrg"))).thenReturn(toFetch);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization/find?name=subOrg");

    final OrganizationDto fetchedOrganization = unwrapDto(response, OrganizationDto.class);
    assertEquals(fetchedOrganization, toFetch);
    verify(orgManager).getByName(eq("subOrg"));
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldThrowBadRequestExceptionWhenFindingOrganizationWithoutName() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .expect()
        .statusCode(400)
        .get(SECURE_PATH + "/organization/find");
  }

  @Test
  public void shouldGetChildOrganizations() throws Exception {
    final OrganizationDto toFetch = createOrganization();

    doReturn(new Page<>(singletonList(toFetch), 0, 1, 1))
        .when(orgManager)
        .getByParent(anyString(), anyInt(), anyLong());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization/parentOrg123/organizations?skipCount=0&maxItems=1");

    final List<OrganizationDto> organizationDtos = unwrapDtoList(response, OrganizationDto.class);
    assertEquals(organizationDtos.size(), 1);
    assertEquals(organizationDtos.get(0), toFetch);
    verify(orgManager).getByParent("parentOrg123", 1, 0);
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldGetOrganizationsByCurrentUserIfParameterIsNotSpecified() throws Exception {
    final OrganizationDto toFetch = createOrganization();

    doReturn(new Page<>(singletonList(toFetch), 0, 1, 1))
        .when(orgManager)
        .getByMember(anyString(), anyInt(), anyInt());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization?skipCount=0&maxItems=1");

    final List<OrganizationDto> organizationDtos = unwrapDtoList(response, OrganizationDto.class);
    assertEquals(organizationDtos.size(), 1);
    assertEquals(organizationDtos.get(0), toFetch);
    verify(orgManager).getByMember(CURRENT_USER_ID, 1, 0);
    verify(linksInjector).injectLinks(any(), any());
  }

  @Test
  public void shouldGetOrganizationsBySpecifiedUser() throws Exception {
    final OrganizationDto toFetch = createOrganization();

    doReturn(new Page<>(singletonList(toFetch), 0, 1, 1))
        .when(orgManager)
        .getByMember(anyString(), anyInt(), anyInt());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/organization?user=user789&skipCount=0&maxItems=1");

    final List<OrganizationDto> organizationDtos = unwrapDtoList(response, OrganizationDto.class);
    assertEquals(organizationDtos.size(), 1);
    assertEquals(organizationDtos.get(0), toFetch);
    verify(orgManager).getByMember("user789", 1, 0);
    verify(linksInjector).injectLinks(any(), any());
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

  private OrganizationDto createOrganization() {
    return DtoFactory.newDto(OrganizationDto.class)
        .withId("organization123")
        .withName("subOrg")
        .withQualifiedName("parentOrg/subOrg")
        .withParent("parentOrg123");
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent()
          .setSubject(new SubjectImpl("userName", CURRENT_USER_ID, "token", false));
    }
  }
}
