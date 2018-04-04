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
package org.eclipse.che.api.languageserver.registry;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LanguageServerDescription {
  private final String id;
  private final List<String> languageIds;
  private final List<DocumentFilter> documentFilters;

  /**
   * The file name patters, format described there {@link
   * java.nio.file.FileSystem#getPathMatcher(String)}
   */
  private List<String> fileWatchPatterns = emptyList();

  public LanguageServerDescription(
      String id, List<String> languageIds, List<DocumentFilter> documentFilters) {
    this(id, languageIds, documentFilters, Collections.emptyList());
  }

  public LanguageServerDescription(
      String id,
      List<String> languageIds,
      List<DocumentFilter> documentFilters,
      List<String> fileWatchPatterns) {
    this.id = id;
    this.languageIds = languageIds;
    this.documentFilters = documentFilters;
    this.fileWatchPatterns = fileWatchPatterns;
  }

  public String getId() {
    return id;
  }

  public List<String> getLanguageIds() {
    return languageIds;
  }

  public List<DocumentFilter> getDocumentFilters() {
    return documentFilters;
  }

  public List<String> getFileWatchPatterns() {
    return fileWatchPatterns;
  }

  /** @param fileWatchPatterns must not be null */
  public void setFileWatchPatterns(List<String> fileWatchPatterns) {
    this.fileWatchPatterns = new ArrayList<>(fileWatchPatterns);
  }
}
