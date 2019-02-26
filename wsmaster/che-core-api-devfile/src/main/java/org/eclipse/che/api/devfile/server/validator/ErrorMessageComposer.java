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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Helps to convert json schema validation result {@link ProcessingReport} into an error message
 * string.
 */
public class ErrorMessageComposer {

  /**
   * Parses {@link ProcessingReport} into an error string. Each processing message is recursively
   * parsed to extract nested errors if any.
   *
   * @param report Schema validation processing report
   * @return composite error string
   */
  public String prepareErrorMessage(ProcessingReport report) {
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
      node.forEach(n -> recursivelyFindErrors(n, messages));
    } else {
      JsonNode reports = node.get("reports");
      if (reports != null) {
        messages.add(getMessage(node));
        reports.forEach(n -> recursivelyFindErrors(n, messages));
      } else {
        String pointer = "/devfile" + getPointer(node);
        messages.add(pointer + " " + getMessage(node));
      }
    }
  }

  private String getPointer(JsonNode node) {
    return node.get("instance").get("pointer").asText();
  }

  private String getMessage(JsonNode node) {
    return node.get("message").asText();
  }
}
