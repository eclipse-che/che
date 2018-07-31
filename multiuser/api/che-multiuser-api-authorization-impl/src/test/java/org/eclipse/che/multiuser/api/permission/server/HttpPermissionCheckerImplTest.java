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
package org.eclipse.che.multiuser.api.permission.server;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.api.permission.shared.dto.PermissionsDto;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link HttpPermissionCheckerImpl}.
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class HttpPermissionCheckerImplTest {
  private static final String API_ENDPOINT = "http://localhost:8000/api";

  @Mock private HttpJsonRequestFactory requestFactory;
  @Mock private HttpJsonResponse response;
  private HttpJsonRequest request;

  private HttpPermissionCheckerImpl httpPermissionChecker;

  @BeforeMethod
  public void setUp() throws Exception {
    request =
        mock(
            HttpJsonRequest.class,
            (Answer)
                invocation -> {
                  if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                    return invocation.getMock();
                  }
                  return RETURNS_DEFAULTS.answer(invocation);
                });
    when(request.request()).thenReturn(response);
    when(requestFactory.fromUrl(anyString())).thenReturn(request);

    httpPermissionChecker = new HttpPermissionCheckerImpl(API_ENDPOINT, requestFactory);
  }

  @Test
  public void shouldCheckPermissionsByHttpRequestToPermissionsService() throws Exception {
    when(response.asDto(anyObject()))
        .thenReturn(
            DtoFactory.newDto(PermissionsDto.class)
                .withUserId("user123")
                .withDomainId("domain123")
                .withInstanceId("instance123")
                .withActions(asList("read", "test")));

    final boolean hasPermission =
        httpPermissionChecker.hasPermission("user123", "domain123", "instance123", "test");

    assertEquals(hasPermission, true);
    verify(requestFactory)
        .fromUrl(
            eq(
                UriBuilder.fromUri(API_ENDPOINT)
                    .path(PermissionsService.class)
                    .path(PermissionsService.class, "getCurrentUsersPermissions")
                    .queryParam("instance", "instance123")
                    .build("domain123")
                    .toString()));
    verify(request).useGetMethod();
    verify(request).request();
    verifyNoMoreInteractions(request);
  }
}
