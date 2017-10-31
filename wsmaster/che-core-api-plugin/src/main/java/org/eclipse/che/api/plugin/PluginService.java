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

package org.eclipse.che.api.plugin;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.ServerException;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Yevhen Vydolob */
@Api(value = "/plugin", description = "Plugin REST API")
@Path("/plugin")
public class PluginService {
  private static final Logger LOG = LoggerFactory.getLogger(PluginService.class);
  private static Tika TIKA;

  private final PluginRegistry registry;

  @Inject
  public PluginService(PluginRegistry registry) {
    this.registry = registry;
  }

  /**
   * Load all installed plugins meta information
   *
   * @return
   */
  @GET
  @ApiOperation(value = "Get a list of installed plugins meta information", response = List.class)
  @Produces(APPLICATION_JSON)
  public List<String> getPlugins() {
    return registry.getPlugins();
  }

  @GET
  @Path("/{plugin}/{path:.*}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportFile(
      @PathParam("plugin") String pluginFQN,
      @ApiParam(value = "Path to resource") @PathParam("path") String path)
      throws NotFoundException, ForbiddenException, ServerException {

    File file = new File(PluginRegistry.PLUGIN_PATH, pluginFQN + "/" + path);

    InputStream inputStream;
    try {
      inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ);
    } catch (IOException e) {
      String errorMessage = String.format("Unable get content of '%s'", file.getPath());
      LOG.error(errorMessage + "\n" + e.getMessage(), e);
      throw new ServerException(errorMessage);
    }
    return Response.ok(inputStream, getTIKA().detect(file.getName()))
        .lastModified(new Date(file.lastModified()))
        .header(HttpHeaders.CONTENT_LENGTH, Long.toString(file.length()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + '"')
        .build();
  }

  /** Lazy init of Tika. */
  private synchronized Tika getTIKA() {
    if (TIKA == null) {
      TIKA = new Tika();
    }
    return TIKA;
  }
}
