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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import java.io.IOException;
import java.net.URL;
import javax.inject.Singleton;

@Singleton
public class DevFileSchemaValidator {

  private JsonValidator validator;
  private JsonNode schema;
  private ObjectMapper yamlReader;

  public DevFileSchemaValidator() throws IOException {
    final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
    URL schemaURL = getClass().getClassLoader().getResource("schema/devfile.json");
    this.schema = JsonLoader.fromURL(schemaURL);
    this.validator = factory.getValidator();
    this.yamlReader = new ObjectMapper(new YAMLFactory());
  }

  public void validateBySchema(String content) throws DevFileFormatException {
    ProcessingReport report;
    try {
      final JsonNode data = yamlReader.readTree(content);
      report = validator.validate(schema, data);
    } catch (IOException | ProcessingException e) {
      throw new DevFileFormatException(
          String.format("Unable to validate devfile. Error: %s" + e.getMessage()));
    }
    if (!report.isSuccess()) {
      StringBuilder sb = new StringBuilder();
      report.forEach(
          jsonError -> {
            if (jsonError.getLogLevel() == LogLevel.ERROR
                || jsonError.getLogLevel() == LogLevel.FATAL) {
              sb.append(String.format("[%s] ", jsonError.getMessage()));
            }
          });
      throw new DevFileFormatException(
          String.format("Devfile schema validation failed. Errors: %s", sb.toString()));
    }
  }
}
