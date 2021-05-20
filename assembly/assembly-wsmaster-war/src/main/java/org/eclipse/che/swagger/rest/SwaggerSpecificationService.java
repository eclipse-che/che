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
package org.eclipse.che.swagger.rest;

import io.swagger.jaxrs.listing.ApiListingResource;
import javax.servlet.ServletConfig;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/docs/swagger.{type:json|yaml}")
public class SwaggerSpecificationService extends ApiListingResource {

  @Override
  public Response getListing(
      @Context Application app,
      @Context ServletConfig sc,
      @Context HttpHeaders headers,
      @Context UriInfo uriInfo,
      @PathParam("type") String type) {
    if (type.trim().equalsIgnoreCase("yaml")) {
      return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
    return super.getListing(app, sc, headers, uriInfo, type);
  }
}
