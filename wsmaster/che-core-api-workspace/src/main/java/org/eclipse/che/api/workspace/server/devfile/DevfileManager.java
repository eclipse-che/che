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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.devfile.convert.DevfileConverter;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
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
  private final DevfileSchemaValidator schemaValidator;
  private final DevfileIntegrityValidator integrityValidator;
  private final DevfileConverter devfileConverter;
  private final WorkspaceManager workspaceManager;

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
   * Creates {@link DevfileImpl} from given devfile content in YAML. Performs schema and integrity
   * validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   */
  public DevfileImpl parseYaml(String devfileContent) throws DevfileFormatException {
    return parse(devfileContent, schemaValidator::validateYaml);
  }

  /**
   * Creates {@link DevfileImpl} from given devfile content in JSON. Performs schema and integrity
   * validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @return Devfile object created from the source content
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws DevfileFormatException when any yaml parsing error occurs
   */
  public DevfileImpl parseJson(String devfileContent) throws DevfileFormatException {
    return parse(devfileContent, schemaValidator::validateJson);
  }

  /**
   * Resolve devfile component references into their reference content.
   *
   * @param devfile input devfile
   * @param fileContentProvider provider to fetch reference content
   */
  public void resolveReference(DevfileImpl devfile, FileContentProvider fileContentProvider)
      throws DevfileException {
    List<ComponentImpl> toResolve =
        devfile
            .getComponents()
            .stream()
            .filter(
                c ->
                    c.getType().equals(KUBERNETES_COMPONENT_TYPE)
                        || c.getType().equals(OPENSHIFT_COMPONENT_TYPE))
            .filter(c -> !isNullOrEmpty(c.getReference()))
            .collect(Collectors.toList());
    for (ComponentImpl c : toResolve) {
      try {
        c.setReferenceContent(fileContentProvider.fetchContent(c.getReference()));
      } catch (IOException e) {
        throw new DevfileException(
            format(
                "Unable to resolve reference of component: %s",
                firstNonNull(c.getAlias(), c.getReference())),
            e);
      }
    }
  }

  private DevfileImpl parse(String content, ValidationFunction validationFunction)
      throws DevfileFormatException {
    JsonNode parsed = validationFunction.validate(content);

    DevfileImpl devfile;
    try {
      devfile = objectMapper.treeToValue(parsed, DevfileImpl.class);
    } catch (JsonProcessingException e) {
      throw new DevfileFormatException(e.getMessage());
    }

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
  public WorkspaceImpl createWorkspace(DevfileImpl devfile, FileContentProvider fileContentProvider)
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
      DevfileImpl devfile, FileContentProvider fileContentProvider)
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
   * @see WorkspaceManager#getWorkspace(String)
   */
  public DevfileImpl exportWorkspace(String key)
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

  @FunctionalInterface
  private interface ValidationFunction {
    JsonNode validate(String content) throws DevfileFormatException;
  }
}
