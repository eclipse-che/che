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
package org.eclipse.che.plugin.pullrequest.client.dialogs.paste;

import com.google.gwt.event.shared.GwtEvent;

/** {@link GwtEvent} class for paste events. */
public class PasteEvent extends GwtEvent<PasteHandler> {
  /** The type of the event. */
  public static Type<PasteHandler> TYPE = new Type<PasteHandler>();

  @Override
  public Type<PasteHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final PasteHandler handler) {
    handler.onPaste(this);
  }
}
