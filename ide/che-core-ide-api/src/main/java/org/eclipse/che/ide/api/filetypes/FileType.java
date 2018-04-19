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
package org.eclipse.che.ide.api.filetypes;

import java.util.Objects;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * FileType is meta information about file. It's contains
 *
 * <ul>
 *   <li><code>image</code> - image resource associated with file
 *   <li><code>extension</code> - extension associated with file
 *   <li><code>namePattern</code> - name pattern
 * </ul>
 *
 * @author Evgen Vidolob
 */
public class FileType {

  private SVGResource image;

  private String extension;

  private String namePattern;

  public void setImage(SVGResource image) {
    this.image = image;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

  public void setNamePattern(String namePattern) {
    this.namePattern = namePattern;
  }

  public FileType(SVGResource image, String extension) {
    this(image, extension, null);
  }

  public FileType(SVGResource image, String extension, String namePattern) {
    this.image = image;
    this.extension = extension;
    this.namePattern = namePattern;
  }

  /** @return the extension */
  public String getExtension() {
    return extension;
  }

  /** @return the namePatterns */
  public String getNamePattern() {
    return namePattern;
  }

  /** @return the SVG resource */
  public SVGResource getImage() {
    return image;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FileType fileType = (FileType) o;

    if (extension != null ? !extension.equals(fileType.extension) : fileType.extension != null)
      return false;
    if (namePattern != null
        ? !namePattern.equals(fileType.namePattern)
        : fileType.namePattern != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(extension, namePattern);
  }
}
