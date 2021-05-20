/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.eclipse.che.api.workspace.server.devfile.exception.OverrideParameterException;

/**
 * Applies override properties to provided devfile {@link JsonNode}. The following set of rules will
 * be used during object modification:
 *
 * <ul>
 *   <li>Only allowed top-level fields can be altered: apiVersion, metadata, project, attributes.
 *   <li>The absent segment will be created as an empty object and next segment will be added as a
 *       field of it
 *   <li>The property identifier cannot ends with an array type reference
 *   <li>The property identifier for object in an array should contain valid object name, error will
 *       be thrown otherwise.
 * </ul>
 */
public class OverridePropertiesApplier {

  private final List<String> allowedFirstSegments =
      asList("apiVersion", "metadata", "projects", "attributes");

  public JsonNode applyPropertiesOverride(
      JsonNode devfileNode, Map<String, String> overrideProperties)
      throws OverrideParameterException {
    for (Map.Entry<String, String> entry : overrideProperties.entrySet()) {
      String[] pathSegments = parseSegments(entry.getKey());
      if (pathSegments.length < 1) {
        continue;
      }
      validateFirstSegment(pathSegments);
      String lastSegment = pathSegments[pathSegments.length - 1];
      JsonNode currentNode = devfileNode;
      Iterator<String> pathSegmentsIterator = Stream.of(pathSegments).iterator();
      do {
        String currentSegment = pathSegmentsIterator.next();
        JsonNode nextNode = currentNode.path(currentSegment);
        if (nextNode.isMissingNode() && pathSegmentsIterator.hasNext()) {
          // no such intermediate node, let's create it as a empty object
          currentNode = ((ObjectNode) currentNode).putObject(currentSegment);
          continue;
        } else if (nextNode.isArray()) {
          // ok we have reference to array, so need to make sure that we have next path segment
          // and then try to retrieve it from array
          if (!pathSegmentsIterator.hasNext()) {
            throw new OverrideParameterException(
                format(
                    "Override property reference '%s' points to an array type object. Please add an item qualifier by name.",
                    entry.getKey()));
          }
          // retrieve object by name from array
          String arrayObjectName = pathSegmentsIterator.next();
          currentNode = findNodeByName((ArrayNode) nextNode, arrayObjectName, currentSegment);
          continue;
        } else {
          // because it's impossible to change value of the current Json node,
          // so to set value, we should be 1 level up and do put(key, value),
          // so not set latest segment as an current node
          if (pathSegmentsIterator.hasNext()) {
            currentNode = nextNode;
          }
        }
      } while (pathSegmentsIterator.hasNext());
      // end of path segments reached, now we can set value
      ((ObjectNode) currentNode).put(lastSegment, entry.getValue());
    }
    return devfileNode;
  }

  private String[] parseSegments(String key) {
    return key.startsWith("attributes.")
        // for attributes we treat the rest as a attribute name so just need only 2 parts
        ? key.split("\\.", 2)
        : key.split("\\.");
  }

  private void validateFirstSegment(String[] pathSegments) throws OverrideParameterException {
    if (!allowedFirstSegments.contains(pathSegments[0])) {
      throw new OverrideParameterException(
          format(
              "Override path '%s' starts with an unsupported field pointer. Supported fields are %s.",
              join(".", pathSegments),
              allowedFirstSegments.stream().collect(joining("\",\"", "{\"", "\"}"))));
    }
  }

  private JsonNode findNodeByName(ArrayNode parentNode, String searchName, String parentNodeName)
      throws OverrideParameterException {
    return StreamSupport.stream(parentNode.spliterator(), false)
        .filter(node -> node.path("name").asText().equals(searchName))
        .findFirst()
        .orElseThrow(
            () ->
                new OverrideParameterException(
                    format(
                        "Cannot apply override: object with name '%s' not found in array of %s.",
                        searchName, parentNodeName)));
  }
}
