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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

@Singleton
public class DevfileManager {

  private final ObjectMapper objectMapper;
  private DevfileSchemaValidator schemaValidator;
  private DevfileIntegrityValidator integrityValidator;
  private DevfileConverter devfileConverter;

  @Inject
  public DevfileManager(
      DevfileSchemaValidator schemaValidator,
      DevfileIntegrityValidator integrityValidator,
      DevfileConverter devfileConverter) {
    this.schemaValidator = schemaValidator;
    this.integrityValidator = integrityValidator;
    this.devfileConverter = devfileConverter;
    this.objectMapper = new ObjectMapper(new YAMLFactory());
  }

  /**
   * Creates {@link WorkspaceConfigImpl} from given devfile content. Performs schema and integrity
   * validation of input data.
   *
   * @param devfileContent raw content of devfile
   * @param verbose when true, method returns more explained validation error messages if any
   * @return WorkspaceConfig created from the devfile
   * @throws DevfileFormatException when any of schema or integrity validations fail
   * @throws JsonProcessingException when parsing error occurs
   */
  public WorkspaceConfigImpl validateAndConvert(String devfileContent, boolean verbose)
      throws DevfileFormatException, JsonProcessingException {
    JsonNode parsed = schemaValidator.validateBySchema(devfileContent, verbose);
    Devfile devFile = objectMapper.treeToValue(parsed, Devfile.class);
    integrityValidator.validateDevfile(devFile);
    return devfileConverter.devFileToWorkspaceConfig(devFile);
  }
}
