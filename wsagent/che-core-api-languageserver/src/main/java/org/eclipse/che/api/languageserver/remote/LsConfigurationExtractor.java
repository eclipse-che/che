/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.remote;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.registry.DocumentFilter;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;

/**
 * This class is responsible for language server description extraction out of server attributes
 * map. It is expected that there will be specific attribute named <code>config</code> that will
 * contain serialized json data that represents all language server configuration data. Structure of
 * json corresponds to {@link LanguageServerDescription} class with all aggregated classes
 */
@Singleton
class LsConfigurationExtractor {
  private final JsonParser jsonParser;

  @Inject
  LsConfigurationExtractor(JsonParser jsonParser) {
    this.jsonParser = jsonParser;
  }

  LanguageServerDescription extract(Map<String, String> attributes) {
    String config = attributes.get("config");
    JsonObject configJsonObject = jsonParser.parse(config).getAsJsonObject();
    String id = getId(configJsonObject);
    List<String> languageIds = getLanguageIds(configJsonObject);
    List<String> fileWatchPatterns = getFileWatchPatterns(configJsonObject);
    List<DocumentFilter> documentFilters = getDocumentFilters(configJsonObject);

    if (languageIds.isEmpty()) {
      throw new IllegalStateException(
          "Language server is not properly configured in workspace configuration: language ids list is empty");
    }
    return new LanguageServerDescription(id, languageIds, documentFilters, fileWatchPatterns);
  }

  private String getId(JsonObject jsonObject) {
    return !jsonObject.has("id") ? null : jsonObject.get("id").getAsString();
  }

  private List<String> getLanguageIds(JsonObject jsonObject) {
    if (!jsonObject.has("languageIds")) {
      return emptyList();
    }

    JsonArray languageIdsJsonArray = jsonObject.get("languageIds").getAsJsonArray();
    int size = languageIdsJsonArray.size();
    List<String> languageIds = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      String languageId = languageIdsJsonArray.get(i).getAsString();
      languageIds.add(languageId);
    }

    return unmodifiableList(languageIds);
  }

  private List<String> getFileWatchPatterns(JsonObject jsonObject) {
    if (!jsonObject.has("fileWatchPatterns")) {
      return emptyList();
    }

    JsonArray fileWatchPatternsJsonArray = jsonObject.get("fileWatchPatterns").getAsJsonArray();
    int size = fileWatchPatternsJsonArray.size();
    List<String> fileWatchPatterns = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      String fileWatchPattern = fileWatchPatternsJsonArray.get(i).getAsString();
      fileWatchPatterns.add(fileWatchPattern);
    }

    return unmodifiableList(fileWatchPatterns);
  }

  private List<DocumentFilter> getDocumentFilters(JsonObject jsonObject) {
    if (!jsonObject.has("documentFilters")) {
      return emptyList();
    }
    JsonArray documentFiltersJsonArray = jsonObject.get("documentFilters").getAsJsonArray();

    int size = documentFiltersJsonArray.size();
    List<DocumentFilter> documentFilters = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      JsonObject documentFilterJsonObject = documentFiltersJsonArray.get(i).getAsJsonObject();

      String pathRegex;
      if (documentFilterJsonObject.has("pathRegex")) {
        pathRegex = documentFilterJsonObject.get("pathRegex").getAsString();
      } else {
        pathRegex = null;
      }

      String languageId;
      if (documentFilterJsonObject.has("languageId")) {
        languageId = documentFilterJsonObject.get("languageId").getAsString();
      } else {
        languageId = null;
      }

      String schema;
      if (documentFilterJsonObject.has("scheme")) {
        schema = documentFilterJsonObject.get("scheme").getAsString();
      } else {
        schema = null;
      }

      DocumentFilter documentFilter = new DocumentFilter(languageId, pathRegex, schema);
      documentFilters.add(documentFilter);
    }

    return unmodifiableList(documentFilters);
  }
}
