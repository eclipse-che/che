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
import org.eclipse.che.ide.api.editor.document.Document;

/** Event will be fired then document fully initialized in editor */
public class DocumentReadyEvent extends GwtEvent<DocumentReadyHandler> {

  /** The type instance for this event. */
  public static final Type<DocumentReadyHandler> TYPE = new Type<>();

  /** The document. */
  private final Document document;

  /** @param document the related initialized document */
  public DocumentReadyEvent(final Document document) {
    this.document = document;
  }

  @Override
  public Type<DocumentReadyHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final DocumentReadyHandler handler) {
    handler.onDocumentReady(this);
  }

  public Document getDocument() {
    return document;
  }
}
