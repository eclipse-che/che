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
