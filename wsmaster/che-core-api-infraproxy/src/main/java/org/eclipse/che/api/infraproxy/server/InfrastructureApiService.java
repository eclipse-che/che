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
package org.eclipse.che.api.infraproxy.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import io.swagger.annotations.Api;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * We use this to give our clients the direct access to the underlying infrastructure REST API. This
 * is only allowed when we can properly impersonate the user - e.g. on OpenShift with OpenShift
 * OAuth switched on.
 */
@Api(InfrastructureApiService.PATH_PREFIX)
@Beta
@Path(InfrastructureApiService.PATH_PREFIX)
public class InfrastructureApiService extends Service {
  static final String PATH_PREFIX = "/unsupported/k8s";
  private static final int PATH_PREFIX_LENGTH = PATH_PREFIX.length();

  private final boolean allowed;
  private final RuntimeInfrastructure runtimeInfrastructure;
  private final ObjectMapper mapper;

  private static boolean determineAllowed(String identityProvider) {
    String infra = System.getenv("CHE_INFRASTRUCTURE_ACTIVE");
    return "openshift".equals(infra)
        && identityProvider != null
        && identityProvider.startsWith("openshift");
  }

  @Inject
  public InfrastructureApiService(
      @Named("che.infra.openshift.oauth_identity_provider") String identityProvider,
      RuntimeInfrastructure runtimeInfrastructure) {
    this.runtimeInfrastructure = runtimeInfrastructure;
    this.mapper = new ObjectMapper();
    this.allowed = determineAllowed(identityProvider);
  }

  @GET
  @Path("{path:.+}")
  public Response get() throws InfrastructureException, ApiException, IOException {
    return request("GET", null);
  }

  @HEAD
  @Path("{path:.+}")
  public Response head() throws InfrastructureException, ApiException, IOException {
    return request("HEAD", null);
  }

  @POST
  @Path("{path:.+}")
  public Response post(Reader body) throws InfrastructureException, IOException, ApiException {
    return request("POST", body);
  }

  @DELETE
  @Path("{path:.+}")
  public Response delete(Reader body) throws InfrastructureException, IOException, ApiException {
    return request("DELETE", body);
  }

  @PUT
  @Path("{path:.+}")
  public Response put(Reader body) throws InfrastructureException, IOException, ApiException {
    return request("PUT", body);
  }

  @OPTIONS
  @Path("{path:.+}")
  public Response options() throws InfrastructureException, ApiException, IOException {
    return request("OPTIONS", null);
  }

  @PATCH
  @Path("{path:.+}")
  public Response patch(Reader body) throws InfrastructureException, IOException, ApiException {
    return request("PATCH", body);
  }

  private void auth() throws ApiException {
    if (!allowed) {
      throw new ForbiddenException(
          "Interaction with backing infrastructure is only allowed in multi-user mode with OpenShift OAuth");
    }
  }

  private Response request(String method, @Nullable Reader body)
      throws ApiException, IOException, InfrastructureException {
    auth();
    JsonNode bodyJson = body == null ? mapper.missingNode() : mapper.readTree(body);
    return runtimeInfrastructure.sendDirectInfrastructureRequest(
        method, relativizeRequestAndStripPrefix(), bodyJson.isMissingNode() ? null : bodyJson);
  }

  /**
   * We need to strip our prefix from the request path before sending it to the infrastructure. The
   * infrastructure is unaware of where we deployed our proxy.
   *
   * @return the relative URI composed from the current request
   */
  private URI relativizeRequestAndStripPrefix() {
    URI unstrippedRelative = uriInfo.getBaseUri().relativize(uriInfo.getRequestUri());
    String str = unstrippedRelative.toString();
    return URI.create(str.substring(PATH_PREFIX_LENGTH));
  }
}
