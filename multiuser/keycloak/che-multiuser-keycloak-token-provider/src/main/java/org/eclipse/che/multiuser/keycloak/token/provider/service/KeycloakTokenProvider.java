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
package org.eclipse.che.multiuser.keycloak.token.provider.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.annotation.Nullable;

@Singleton
public class KeycloakTokenProvider {
  private static final String ACCESS_TOKEN = "access_token";
  private final String gitHubEndpoint;
  private final String openShiftEndpoint;

  private final HttpJsonRequestFactory httpJsonRequestFactory;

  @Inject
  public KeycloakTokenProvider(
      @Nullable @Named("che.keycloak.github.endpoint") String gitHubEndpoint,
      @Nullable @Named("che.keycloak.oso.endpoint") String openShiftEndpoint,
      HttpJsonRequestFactory httpJsonRequestFactory) {
    this.gitHubEndpoint = gitHubEndpoint;
    this.openShiftEndpoint = openShiftEndpoint;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
  }

  /**
   * Return GitHub access token based on Keycloak token
   *
   * <p>Note: valid response from keycloak endpoint:
   * {"access_token":"token","scope":"admin:repo_hook,gist,read:org,repo,user","token_type":"bearer"}
   *
   * @param keycloakToken
   * @return GitHub access token
   * @throws IOException
   * @throws BadRequestException
   * @throws ConflictException
   * @throws NotFoundException
   * @throws ForbiddenException
   * @throws UnauthorizedException
   * @throws ServerException
   */
  public String obtainGitHubToken(final String keycloakToken)
      throws ServerException, UnauthorizedException, ForbiddenException, NotFoundException,
          ConflictException, BadRequestException, IOException {
    return getAccessToken(gitHubEndpoint, keycloakToken);
  }

  /**
   * Return OpenShift online token based on Keycloak token
   *
   * <p>Note: valid response from keycloak endpoint:
   * {"access_token":"token","expires_in":86400,"scope":"user:full","token_type":"Bearer"}
   *
   * @param keycloakToken
   * @return OpenShift online token
   * @throws BadRequestException
   * @throws ConflictException
   * @throws NotFoundException
   * @throws ForbiddenException
   * @throws UnauthorizedException
   * @throws ServerException
   */
  public String obtainOsoToken(final String keycloakToken)
      throws IOException, ServerException, UnauthorizedException, ForbiddenException,
          NotFoundException, ConflictException, BadRequestException {
    return getAccessToken(openShiftEndpoint, keycloakToken);
  }

  private String getAccessToken(final String endpoint, final String keycloakToken)
      throws IOException, ForbiddenException, BadRequestException, ConflictException,
          NotFoundException, ServerException, UnauthorizedException {
    String responseBody = getResponseBody(endpoint, keycloakToken);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode json = mapper.readTree(responseBody);
    JsonNode token = json.get(ACCESS_TOKEN);
    return token.asText();
  }

  private String getResponseBody(final String endpoint, final String keycloakToken)
      throws ServerException, UnauthorizedException, ForbiddenException, NotFoundException,
          ConflictException, BadRequestException, IOException {
    HttpJsonResponse request =
        httpJsonRequestFactory
            .fromUrl(endpoint)
            .setMethod("GET")
            .setAuthorizationHeader(keycloakToken)
            .request();
    return request.asString();
  }
}
