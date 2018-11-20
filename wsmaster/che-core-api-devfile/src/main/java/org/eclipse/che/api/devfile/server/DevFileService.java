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
package org.eclipse.che.api.devfile.server;

import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.devfile.server.DevFileConverter.devFileToWorkspaceConfig;
import static org.eclipse.che.api.devfile.server.DevFileConverter.workspaceToDevFile;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.workspace.server.WorkspaceLinksGenerator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.env.EnvironmentContext;

@Path("/devfile")
public class DevFileService extends Service {

  private WorkspaceLinksGenerator linksGenerator;
  private DevFileSchemaValidator schemaValidator;
  private WorkspaceManager workspaceManager;
  private ObjectMapper objectMapper;

  @Inject
  public DevFileService(
      WorkspaceLinksGenerator linksGenerator,
      DevFileSchemaValidator schemaValidator,
      WorkspaceManager workspaceManager) {
    this.linksGenerator = linksGenerator;
    this.schemaValidator = schemaValidator;
    this.workspaceManager = workspaceManager;
    this.objectMapper = new ObjectMapper(new YAMLFactory());
  }

  //  Creates a workspace by providing the url to the repository
  //  Initially this method will return
  //  empty workspace configuration. And will start che-devfile-broker on a background to clone
  //  sources and get devfile.
  //  @POST
  //  @Produces(APPLICATION_JSON)
  //  public Response create(@QueryParam("repo_url") String repo_url){
  //  }
  //
  //

  /** Generates a workspace from provided devfile to a rest API */
  @POST
  @Consumes({"text/yaml", "text/x-yaml", "application/yaml"})
  @Produces(APPLICATION_JSON)
  public WorkspaceDto createFromYaml(String data)
      throws ServerException, ConflictException, NotFoundException, ValidationException,
          BadRequestException {

    Devfile devFile;
    WorkspaceConfig workspaceConfig;
    try {
      schemaValidator.validateBySchema(data);
      devFile = objectMapper.readValue(data, Devfile.class);
      workspaceConfig = devFileToWorkspaceConfig(devFile);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    } catch (DevFileFormatException e) {
      throw new BadRequestException(e.getMessage());
    }

    final String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    WorkspaceImpl workspace =
        workspaceManager.createWorkspace(workspaceConfig, namespace, emptyMap());
    WorkspaceDto workspaceDto =
        asDto(workspace).withLinks(linksGenerator.genLinks(workspace, getServiceContext()));
    return workspaceDto;
  }

  /**
   * Generates the devfile based on an existing workspace. Key is workspace id or
   * namespace/workspace_name
   *
   * @see WorkspaceManager#getByKey(String)
   */
  @GET
  @Path("/{key:.*}")
  @Produces("text/yml")
  public Response createFromWorkspace(@PathParam("key") String key)
      throws NotFoundException, ServerException {
    // TODO: validate key
    WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
    Devfile workspaceDevFile = workspaceToDevFile(workspace.getConfig());
    // Write object as YAML
    try {
      return Response.ok().entity(objectMapper.writeValueAsString(workspaceDevFile)).build();
    } catch (JsonProcessingException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }
}
