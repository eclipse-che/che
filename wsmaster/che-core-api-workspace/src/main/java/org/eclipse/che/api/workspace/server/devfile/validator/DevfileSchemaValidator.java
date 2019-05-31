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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
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
  private ObjectMapper yamlReader;
  private ObjectMapper jsonWriter;
  private JsonSchema schema;
  private ErrorMessageComposer errorMessageComposer;

  @Inject
  public DevfileSchemaValidator(DevfileSchemaProvider schemaProvider) {
    this.yamlReader = new ObjectMapper(new YAMLFactory());
    this.jsonWriter = new ObjectMapper();
    this.errorMessageComposer = new ErrorMessageComposer();
    try {
      this.schema = service.readSchema(schemaProvider.getAsReader());
    } catch (IOException e) {
      throw new RuntimeException("Unable to read devfile json schema for validation.", e);
    }
  }

  public JsonNode validateBySchema(String yamlContent) throws DevfileFormatException {
    JsonNode contentNode;
    try {
      contentNode = yamlReader.readTree(yamlContent);
      List<Problem> validationErrors = new ArrayList<>();
      ProblemHandler handler = ProblemHandler.collectingTo(validationErrors);
      try (JsonReader reader =
          service.createReader(
              new StringReader(jsonWriter.writeValueAsString(contentNode)), schema, handler)) {
        reader.read();
      }
      if (validationErrors.isEmpty()) {
        return contentNode;
      }
      String error = errorMessageComposer.extractMessages(validationErrors, new StringBuilder());
      throw new DevfileFormatException(
          format("Devfile schema validation failed. Error: %s", error));
    } catch (IOException e) {
      throw new DevfileFormatException("Unable to validate Devfile. Error: " + e.getMessage());
    }
  }
}
