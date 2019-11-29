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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonReader;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/** Validates YAML devfile content against given JSON schema. */
@Singleton
public class DevfileSchemaValidator {

  private final JsonValidationService service = JsonValidationService.newInstance();
  private ObjectMapper jsonMapper;
  private Map<String, JsonSchema> schemasByVersion;
  private ErrorMessageComposer errorMessageComposer;

  @Inject
  public DevfileSchemaValidator(DevfileSchemaProvider schemaProvider) {
    this.jsonMapper = new ObjectMapper();
    this.errorMessageComposer = new ErrorMessageComposer();
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
      if (!contentNode.hasNonNull("apiVersion")) {
        throw new DevfileFormatException(
            "Devfile schema validation failed. Error: The object must have a property whose name is \"apiVersion\".");
      }
      String apiVersion = contentNode.get("apiVersion").asText();

      if (!schemasByVersion.containsKey(apiVersion)) {
        throw new DevfileFormatException(
            String.format(
                "Version '%s' of the devfile is not supported. Supported versions are '%s'.",
                apiVersion, SUPPORTED_VERSIONS));
      }
      JsonSchema schema = schemasByVersion.get(apiVersion);
      try (JsonReader reader =
          service.createReader(
              new StringReader(jsonMapper.writeValueAsString(contentNode)), schema, handler)) {
        reader.read();
      }
      if (!validationErrors.isEmpty()) {
        String error = errorMessageComposer.extractMessages(validationErrors, new StringBuilder());
        throw new DevfileFormatException(
            format("Devfile schema validation failed. Error: %s", error));
      }
    } catch (IOException e) {
      throw new DevfileFormatException("Unable to validate Devfile. Error: " + e.getMessage());
    }
  }
}
