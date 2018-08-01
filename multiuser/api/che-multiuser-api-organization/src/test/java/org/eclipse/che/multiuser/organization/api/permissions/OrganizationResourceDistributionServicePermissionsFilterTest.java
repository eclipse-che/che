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
package org.eclipse.che.multiuser.organization.api.permissions;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.server.SuperPrivilegesChecker;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.api.resource.OrganizationResourcesDistributionService;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.permissions.OrganizationResourceDistributionServicePermissionsFilter}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class OrganizationResourceDistributionServicePermissionsFilterTest {
  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @SuppressWarnings("unused")
  private static final CheJsonProvider JSON_PROVIDER = new CheJsonProvider(new HashSet<>());

  private static final String SUBORGANIZATION = "org123";
  private static final String PARENT_ORGANIZATION = "parentOrg123";

  @Mock private static Subject subject;

  @Mock private OrganizationResourcesDistributionService service;

  @Mock private OrganizationManager manager;

  @Mock private SuperPrivilegesChecker superPrivilegesChecker;

  @InjectMocks private OrganizationResourceDistributionServicePermissionsFilter permissionsFilter;

  @BeforeMethod
  public void setUp() throws Exception {
    when(manager.getById(SUBORGANIZATION))
        .thenReturn(new OrganizationImpl(SUBORGANIZATION, "testOrg", PARENT_ORGANIZATION));
    when(manager.getById(PARENT_ORGANIZATION))
        .thenReturn(new OrganizationImpl(PARENT_ORGANIZATION, "parentOrg", null));

    when(subject.hasPermission(anyString(), anyString(), anyString())).thenReturn(true);
  }

  @Test
  public void shouldTestThatAllPublicMethodsAreCoveredByPermissionsFilter() throws Exception {
    // given
    final List<String> collect =
        Stream.of(OrganizationResourcesDistributionService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(toList());

    // then
    assertEquals(collect.size(), 3);
    assertTrue(
        collect.contains(
            OrganizationResourceDistributionServicePermissionsFilter.CAP_RESOURCES_METHOD));
    assertTrue(
        collect.contains(
            OrganizationResourceDistributionServicePermissionsFilter.GET_RESOURCES_CAP_METHOD));
    assertTrue(
        collect.contains(
            OrganizationResourceDistributionServicePermissionsFilter.GET_DISTRIBUTED_RESOURCES));
  }

  @Test
  public void shouldCheckManageResourcesPermissionsOnResourcesCappingForSuborganization()
      throws Exception {
    List<ResourceDto> resources =
        Collections.singletonList(
            DtoFactory.newDto(ResourceDto.class).withType("test").withAmount(123).withUnit("unit"));
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType(MediaType.APPLICATION_JSON)
        .body(resources)
        .expect()
        .statusCode(204)
        .when()
        .post(SECURE_PATH + "/organization/resource/" + SUBORGANIZATION + "/cap");

    verify(service).capResources(SUBORGANIZATION, resources);
    verify(subject)
        .hasPermission(
            OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
  }

  @Test
  public void shouldNotCheckPermissionsOnResourcesCappingForRootOrganization() throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType(MediaType.APPLICATION_JSON)
        .body(emptyList())
        .expect()
        .statusCode(204)
        .when()
        .post(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION + "/cap");

    verify(service).capResources(PARENT_ORGANIZATION, emptyList());
    verify(subject, never()).hasPermission(anyString(), anyString(), anyString());
  }

  @Test
  public void
      shouldCheckManageResourcesPermissionsOnGettingDistributedResourcesWhenUserDoesNotHaveSuperPrivileges()
          throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION);

    verify(service).getDistributedResources(eq(PARENT_ORGANIZATION), anyInt(), anyLong());
    verify(subject)
        .hasPermission(
            OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
    verify(superPrivilegesChecker).hasSuperPrivileges();
  }

  @Test
  public void
      shouldNotCheckManageResourcesPermissionsOnGettingDistributedResourcesWhenUserHasSuperPrivileges()
          throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(true);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(204)
        .when()
        .get(SECURE_PATH + "/organization/resource/" + PARENT_ORGANIZATION);

    verify(service).getDistributedResources(eq(PARENT_ORGANIZATION), anyInt(), anyLong());
    verify(subject, never())
        .hasPermission(
            OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
    verify(superPrivilegesChecker).hasSuperPrivileges();
  }

  @Test
  public void
      shouldCheckManageResourcesPermissionsOnGettingResourcesCapWhenUserDoesNotHaveSuperPrivileges()
          throws Exception {
    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(200)
        .when()
        .get(SECURE_PATH + "/organization/resource/" + SUBORGANIZATION + "/cap");

    verify(service).getResourcesCap(SUBORGANIZATION);
    verify(subject)
        .hasPermission(
            OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
    verify(superPrivilegesChecker).hasSuperPrivileges();
  }

  @Test
  public void
      shouldNotCheckManageResourcesPermissionsOnGettingResourcesCapWhenUserHasSuperPrivileges()
          throws Exception {
    when(superPrivilegesChecker.hasSuperPrivileges()).thenReturn(true);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .expect()
        .statusCode(200)
        .when()
        .get(SECURE_PATH + "/organization/resource/" + SUBORGANIZATION + "/cap");

    verify(service).getResourcesCap(SUBORGANIZATION);
    verify(subject, never())
        .hasPermission(
            OrganizationDomain.DOMAIN_ID, PARENT_ORGANIZATION, OrganizationDomain.MANAGE_RESOURCES);
    verify(superPrivilegesChecker).hasSuperPrivileges();
  }

  @Test(
    expectedExceptions = ForbiddenException.class,
    expectedExceptionsMessageRegExp = "The user does not have permission to perform this operation"
  )
  public void shouldThrowForbiddenExceptionWhenRequestedUnknownMethod() throws Exception {
    final GenericResourceMethod mock = mock(GenericResourceMethod.class);
    Method unknownMethod =
        OrganizationResourcesDistributionService.class.getMethod("getServiceDescriptor");
    when(mock.getMethod()).thenReturn(unknownMethod);

    permissionsFilter.filter(mock, new Object[] {});
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
