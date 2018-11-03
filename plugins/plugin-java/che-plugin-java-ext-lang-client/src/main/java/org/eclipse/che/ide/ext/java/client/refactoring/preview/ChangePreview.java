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
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

/**
 * Describes preview information about the change of the refactoring operation.
 *
 * @author Valeriy Svydenko
 */
public class ChangePreview {
  private String oldContent;
  private String newContent;
  private String fileName;

  /** @return part of the old content */
  public String getOldContent() {
    return oldContent;
  }

  public void setOldContent(String oldContent) {
    this.oldContent = oldContent;
  }

  /** @return part of the new content */
  public String getNewContent() {
    return newContent;
  }

  public void setNewContent(String newContent) {
    this.newContent = newContent;
  }

  /** @return name of the file which has corresponding change */
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ChangePreview other = (ChangePreview) obj;
    if (this.oldContent == null) {
      if (other.oldContent != null) return false;
    } else if (!this.oldContent.equals(other.oldContent)) return false;
    if (this.newContent == null) {
      if (other.newContent != null) return false;
    } else if (!this.newContent.equals(other.newContent)) return false;
    if (this.fileName == null) {
      if (other.fileName != null) return false;
    } else if (!this.fileName.equals(other.fileName)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.fileName == null) ? 0 : this.fileName.hashCode());
    result = prime * result + ((this.oldContent == null) ? 0 : this.oldContent.hashCode());
    result = prime * result + ((this.newContent == null) ? 0 : this.newContent.hashCode());
    return result;
  }
}
