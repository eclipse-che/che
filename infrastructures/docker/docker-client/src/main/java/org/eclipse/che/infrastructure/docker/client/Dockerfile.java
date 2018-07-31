/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @author andrew00x */
public class Dockerfile {
  // TODO: docs about template features
  static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\$[^\\$^\\$]+\\$");
  static final Pattern TEMPLATE_DEFAULT_PATTERN = Pattern.compile("(.*):-(.*)");
  // Docker file templates may contains following constructions:
  // parameter[= > < >= <= !=]condition?expression1:expression2
  // Parameter is replaced with expression1 if value of parameter is matched to condition and
  // replaced with expression2 otherwise
  static final Pattern TEMPLATE_CONDITIONAL_PATTERN =
      Pattern.compile("([^><=!\\s]+)([><=!]+)?(.*)?\\?(.*)?:(.*)?");

  private List<String> lines;
  private Map<String, Object> parameters;
  private List<DockerImage> images;

  public List<String> getLines() {
    if (lines == null) {
      lines = new LinkedList<>();
    }
    return lines;
  }

  public List<DockerImage> getImages() {
    if (images == null) {
      images = new LinkedList<>();
    }
    return images;
  }

  public Map<String, Object> getParameters() {
    if (parameters == null) {
      parameters = new LinkedHashMap<>();
    }
    return parameters;
  }

  public void writeDockerfile(java.io.File path) throws IOException {
    try (FileWriter output = new FileWriter(path)) {
      writeDockerfile(output);
    }
  }

  public void writeDockerfile(Appendable output) throws IOException {
    StringBuilder buf = null;
    for (String line : getLines()) {
      boolean isEmptyOutput = false;
      final Matcher matcher = TEMPLATE_PATTERN.matcher(line);
      if (matcher.find()) {
        int start = 0;
        if (buf == null) {
          buf = new StringBuilder();
        } else {
          buf.setLength(0);
        }
        do {
          buf.append(line.substring(start, matcher.start()));
          final String template = line.substring(matcher.start(), matcher.end());
          final String expression = line.substring(matcher.start() + 1, matcher.end() - 1);

          String parameterName;
          Matcher subMatcher;
          Object value;
          // we've a default pattern so extract parameter name
          if ((subMatcher = TEMPLATE_DEFAULT_PATTERN.matcher(expression)).matches()) {
            parameterName = subMatcher.group(1);
            value = getParameters().get(parameterName);
            if (value == null) {
              value = subMatcher.group(2);
            }
          } else if ((subMatcher = TEMPLATE_CONDITIONAL_PATTERN.matcher(expression)).matches()) {
            parameterName = subMatcher.group(1);
            final String operator = subMatcher.group(2);
            final String condition = subMatcher.group(3);
            final String ifTrue = subMatcher.group(4);
            final String ifFalse = subMatcher.group(5);
            if (operator == null && condition.isEmpty()) {
              // parameter?expression1:expression2
              value = getParameters().get(parameterName);
              value =
                  (value instanceof Boolean && (Boolean) value)
                          || (value instanceof String && Boolean.parseBoolean((String) value))
                          || (value instanceof Number && 0 != ((Number) value).intValue())
                      ? ifTrue
                      : ifFalse;
            } else {
              value = getParameters().get(parameterName);
              if ("=".equals(operator)) {
                value = condition.equals(value) ? ifTrue : ifFalse;
              } else if ("!=".equals(operator)) {
                value = condition.equals(value) ? ifFalse : ifTrue;
              } else if (">".equals(operator)) {
                value = String.valueOf(value).compareTo(condition) > 0 ? ifTrue : ifFalse;
              } else if ("<".equals(operator)) {
                value = condition.compareTo(String.valueOf(value)) > 0 ? ifTrue : ifFalse;
              } else if (">=".equals(operator)) {
                value = condition.compareTo(String.valueOf(value)) <= 0 ? ifTrue : ifFalse;
              } else if ("<=".equals(operator)) {
                value = condition.compareTo(String.valueOf(value)) >= 0 ? ifTrue : ifFalse;
              }
            }
            if (!isEmptyOutput && (value == null || value.toString().length() == 0)) {
              isEmptyOutput = true;
            }
          } else {
            // not a default expression, so expression is the parameter name
            parameterName = expression;
            value = getParameters().get(parameterName);
          }
          if (value == null) {
            value = template;
          }
          buf.append(String.valueOf(value));
          start = matcher.end();
        } while (matcher.find());
        final String subLine = line.substring(start);
        if (isEmptyOutput && subLine.length() == 0) {
          continue;
        }
        buf.append(subLine);
        output.append(buf);
      } else {
        output.append(line);
      }
      output.append('\n');
    }
  }
}
