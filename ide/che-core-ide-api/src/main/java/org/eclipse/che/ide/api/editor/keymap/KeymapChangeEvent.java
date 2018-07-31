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
package org.eclipse.che.ide.api.editor.keymap;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event type for change in keymap preference.
 *
 * @author "Mickaël Leduque"
 */
public class KeymapChangeEvent extends GwtEvent<KeymapChangeHandler> {
  /** Type instance for the event. */
  public static final Type<KeymapChangeHandler> TYPE = new Type<>();

  /** The key of the new keymap. */
  private final String keymapKey;

  /**
   * Creates a new keymap change event
   *
   * @param keymapKey the new keymap
   */
  public KeymapChangeEvent(final String keymapKey) {
    this.keymapKey = keymapKey;
  }

  @Override
  public Type<KeymapChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final KeymapChangeHandler handler) {
    handler.onKeymapChanged(this);
  }

  /**
   * Returns the keymap key.
   *
   * @return the keymap key
   */
  public String getKeymapKey() {
    return keymapKey;
  }
}
