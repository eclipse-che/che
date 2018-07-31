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

/** Events that correspond to external completion requests. */
public class CompletionRequestEvent extends GwtEvent<CompletionRequestHandler> {

  /** The type instance for this event. */
  public static final Type<CompletionRequestHandler> TYPE = new Type<>();

  @Override
  public Type<CompletionRequestHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final CompletionRequestHandler handler) {
    handler.onCompletionRequest(this);
  }
}
