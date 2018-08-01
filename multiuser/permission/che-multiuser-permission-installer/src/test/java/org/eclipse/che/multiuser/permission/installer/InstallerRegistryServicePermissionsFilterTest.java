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
package org.eclipse.che.multiuser.permission.installer;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.api.permission.server.SystemDomain;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link InstallerRegistryServicePermissionsFilter}.
 *
 * @author Max Shaposhnyk
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class InstallerRegistryServicePermissionsFilterTest {

  private static final Set<String> TEST_HANDLED_METHODS =
      new HashSet<>(
          asList(
              "add",
              "remove",
              "update",
              "getInstaller",
              "getVersions",
              "getInstallers",
              "getOrderedInstallers"));

  @SuppressWarnings("unused")
  private static final InstallerRegistryServicePermissionsFilter serviceFilter =
      new InstallerRegistryServicePermissionsFilter();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter envFilter = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private InstallerRegistryService installerRegistryService;

  @Test
  public void allPublicMethodsAreFiltered() {
    Set<String> existingMethods =
        Stream.of(InstallerRegistryService.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(Method::getName)
            .collect(Collectors.toSet());
    assertTrue(existingMethods.size() == TEST_HANDLED_METHODS.size());
    for (String method : TEST_HANDLED_METHODS) {
      assertTrue(existingMethods.contains(method));
    }
  }

  @Test
  public void allowsAddInstallerForUserWithManageSystemPermission() throws Exception {
    permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .post(SECURE_PATH + "/installer")
        .then()
        .statusCode(204);

    verify(installerRegistryService).add(any());
  }

  @Test
  public void rejectsAddInstallerForUserWithoutManageSystemPermission() throws Exception {
    permitSubject("nothing");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .contentType("application/json")
        .when()
        .post(SECURE_PATH + "/installer")
        .then()
        .statusCode(403);

    verify(installerRegistryService, never()).add(any());
  }

  @Test
  public void allowsDeleteInstallerForUserWithManageSystemPermission() throws Exception {
    permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .delete(SECURE_PATH + "/installer/abc-123");

    verify(installerRegistryService).remove(anyString());
  }

  @Test
  public void rejectsDeleteInstallerForUserWithoutManageSystemPermission() throws Exception {
    permitSubject("nothing");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .delete(SECURE_PATH + "/installer/abc-123")
        .then()
        .statusCode(403);

    verify(installerRegistryService, never()).remove(anyString());
  }

  @Test
  public void allowsUpdateInstallerForUserWithManageSystemPermission() throws Exception {
    permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .put(SECURE_PATH + "/installer");

    verify(installerRegistryService).update(any());
  }

  @Test
  public void rejectsUpdateInstallerForUserWithoutManageSystemPermission() throws Exception {
    permitSubject("nothing");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .delete(SECURE_PATH + "/installer/abc-123")
        .then()
        .statusCode(403);

    verify(installerRegistryService, never()).update(any());
  }

  private static void permitSubject(String... allowedActions) throws ForbiddenException {
    doAnswer(
            inv -> {
              if (!new HashSet<>(Arrays.asList(allowedActions))
                  .contains(inv.getArguments()[2].toString())) {
                throw new ForbiddenException("Not allowed!");
              }
              return null;
            })
        .when(subject)
        .checkPermission(any(), any(), any());
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
