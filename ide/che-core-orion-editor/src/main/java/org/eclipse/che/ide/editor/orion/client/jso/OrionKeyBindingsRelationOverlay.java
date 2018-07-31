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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/** Class that bind actionId and keyBindings for this action */
public class OrionKeyBindingsRelationOverlay extends JavaScriptObject {
  protected OrionKeyBindingsRelationOverlay() {}

  /**
   * Get actionId
   *
   * @return actionId
   */
  public final native String getActionId() /*-{
        return this.actionID;
    }-*/;

  /**
   * Get keybinBings for action
   *
   * @return keybindings
   */
  public final native OrionKeyBindingOverlay getKeyBindings() /*-{
        return this.keyBinding;
    }-*/;
}
