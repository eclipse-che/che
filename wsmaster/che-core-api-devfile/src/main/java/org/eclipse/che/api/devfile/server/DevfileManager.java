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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.devfile.server.DevfileFactory.initializeMaps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.convert.DevfileConverter;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
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
    this(
        schemaValidator,
        integrityValidator,
        devfileConverter,
        workspaceManager,
        new ObjectMapper(new YAMLFactory()));
  }

  @VisibleForTesting
  DevfileManager(
      DevfileSchemaValidator schemaValidator,
      DevfileIntegrityValidator integrityValidator,
      DevfileConverter devfileConverter,
      WorkspaceManager workspaceManager,
      ObjectMapper objectMapper) {
    this.schemaValidator = schemaValidator;
    this.integrityValidator = integrityValidator;
    this.devfileConverter = devfileConverter;
    this.workspaceManager = workspaceManager;
    this.objectMapper = objectMapper;
  }

  /**
   * Creates {@link Devfile} from given devfile content. Performs schema and integrity validation of
   * input data.
   *
   * @param devfileContent raw content of devfile
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   */
  public Devfile parse(String devfileContent) throws DevfileFormatException {
    JsonNode parsed = schemaValidator.validateBySchema(devfileContent);

    Devfile devfile;
    try {
      devfile = objectMapper.treeToValue(parsed, Devfile.class);
    } catch (JsonProcessingException e) {
      throw new DevfileFormatException(e.getMessage());
    }
    initializeMaps(devfile);

    integrityValidator.validateDevfile(devfile);
    return devfile;
  }

  /**
   * Creates {@link WorkspaceImpl} from given devfile with available name search
   *
   * @param devfile source devfile
   * @param fileContentProvider content provider for recipe-type component
   * @return created {@link WorkspaceImpl} instance
   * @throws DevfileFormatException when devfile integrity validation fail
   * @throws DevfileRecipeFormatException when devfile recipe format is invalid
   * @throws DevfileException when any another devfile related error occurs
   * @throws ValidationException when incoming configuration or attributes are not valid
   * @throws ConflictException when any conflict occurs
   * @throws NotFoundException when user account is not found
   * @throws ServerException when other error occurs
   */
  public WorkspaceImpl createWorkspace(Devfile devfile, FileContentProvider fileContentProvider)
      throws ServerException, ConflictException, NotFoundException, ValidationException,
          DevfileException {
    checkArgument(devfile != null, "Devfile must not be null");
    checkArgument(fileContentProvider != null, "File content provider must not be null");

    WorkspaceConfigImpl workspaceConfig = createWorkspaceConfig(devfile, fileContentProvider);
    final String namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
    return workspaceManager.createWorkspace(
        findAvailableName(workspaceConfig), namespace, emptyMap());
  }

  /**
   * Creates {@link WorkspaceConfigImpl} from given devfile with integrity validation
   *
   * @param devfile source devfile
   * @param fileContentProvider content provider for recipe-type component
   * @return created {@link WorkspaceConfigImpl} instance
   * @throws DevfileFormatException when devfile integrity validation fail
   * @throws DevfileRecipeFormatException when devfile recipe format is invalid
   * @throws DevfileException when any another devfile related error occurs
   */
  public WorkspaceConfigImpl createWorkspaceConfig(
      Devfile devfile, FileContentProvider fileContentProvider)
      throws DevfileFormatException, DevfileRecipeFormatException, DevfileException {
    checkArgument(devfile != null, "Devfile must not be null");
    checkArgument(fileContentProvider != null, "File content provider must not be null");

    FileContentProvider cachingProvider = FileContentProvider.cached(fileContentProvider);

    integrityValidator.validateDevfile(devfile);
    integrityValidator.validateContentReferences(devfile, cachingProvider);
    return devfileConverter.devFileToWorkspaceConfig(devfile, cachingProvider);
  }

  /**
   * Exports provided workspace into devfile
   *
   * @param key string composite workspace key
   * @return devfile representation of given workspace
   * @throws NotFoundException when no workspace can be found by given key
   * @throws ConflictException when workspace cannot be exported into devfile
   * @throws ServerException when other error occurs
   * @see WorkspaceManager#getByKey(String)
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
