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
package org.eclipse.che.api.workspace.server.devfile.validator;

import static java.lang.String.format;
import static org.eclipse.che.api.workspace.server.devfile.Constants.SUPPORTED_VERSIONS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/** Validates YAML devfile content against given JSON schema. */
@Singleton
public class DevfileSchemaValidator {

  private final JsonValidationService service;
  private final ObjectMapper jsonMapper;
  private final Map<String, JsonSchema> schemasByVersion;
  private final ErrorMessageComposer errorMessageComposer;
  private final DevfileVersionDetector devfileVersionDetector;

  @Inject
  public DevfileSchemaValidator(
      DevfileSchemaProvider schemaProvider, DevfileVersionDetector devfileVersionDetector) {
    this.service = JsonValidationService.newInstance();
    this.jsonMapper = new ObjectMapper();
    this.errorMessageComposer = new ErrorMessageComposer();
    this.devfileVersionDetector = devfileVersionDetector;
    try {
      this.schemasByVersion = new HashMap<>();
      for (String version : SUPPORTED_VERSIONS) {
        this.schemasByVersion.put(version, service.readSchema(schemaProvider.getAsReader(version)));
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to read devfile json schema for validation.", e);
    }
  }

  public void validate(JsonNode contentNode) throws DevfileFormatException {
    try {
      List<Problem> validationErrors = new ArrayList<>();
      ProblemHandler handler = ProblemHandler.collectingTo(validationErrors);
      String devfileVersion = devfileVersionDetector.devfileVersion(contentNode);

      if (!schemasByVersion.containsKey(devfileVersion)) {
        throw new DevfileFormatException(
            String.format(
                "Version '%s' of the devfile is not supported. Supported versions are '%s'.",
                devfileVersion, SUPPORTED_VERSIONS));
      }
      JsonSchema schema = schemasByVersion.get(devfileVersion);
      try (JsonReader reader =
          service.createReader(
              new StringReader(jsonMapper.writeValueAsString(contentNode)), schema, handler)) {
        reader.read();
      }
      if (!validationErrors.isEmpty()) {
        String error = errorMessageComposer.extractMessages(validationErrors, new StringBuilder());
        throw new DevfileFormatException(error);
      }
    } catch (DevfileException dfe) {
      throw new DevfileFormatException(
          format("Devfile schema validation failed. Error: %s", dfe.getMessage()));
    } catch (IOException e) {
      throw new DevfileFormatException("Unable to validate Devfile. Error: " + e.getMessage());
    }
  }
}
