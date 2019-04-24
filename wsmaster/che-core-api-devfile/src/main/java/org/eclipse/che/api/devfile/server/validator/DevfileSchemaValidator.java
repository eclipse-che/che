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
package org.eclipse.che.api.devfile.server.validator;

import static com.google.common.base.Strings.isNullOrEmpty;
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
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;

/** Validates YAML devfile content against given JSON schema. */
@Singleton
public class DevfileSchemaValidator {

  private static final JsonValidationService service = JsonValidationService.newInstance();
  private ObjectMapper yamlReader;
  private ObjectMapper jsonWriter;
  private DevfileSchemaProvider schemaProvider;

  @Inject
  public DevfileSchemaValidator(DevfileSchemaProvider schemaProvider) {
    this.schemaProvider = schemaProvider;
    this.yamlReader = new ObjectMapper(new YAMLFactory());
    this.jsonWriter = new ObjectMapper();
  }

  public JsonNode validateBySchema(String yamlContent) throws DevfileFormatException {
    JsonSchema schema;
    JsonNode contentNode;
    try {
      schema = service.readSchema(schemaProvider.getAsReader());
      contentNode = yamlReader.readTree(yamlContent);

      // Problem handler
      List<Problem> validationErrors = new ArrayList<>();
      ProblemHandler handler = ProblemHandler.collectingTo(validationErrors);
      try (JsonReader reader =
          service.createReader(
              new StringReader(jsonWriter.writeValueAsString(contentNode)), schema, handler)) {
        reader.read();
      }
      if (!validationErrors.isEmpty()) {
        //        System.out.println(validationErrors);
        String error = extractMessages(validationErrors, new StringBuilder());
        throw new DevfileFormatException(
            format("Devfile schema validation failed. Errors: %s", error));
      }
    } catch (IOException e) {
      throw new DevfileFormatException("Unable to validate Devfile. Error: " + e.getMessage());
    }
    return contentNode;
  }

  private String extractMessages(List<Problem> validationErrors, StringBuilder messageBuilder) {

    for (Problem problem : validationErrors) {
      int branchCount = problem.countBranches();
      if (branchCount == 0) {
        messageBuilder.append(getMessage(problem));
      } else {
        messageBuilder.append(problem.getMessage()).append(": [");
        for (int i = 0; i < branchCount; i++) {
          extractMessages(problem.getBranch(i), messageBuilder);
        }
        messageBuilder.append("]");
      }
    }
    return messageBuilder.toString();
  }

  private String getMessage(Problem problem) {
    StringBuilder messageBuilder = new StringBuilder();
    if (!isNullOrEmpty(problem.getPointer())) {
      messageBuilder.append("(").append(problem.getPointer()).append("):");
    }
    messageBuilder.append(problem.getMessage());
    return messageBuilder.toString();
  }
}
