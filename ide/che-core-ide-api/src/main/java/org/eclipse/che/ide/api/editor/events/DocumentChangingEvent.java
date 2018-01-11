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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;

public class DocumentChangingEvent extends GwtEvent<DocumentChangingHandler> {

  /** The type instance for this event. */
  public static final Type<DocumentChangingHandler> TYPE = new Type<>();

  /** The document handle */
  private final DocumentHandle document;

  /** The document offset */
  private final int offset;

  /** Length of the replaced document text */
  private final int length;

  /** Text inserted into the document */
  private final String text;

  private final int removedCharCount;

  public DocumentChangingEvent(
      final DocumentHandle document,
      final int offset,
      final int length,
      final String text,
      int removedCharCount) {
    this.offset = offset;
    this.length = length;
    this.text = text;
    this.document = document;
    this.removedCharCount = removedCharCount;
  }

  @Override
  public Type<DocumentChangingHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final DocumentChangingHandler handler) {
    handler.onDocumentChanging(this);
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public String getText() {
    return text;
  }

  public DocumentHandle getDocument() {
    return document;
  }

  public int getRemoveCharCount() {
    return removedCharCount;
  }
}
