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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.devfile.server.DevfileFormatException;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;

/** Validates YAML devfile content against given JSON schema. */
@Singleton
public class DevfileSchemaValidator {

  private JsonValidator validator;
  private ObjectMapper yamlReader;
  private DevfileSchemaProvider schemaProvider;

  @Inject
  public DevfileSchemaValidator(DevfileSchemaProvider schemaProvider) {
    this.schemaProvider = schemaProvider;
    this.validator = JsonSchemaFactory.byDefault().getValidator();
    this.yamlReader = new ObjectMapper(new YAMLFactory());
  }

  public JsonNode validateBySchema(String yamlContent) throws DevfileFormatException {
    ProcessingReport report;
    JsonNode data;
    try {
      data = yamlReader.readTree(yamlContent);
      report = validator.validate(schemaProvider.getJsoneNode(), data);
    } catch (IOException | ProcessingException e) {
      throw new DevfileFormatException("Unable to validate Devfile. Error: " + e.getMessage());
    }
    if (!report.isSuccess()) {
      throw new DevfileFormatException(prepareErrorMessage(report));
    }
    return data;
  }

  private String prepareErrorMessage(ProcessingReport report) {
    List<String> errors = new ArrayList<>();
    StreamSupport.stream(report.spliterator(), false)
        .filter(m -> m.getLogLevel() == LogLevel.ERROR || m.getLogLevel() == LogLevel.FATAL)
        .forEach(msg -> recursivelyFindErrors(msg.asJson(), errors));
    StringBuilder sb = new StringBuilder("Devfile schema validation failed.");
    if (errors.size() == 1) {
      sb.append(" Error: ").append(errors.get(0));
    } else {
      String msg = errors.stream().collect(Collectors.joining(",", "[", "]"));
      sb.append(" Errors: ").append(msg);
    }
    return sb.toString();
  }

  private void recursivelyFindErrors(JsonNode node, List<String> messages) {
    if (node instanceof ArrayNode) {
      for (JsonNode subNode : node) {
        recursivelyFindErrors(subNode, messages);
      }
    } else {
      if (node.get("reports") == null) {
        String pointer = "/devfile" + node.get("instance").get("pointer").asText();
        messages.add(pointer + " " + node.get("message").asText());
      } else {
        messages.add(node.get("message").asText());
        for (JsonNode subNode : node.get("reports")) {
          recursivelyFindErrors(subNode, messages);
        }
      }
    }
  }
}
