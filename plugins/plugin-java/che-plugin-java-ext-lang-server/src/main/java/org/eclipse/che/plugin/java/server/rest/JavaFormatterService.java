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
package org.eclipse.che.plugin.java.server.rest;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;

/**
 * Java formatter service. Updates formatter's configuration for the project or for whole workspace.
 */
@Path("java/formatter/")
public class JavaFormatterService {

  public static final String CHE_FOLDER = ".che";
  public static final String CHE_FORMATTER_XML = "che-formatter.xml";

  private final FsManager fsManager;

  @Inject
  public JavaFormatterService(FsManager fsManager) {
    this.fsManager = fsManager;
  }

  @POST
  @Path("update/workspace")
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Updates configuration of the jav formatter for the workspace")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Formatter was imported successfully"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void updateRootFormatter(
      @ApiParam(value = "The content of the formatter. Eclipse code formatting is supported only")
          String content)
      throws ServerException {
    try {
      String rootCheFolderWsPath = absolutize(CHE_FOLDER);

      if (!fsManager.existsAsDir(rootCheFolderWsPath)) {
        fsManager.createDir(rootCheFolderWsPath);
      }

      String cheFormatterWsPath = resolve(rootCheFolderWsPath, CHE_FORMATTER_XML);

      if (!fsManager.existsAsFile(cheFormatterWsPath)) {
        fsManager.createFile(cheFormatterWsPath, content);
      } else {
        fsManager.update(cheFormatterWsPath, content);
      }
    } catch (ServerException | ConflictException | NotFoundException e) {
      throw new ServerException(e);
    }
  }

  @POST
  @Path("update/project")
  @Consumes(MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Updates configuration of the jav formatter for the project")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Formatter was imported successfully"),
    @ApiResponse(code = 404, message = "The project was not found"),
    @ApiResponse(code = 500, message = "Internal server error occurred")
  })
  public void updateProjectFormatter(
      @ApiParam(value = "Path to the root project") @QueryParam("projectpath") String projectPath,
      @ApiParam(value = "The content of the formatter. Eclipse code formatting is supported only")
          String content)
      throws ServerException, NotFoundException {
    try {
      String projectWsPath = absolutize(projectPath);
      String projectCheFolderWsPath = resolve(projectWsPath, CHE_FOLDER);

      if (!fsManager.existsAsDir(projectCheFolderWsPath)) {
        fsManager.createDir(projectCheFolderWsPath);
      }

      String cheFormatterWsPath = resolve(projectCheFolderWsPath, CHE_FORMATTER_XML);

      if (!fsManager.existsAsFile(cheFormatterWsPath)) {
        fsManager.createFile(cheFormatterWsPath, content);
      } else {
        fsManager.update(cheFormatterWsPath, content);
      }

    } catch (ConflictException | NotFoundException e) {
      throw new ServerException(e);
    }
  }
}
