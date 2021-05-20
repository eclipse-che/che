/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.permission.system;

import static com.jayway.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.fail;

import com.google.common.collect.Sets;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.system.server.SystemService;
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
 * Tests {@link SystemServicePermissionsFilter}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class SystemServicePermissionsFilterTest {

  private static final Set<String> TEST_HANDLED_METHODS = new HashSet<>(asList("stop", "getState"));

  @SuppressWarnings("unused")
  private static final SystemServicePermissionsFilter serviceFilter =
      new SystemServicePermissionsFilter();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter envFilter = new EnvironmentFilter();

  @Mock private static Subject subject;

  @Mock private SystemService systemService;

  @Test
  public void allPublicMethodsAreFiltered() {
    Set<String> existingMethods = getDeclaredPublicMethods(SystemService.class);

    if (!existingMethods.equals(TEST_HANDLED_METHODS)) {
      Set<String> existingMinusExpected = Sets.difference(existingMethods, TEST_HANDLED_METHODS);
      Set<String> expectedMinusExisting = Sets.difference(TEST_HANDLED_METHODS, existingMethods);
      fail(
          format(
              "The set of public methods tested by by the filter was changed.\n"
                  + "Methods present in service but not declared in test: '%s'\n"
                  + "Methods present in test but missing from service: '%s'",
              existingMinusExpected, expectedMinusExisting));
    }
  }

  @Test
  public void allowsStopForUserWithManageSystemPermission() throws Exception {
    permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .post(SECURE_PATH + "/system/stop")
        .then()
        .statusCode(204);

    verify(systemService).stop(anyBoolean());
  }

  @Test
  public void rejectsStopForUserWithoutManageSystemPermission() throws Exception {
    permitSubject("nothing");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .post(SECURE_PATH + "/system/stop")
        .then()
        .statusCode(403);

    verify(systemService, never()).stop(anyBoolean());
  }

  @Test
  public void allowsGetStateForUserWithManageSystemPermission() throws Exception {
    permitSubject(SystemDomain.MANAGE_SYSTEM_ACTION);

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/system/state");

    verify(systemService).getState();
  }

  @Test
  public void shouldNotRejectsGetStateForUserWithoutManageSystemPermission() throws Exception {
    permitSubject("nothing");

    given()
        .auth()
        .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        .when()
        .get(SECURE_PATH + "/system/state");

    verify(systemService).getState();
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
        .checkPermission(anyObject(), anyObject(), anyObject());
  }

  private static Set<String> getDeclaredPublicMethods(Class<?> c) {
    return Arrays.stream(c.getDeclaredMethods())
        .filter(method -> Modifier.isPublic(method.getModifiers()))
        .map(Method::getName)
        .collect(Collectors.toSet());
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(subject);
    }
  }
}
