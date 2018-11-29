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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.workspace.server.DtoConverter.asDto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.workspace.server.WorkspaceLinksGenerator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.env.EnvironmentContext;

@Path("/devfile")
public class DevFileService extends Service {

  private WorkspaceLinksGenerator linksGenerator;
  private DevFileSchemaValidator schemaValidator;
  private DevfileSchemaCachedProvider schemaCachedProvider;
  private WorkspaceManager workspaceManager;
  private ObjectMapper objectMapper;
  private DevFileConverter devFileConverter;

  @Inject
  public DevFileService(
      WorkspaceLinksGenerator linksGenerator,
      DevFileSchemaValidator schemaValidator,
      DevfileSchemaCachedProvider schemaCachedProvider,
      WorkspaceManager workspaceManager) {
    this.linksGenerator = linksGenerator;
    this.schemaValidator = schemaValidator;
    this.schemaCachedProvider = schemaCachedProvider;
    this.workspaceManager = workspaceManager;
    this.objectMapper = new ObjectMapper(new YAMLFactory());
    this.devFileConverter = new DevFileConverter();
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

  /**
   * Retrieves the json schema.
   *
   * @return json schema
   */
  @GET
  @Produces(APPLICATION_JSON)
  public Response getSchema() throws ServerException {
    return Response.ok(schemaCachedProvider.getSchemaContent()).build();
  }

  /**
   * Creates workspace from provided devfile
   *
   * @param data devfile content
   * @param verbose return more explained validation error messages if any
   * @return created workspace configuration
   */
  @POST
  @Consumes({"text/yaml", "text/x-yaml", "application/yaml"})
  @Produces(APPLICATION_JSON)
  public WorkspaceDto createFromYaml(String data, @QueryParam("verbose") boolean verbose)
      throws ServerException, ConflictException, NotFoundException, ValidationException,
          BadRequestException {

    Devfile devFile;
    WorkspaceConfigImpl workspaceConfig;
    try {
      JsonNode parsed = schemaValidator.validateBySchema(data, verbose);
      devFile = objectMapper.treeToValue(parsed, Devfile.class);
      workspaceConfig = devFileConverter.devFileToWorkspaceConfig(devFile);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    } catch (DevFileFormatException e) {
      throw new BadRequestException(e.getMessage());
    }

    final String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    WorkspaceImpl workspace =
        workspaceManager.createWorkspace(findAvailableName(workspaceConfig), namespace, emptyMap());
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
      throws NotFoundException, ServerException, BadRequestException {
    validateKey(key);
    WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
    Devfile workspaceDevFile = devFileConverter.workspaceToDevFile(workspace.getConfig());
    // Write object as YAML
    try {
      return Response.ok().entity(objectMapper.writeValueAsString(workspaceDevFile)).build();
    } catch (JsonProcessingException e) {
      throw new ServerException(e.getMessage(), e);
    }
  }

  private WorkspaceConfigImpl findAvailableName(WorkspaceConfigImpl config) throws ServerException {
    String nameCandidate = config.getName();
    String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    int counter = 0;
    while (true) {
      try {
        workspaceManager.getWorkspace(nameCandidate, namespace);
        nameCandidate = config.getName() + "_" + ++counter;
      } catch (NotFoundException nf) {
        config.setName(nameCandidate);
        break;
      }
    }
    return config;
  }

  private void validateKey(String key) throws BadRequestException {
    String[] parts = key.split(":", -1); // -1 is to prevent skipping trailing part
    switch (parts.length) {
      case 1:
        {
          return; // consider it's id
        }
      case 2:
        {
          if (parts[1].isEmpty()) {
            throw new BadRequestException(
                "Wrong composite key format - workspace name required to be set.");
          }
          break;
        }
      default:
        {
          throw new BadRequestException(
              format("Wrong composite key %s. Format should be 'username:workspace_name'. ", key));
        }
    }
  }
}
