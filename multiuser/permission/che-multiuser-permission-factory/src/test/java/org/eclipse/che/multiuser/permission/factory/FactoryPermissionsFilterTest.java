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
package org.eclipse.che.multiuser.permission.factory;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static org.eclipse.che.multiuser.permission.workspace.server.WorkspaceDomain.READ;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.Map;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.factory.server.FactoryManager;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link FactoryPermissionsFilter}.
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class FactoryPermissionsFilterTest {

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private FactoryService service;

  @Mock private FactoryManager factoryManager;

  @InjectMocks private FactoryPermissionsFilter permissionsFilter;

  @Test
  public void shouldCheckPermissionsOnGettingFactoryJsonByWorkspaceId() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/factory/workspace/workspace123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).getFactoryJson(eq("workspace123"), nullable(String.class));
    verify(subject).checkPermission(DOMAIN_ID, "workspace123", READ);
  }

  @Test
  public void shouldReturnForbiddenWhenUserDoesHavePermissionsToReadWorkspaceOnGettingFactoryJson()
      throws Exception {
    doThrow(new ForbiddenException("User in not authorized"))
        .when(subject)
        .checkPermission(anyString(), anyString(), anyString());

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/factory/workspace/workspace123");

    assertEquals(response.getStatusCode(), 403);
  }

  @Test
  public void shouldMakeSureThatUserIsCreatorOnUpdatingFactory() throws Exception {
    doReturn("user123").when(subject).getUserId();

    Factory factory = mock(Factory.class);
    doReturn(new AuthorImpl("user123", 12345L)).when(factory).getCreator();
    when(factoryManager.getById("factory123")).thenReturn(factory);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(DtoFactory.newDto(FactoryDto.class))
            .when()
            .put(SECURE_PATH + "/factory/factory123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).updateFactory(eq("factory123"), any(FactoryDto.class));
  }

  @Test
  public void shouldReturnForbiddenWhenUserIsNotCreatorOnUpdatingFactory() throws Exception {
    doReturn("user321").when(subject).getUserId();

    Factory factory = mock(Factory.class);
    doReturn(new AuthorImpl("user123", 12345L)).when(factory).getCreator();
    when(factoryManager.getById("factory123")).thenReturn(factory);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(DtoFactory.newDto(FactoryDto.class))
            .when()
            .put(SECURE_PATH + "/factory/factory123");

    assertEquals(response.getStatusCode(), 403);
    verify(service, never()).updateFactory(any(), any());
  }

  @Test
  public void shouldMakeSureThatUserIsCreatorOnRemovingFactory() throws Exception {
    doReturn("user123").when(subject).getUserId();

    Factory factory = mock(Factory.class);
    doReturn(new AuthorImpl("user123", 12345L)).when(factory).getCreator();
    when(factoryManager.getById("factory123")).thenReturn(factory);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/factory/factory123");

    assertEquals(response.getStatusCode(), 204);
    verify(service).removeFactory(eq("factory123"));
  }

  @Test
  public void shouldReturnForbiddenWhenUserIsNotCreatorOnRemovingForeignFactory() throws Exception {
    doReturn("user321").when(subject).getUserId();

    Factory factory = mock(Factory.class);
    doReturn(new AuthorImpl("user123", 12345L)).when(factory).getCreator();
    when(factoryManager.getById("factory123")).thenReturn(factory);

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .delete(SECURE_PATH + "/factory/factory123");

    assertEquals(response.getStatusCode(), 403);
    verify(service, never()).removeFactory(any());
  }

  @Test(dataProvider = "publicMethods")
  public void shouldDoNothingWhenPublicMethodMethodIsCalled(String name, Class[] parameterTypes)
      throws Exception {
    GenericResourceMethod genericResourceMethod = mock(GenericResourceMethod.class);
    when(genericResourceMethod.getMethod())
        .thenReturn(FactoryService.class.getMethod(name, parameterTypes));

    permissionsFilter.filter(genericResourceMethod, new Object[0]);
  }

  @DataProvider(name = "publicMethods")
  public Object[][] publicMethods() {
    return new Object[][] {
      {"saveFactory", new Class[] {FactoryDto.class}},
      {"getFactory", new Class[] {String.class, Boolean.class}},
      {"resolveFactory", new Class[] {Map.class, Boolean.class}},
      {"getFactoryByAttribute", new Class[] {Integer.class, Integer.class, UriInfo.class}},
    };
  }

  @Test(
      expectedExceptions = ForbiddenException.class,
      expectedExceptionsMessageRegExp =
          "The user does not have permission to perform this operation")
  public void shouldThrowForbiddenExceptionWhenUnlistedMethodIsCalled() throws Exception {
    GenericResourceMethod genericResourceMethod = mock(GenericResourceMethod.class);
    when(genericResourceMethod.getMethod())
        .thenReturn(FactoryService.class.getMethod("getServiceDescriptor"));

    permissionsFilter.filter(genericResourceMethod, new Object[0]);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {

    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
