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
package org.eclipse.che.api.languageserver.shared.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** @author Anatolii Bazko */
public class LanguageDescription {
  /** The language id. */
  private String languageId;
  /** The optional content types this language is associated with. */
  private String mimeType;
  /** The fileExtension this language is associated with. */
  private List<String> fileExtensions = Collections.emptyList();
  /** The file names this language is associated with. */
  private List<String> fileNames = Collections.emptyList();

  /**
   * The optional highlighting configuration to support client side syntax highlighting. The format
   * is client (editor) dependent.
   */
  private String highlightingConfiguration;

  public String getLanguageId() {
    return this.languageId;
  }

  public void setLanguageId(final String languageId) {
    this.languageId = languageId;
  }

  public String getMimeType() {
    return this.mimeType;
  }

  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  public List<String> getFileExtensions() {
    return this.fileExtensions;
  }

  /** @param fileExtensions must not be null */
  public void setFileExtensions(final List<String> fileExtensions) {
    this.fileExtensions = new ArrayList<>(fileExtensions);
  }

  public List<String> getFileNames() {
    return fileNames;
  }

  /** @param fileNames must not be null */
  public void setFileNames(List<String> fileNames) {
    this.fileNames = new ArrayList<>(fileNames);
  }

  public String getHighlightingConfiguration() {
    return this.highlightingConfiguration;
  }

  public void setHighlightingConfiguration(final String highlightingConfiguration) {
    this.highlightingConfiguration = highlightingConfiguration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LanguageDescription that = (LanguageDescription) o;
    return Objects.equals(languageId, that.languageId)
        && Objects.equals(mimeType, that.mimeType)
        && Objects.equals(fileExtensions, that.fileExtensions)
        && Objects.equals(fileNames, that.fileNames)
        && Objects.equals(highlightingConfiguration, that.highlightingConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(languageId, mimeType, fileExtensions, fileNames, highlightingConfiguration);
  }

  @Override
  public String toString() {
    return "LanguageDescriptionImpl{"
        + "languageId='"
        + languageId
        + '\''
        + ", mimeTypes="
        + mimeType
        + ", fileExtensions="
        + fileExtensions
        + ", fileNames="
        + fileNames
        + ", highlightingConfiguration='"
        + highlightingConfiguration
        + '\''
        + '}';
  }
}
