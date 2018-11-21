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

import static java.lang.String.format;

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
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/** Validates YAML content against given JSON schema. */
@Singleton
public class DevFileSchemaValidator {

  private JsonValidator validator;
  private JsonNode schema;
  private ObjectMapper yamlReader;

  @Inject
  public DevFileSchemaValidator(@Named("che.devfile.schema.file_location") String schemaFile)
      throws IOException {
    final URL schemaURL = getClass().getClassLoader().getResource(schemaFile);
    if (schemaURL == null) {
      throw new IOException("Devfile schema is not found at specified path:" + schemaFile);
    }
    this.schema = JsonLoader.fromURL(schemaURL);
    this.validator = JsonSchemaFactory.byDefault().getValidator();
    this.yamlReader = new ObjectMapper(new YAMLFactory());
  }

  public void validateBySchema(String yamlContent) throws DevFileFormatException {
    ProcessingReport report;
    try {
      final JsonNode data = yamlReader.readTree(yamlContent);
      report = validator.validate(schema, data);
    } catch (IOException | ProcessingException e) {
      throw new DevFileFormatException("Unable to validate devfile. Error: " + e.getMessage());
    }
    if (!report.isSuccess()) {
      StringBuilder sb = new StringBuilder();
      StreamSupport.stream(report.spliterator(), false)
          .filter(
              message ->
                  message.getLogLevel() == LogLevel.ERROR
                      || message.getLogLevel() == LogLevel.FATAL)
          .forEach(message -> sb.append(format("[%s] ", message.getMessage())));
      throw new DevFileFormatException(format("Devfile schema validation failed. Errors: %s", sb));
    }
  }
}
