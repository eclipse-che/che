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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.GwtEvent;

/** Event that notifies of file content changes. */
public class FileContentUpdateEvent extends GwtEvent<FileContentUpdateHandler> {
  /** The event type. */
  public static Type<FileContentUpdateHandler> TYPE = new Type<>();

  /** The path to the file that is updated. */
  private final String filePath;

  /** Encoded content. */
  private String modificationStamp;

  /**
   * Constructor.
   *
   * @param filePath the path of the file that changed
   */
  public FileContentUpdateEvent(final String filePath) {
    this.filePath = filePath;
  }

  public FileContentUpdateEvent(final String filePath, final String contentStamp) {
    this.filePath = filePath;
    this.modificationStamp = contentStamp;
  }

  @Override
  public Type<FileContentUpdateHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(FileContentUpdateHandler handler) {
    handler.onFileContentUpdate(this);
  }

  /**
   * Returns the path to the file that had changes.
   *
   * @return the path
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * Returns content's stamp of the file that had changes.
   *
   * @return the path
   */
  public String getModificationStamp() {
    return modificationStamp;
  }
}
