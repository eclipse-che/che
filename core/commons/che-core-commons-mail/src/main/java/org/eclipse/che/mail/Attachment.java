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
package org.eclipse.che.mail;

import java.util.Objects;

/**
 * Describing e-mail attachment.
 *
 * @author Igor Vinokur
 * @author Alexander Garagatyi
 */
public class Attachment {
  private String content;
  private String contentId;
  private String fileName;

  /** Base-64 encoded string that represents attachment content. */
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Attachment withContent(String content) {
    this.content = content;
    return this;
  }

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public Attachment withContentId(String contentId) {
    this.contentId = contentId;
    return this;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Attachment withFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Attachment)) return false;
    Attachment that = (Attachment) o;
    return Objects.equals(getContent(), that.getContent())
        && Objects.equals(getContentId(), that.getContentId())
        && Objects.equals(getFileName(), that.getFileName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getContent(), getContentId(), getFileName());
  }

  @Override
  public String toString() {
    return "Attachment{"
        + "content='"
        + content
        + '\''
        + ", contentId='"
        + contentId
        + '\''
        + ", fileName='"
        + fileName
        + '\''
        + '}';
  }
}
