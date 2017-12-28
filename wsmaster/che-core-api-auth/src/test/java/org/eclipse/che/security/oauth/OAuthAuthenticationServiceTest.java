/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.assured.JettyHttpServer;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class OAuthAuthenticationServiceTest {
  @SuppressWarnings("unused")
  private EnvironmentFilter filter = new EnvironmentFilter();

  @SuppressWarnings("unused")
  private final ApiExceptionMapper exceptionMapper = new ApiExceptionMapper();

  @Mock protected OAuthAuthenticatorProvider providers;
  @Mock protected UriInfo uriInfo;
  @Mock protected SecurityContext security;
  @InjectMocks OAuthAuthenticationService service;

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext context = EnvironmentContext.getCurrent();
      context.setSubject(
          new SubjectImpl(JettyHttpServer.ADMIN_USER_NAME, "id-2314", "token-2323", false));
    }
  }

  @Test
  public void shouldThrowExceptionIfNoSuchProviderFound() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .queryParam("oauth_provider", "unknown")
            .get(SECURE_PATH + "/oauth/token");

    assertEquals(response.getStatusCode(), 404);
    assertEquals(
        DtoFactory.getInstance()
            .createDtoFromJson(response.getBody().asInputStream(), ServiceError.class)
            .getMessage(),
        "Unsupported OAuth provider unknown");
  }

  @Test
  public void shouldBeAbleToGetUserToken() throws Exception {
    String provider = "myprovider";
    String token = "token123";
    OAuthAuthenticator authenticator = mock(OAuthAuthenticator.class);
    when(providers.getAuthenticator(eq(provider))).thenReturn(authenticator);
    when(authenticator.getToken(anyString())).thenReturn(newDto(OAuthToken.class).withToken(token));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .queryParam("oauth_provider", provider)
            .get(SECURE_PATH + "/oauth/token");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(
        DtoFactory.getInstance()
            .createDtoFromJson(response.getBody().asInputStream(), OAuthToken.class)
            .getToken(),
        token);
  }
}
