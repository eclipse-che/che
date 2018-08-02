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
package org.eclipse.che.api.editor.server.impl;

import static java.lang.String.format;
import static org.eclipse.che.api.project.shared.dto.EditorChangesDto.Type.INSERT;
import static org.eclipse.che.api.project.shared.dto.EditorChangesDto.Type.REMOVE;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;

/**
 * In-memory implementation of working copy for opened editor on client.
 *
 * @author Roman Nikitenko
 */
public class EditorWorkingCopy {
  private String path;
  private String projectPath;
  private byte[] content;

  /**
   * Creates a working copy for opened editor on client.
   *
   * @param path path to the persistent working copy
   * @param projectPath path to the project which contains the opened editor on client
   * @param content the content of the original file to creating working copy
   */
  public EditorWorkingCopy(String path, String projectPath, byte[] content) {
    this.path = path;
    this.projectPath = projectPath;
    this.content = Arrays.copyOf(content, content.length);
  }

  /**
   * Gets content of the working copy as bytes.
   *
   * @return content ot the working copy
   */
  public byte[] getContentAsBytes() {
    if (content == null) {
      content = new byte[0];
    }
    return Arrays.copyOf(content, content.length);
  }

  /**
   * Gets content of the working copy as String decoding bytes.
   *
   * @return content ot the working copy
   */
  public String getContentAsString() {
    return new String(getContentAsBytes());
  }

  /**
   * Gets content of the working copy.
   *
   * @return content ot the working copy
   */
  public InputStream getContent() {
    return new ByteArrayInputStream(getContentAsBytes());
  }

  /**
   * Updates content of the working copy.
   *
   * @param content content
   * @return current working copy after updating content
   */
  EditorWorkingCopy updateContent(byte[] content) {
    this.content = content;
    return this;
  }

  /**
   * Updates content of the working copy.
   *
   * @param content content
   * @return current working copy after updating content
   */
  EditorWorkingCopy updateContent(String content) {
    updateContent(content.getBytes());
    return this;
  }

  /**
   * Updates content of the working copy.
   *
   * @param content content
   * @return current working copy after updating content
   */
  EditorWorkingCopy updateContent(InputStream content) throws ServerException {
    byte[] bytes;
    try {
      bytes = ByteStreams.toByteArray(content);
    } catch (IOException e) {
      throw new ServerException(
          format(
              "Can not update the content of '%s'. The reason is: %s", getPath(), e.getMessage()));
    }
    return updateContent(bytes);
  }

  /**
   * Updates content of the working copy by applying editor content changes.
   *
   * @param changes contains editor content changes
   */
  void applyChanges(EditorChangesDto changes) {
    synchronized (this) {
      String text = changes.getText();
      int offset = changes.getOffset();
      int removedCharCount = changes.getRemovedCharCount();

      String newContent = null;
      String oldContent = getContentAsString();
      EditorChangesDto.Type type = changes.getType();
      if (type == INSERT) {
        newContent = new StringBuilder(oldContent).insert(offset, text).toString();
      }

      if (type == REMOVE && removedCharCount > 0) {
        newContent =
            new StringBuilder(oldContent).delete(offset, offset + removedCharCount).toString();
      }

      if (newContent != null) {
        updateContent(newContent);
      }
    }
  }

  /** Returns the path to the persistent working copy */
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /** Returns the path to the project which contains the opened editor on client */
  public String getProjectPath() {
    return projectPath;
  }

  public void setProjectPath(String projectPath) {
    this.projectPath = projectPath;
  }
}
