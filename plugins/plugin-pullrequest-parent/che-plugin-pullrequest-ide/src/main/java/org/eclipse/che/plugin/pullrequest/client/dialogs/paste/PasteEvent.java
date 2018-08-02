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
