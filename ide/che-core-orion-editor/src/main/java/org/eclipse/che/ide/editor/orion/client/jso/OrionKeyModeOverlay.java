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
import com.google.gwt.core.client.JsArray;

public class OrionKeyModeOverlay extends JavaScriptObject {

  protected OrionKeyModeOverlay() {}

  /* These two method should be modified to use an overlay type instead of JSO */
  public final native JsArray<OrionKeyBindingOverlay> getKeyBindings(String actionID) /*-{
        return this.getKeyBindings(actionID);
    }-*/;

  public final native void setKeyBinding(OrionKeyBindingOverlay keyBinding, String actionID) /*-{
        return this.setKeyBinding(keyBinding, actionID);
    }-*/;

  /* ------------------------------------------------------------------------- */

  public final native OrionTextViewOverlay getView() /*-{
        return this.getView();
    }-*/;

  public final native boolean isActive() /*-{
        return this.isActive();
    }-*/;

  public static final native OrionKeyModeOverlay getEmacsKeyMode(
      JavaScriptObject orionEmacsModuleObject, OrionTextViewOverlay textView) /*-{
        return new orionEmacsModuleObject.EmacsMode(textView);
    }-*/;

  public static final native OrionKeyModeOverlay getViKeyMode(
      JavaScriptObject orionViModuleObject, OrionTextViewOverlay textView) /*-{
        return new orionViModuleObject.VIMode(textView);
    }-*/;

  public static final native OrionKeyModeOverlay getCheCodeAssistMode(
      JavaScriptObject cheCodeAssistModule, OrionTextViewOverlay textView) /*-{
        return new cheCodeAssistModule.CheContentAssist(textView);
    }-*/;

  public static final native OrionKeyModeOverlay getDefaultKeyMode(
      OrionTextViewOverlay textView) /*-{
        var keyModes = textView.getKeyModes();
        return keyModes[0];
    }-*/;

  /**
   * Return list objects with relation actionId and keyBindings for this action
   *
   * @param textView text view
   * @return objects with relation actionId and keyBindings
   */
  public static final native JsArray<OrionKeyBindingsRelationOverlay> getKeyBindings_(
      OrionTextViewOverlay textView) /*-{
        var keyModes = textView.getKeyModes();
        var size = keyModes.length;
        return keyModes[size - 1]._keyBindings;
    }-*/;
}
