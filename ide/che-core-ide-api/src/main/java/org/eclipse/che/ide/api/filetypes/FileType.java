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
package org.eclipse.che.ide.api.filetypes;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.EMPTY_SET;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.che.commons.annotation.Nullable;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * FileType is meta information about file. It's contains
 *
 * <ul>
 *   <li><code>image</code> - image resource associated with file
 *   <li><code>extension</code> - extension associated with file
 *   <li><code>namePatterns</code> - set of name patterns
 * </ul>
 *
 * @author Evgen Vidolob
 */
public class FileType {

  private SVGResource image;
  private String extension;
  private Set<String> namePatterns;

  public FileType(SVGResource image, String extension) {
    this(image, extension, null);
  }

  public FileType(SVGResource image, String extension, String namePattern) {
    this.image = image;
    this.extension = extension;
    this.namePatterns = new HashSet<>();
    addNamePattern(namePattern);
  }

  /** Returns the image resource associated with file */
  public SVGResource getImage() {
    return image;
  }

  /** Returns the extension associated with file */
  public String getExtension() {
    return extension;
  }

  /**
   * Returns some element of the name patterns set or {@code null} if the set is empty.
   *
   * @deprecated FileType can contain a few name patterns, so use {@link #getNamePatterns()} instead
   */
  @Nullable
  public String getNamePattern() {
    return namePatterns.isEmpty() ? null : namePatterns.iterator().next();
  }

  /** Returns the name patterns set describing the file type */
  public Set<String> getNamePatterns() {
    return namePatterns.isEmpty() ? EMPTY_SET : new HashSet<>(namePatterns);
  }

  /** Sets image associated with the file type */
  public void setImage(SVGResource image) {
    this.image = image;
  }

  /** Sets extension associated with the file type */
  public void setExtension(String extension) {
    this.extension = extension;
  }

  /**
   * Adds name pattern describing the file type
   *
   * @deprecated FileType can contain a few name patterns, so use {@link #addNamePattern(String)}
   *     instead
   */
  public void setNamePattern(String namePattern) {
    addNamePattern(namePattern);
  }

  /**
   * Adds name pattern describing the file type
   *
   * @param namePattern pattern for adding
   * @return {@code true} if the pattern was added and {@code false} if given pattern is illegal
   *     ({@code null} or empty) either if it is already present
   */
  public boolean addNamePattern(String namePattern) {
    if (isNullOrEmpty(namePattern)) {
      return false;
    }

    return namePatterns.add(namePattern);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileType fileType = (FileType) o;

    if (!Objects.equals(this.extension, fileType.extension)) {
      return false;
    } else {
      return getNamePatterns().equals(fileType.getNamePatterns());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(extension, getNamePatterns());
  }
}
