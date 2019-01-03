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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Facade for devfile related operations.
 *
 * @author Max Shaposhnyk
 */
@Beta
@Singleton
public class DevfileManager {

  private final ObjectMapper objectMapper;
  private DevfileSchemaValidator schemaValidator;
  private DevfileIntegrityValidator integrityValidator;
  private DevfileConverter devfileConverter;
  private WorkspaceManager workspaceManager;

  @Inject
  public DevfileManager(
      DevfileSchemaValidator schemaValidator,
      DevfileIntegrityValidator integrityValidator,
      DevfileConverter devfileConverter,
      WorkspaceManager workspaceManager) {
    this.schemaValidator = schemaValidator;
    this.integrityValidator = integrityValidator;
    this.devfileConverter = devfileConverter;
    this.workspaceManager = workspaceManager;
    this.objectMapper = new ObjectMapper(new YAMLFactory());
  }

  /**
   * Creates {@link Devfile} from given devfile content. Performs schema and integrity validation of
   * input data.
   *
   * @param devfileContent raw content of devfile
   * @param verbose when true, method returns more explained validation error messages if any
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws JsonProcessingException when parsing error occurs
   */
  public Devfile parse(String devfileContent, boolean verbose)
      throws DevfileFormatException, JsonProcessingException {
    JsonNode parsed = schemaValidator.validateBySchema(devfileContent, verbose);
    Devfile devfile = objectMapper.treeToValue(parsed, Devfile.class);
    integrityValidator.validateDevfile(devfile);
    return devfile;
  }

  /**
   * Creates {@link WorkspaceImpl} from given devfile with available name search
   *
   * @param devfile source devfile
   * @return created {@link WorkspaceImpl} instance
   * @throws DevfileFormatException when devfile integrity validation fail
   * @throws ValidationException when incoming configuration or attributes are not valid
   * @throws ConflictException when any conflict occurs
   * @throws NotFoundException when user account is not found
   * @throws ServerException when other error occurs
   */
  public WorkspaceImpl createWorkspace(Devfile devfile)
      throws ServerException, DevfileFormatException, ConflictException, NotFoundException,
          ValidationException {
    WorkspaceConfigImpl workspaceConfig = createWorkspaceConfig(devfile);
    final String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    return workspaceManager.createWorkspace(
        findAvailableName(workspaceConfig), namespace, emptyMap());
  }

  /**
   * Creates {@link WorkspaceConfigImpl} from given devfile with integrity validation
   *
   * @param devfile source devfile
   * @return created {@link WorkspaceConfigImpl} instance
   * @throws DevfileFormatException when devfile integrity validation fail
   */
  public WorkspaceConfigImpl createWorkspaceConfig(Devfile devfile) throws DevfileFormatException {
    integrityValidator.validateDevfile(devfile);
    return devfileConverter.devFileToWorkspaceConfig(devfile);
  }

  /**
   * Exports provided workspace into devfile
   *
   * @param key string composite workspace key
   * @see WorkspaceManager#getByKey(String)
   * @return devfile representation of given workspace
   * @throws NotFoundException when no workspace can be found by given key
   * @throws ConflictException when workspace cannot be exported into devfile
   * @throws ServerException when other error occurs
   */
  public Devfile exportWorkspace(String key)
      throws NotFoundException, ServerException, ConflictException {
    WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
    try {
      return devfileConverter.workspaceToDevFile(workspace.getConfig());
    } catch (WorkspaceExportException e) {
      throw new ConflictException(e.getMessage());
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
}
