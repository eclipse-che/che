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
package org.eclipse.che.api.workspace.server.devfile;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;

@Api(value = "/devfile", description = "Devfile REST API")
@Path("/devfile")
public class DevfileService extends Service {

  private DevfileSchemaProvider schemaCachedProvider;

  @Inject
  public DevfileService(DevfileSchemaProvider schemaCachedProvider) {
    this.schemaCachedProvider = schemaCachedProvider;
  }

  /**
   * Retrieves the json schema.
   *
   * @return json schema
   */
  @GET
  @Produces(APPLICATION_JSON)
  @ApiOperation(value = "Retrieves current version of devfile JSON schema")
  @ApiResponses({
    @ApiResponse(code = 200, message = "The schema successfully retrieved"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public Response getSchema() throws ServerException {
    try {
      return Response.ok(schemaCachedProvider.getSchemaContent()).build();
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }
}
