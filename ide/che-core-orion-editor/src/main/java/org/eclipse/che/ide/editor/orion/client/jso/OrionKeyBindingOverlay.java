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

public class OrionKeyBindingOverlay extends JavaScriptObject {

  protected OrionKeyBindingOverlay() {}

  public static final native OrionKeyBindingOverlay createKeyStroke(
      String keyCode,
      boolean modifier1,
      boolean modifier2,
      boolean modifier3,
      boolean modifier4,
      String type,
      JavaScriptObject keyBindingModule) /*-{
        return new keyBindingModule.KeyStroke(keyCode, modifier1, modifier2, modifier3, modifier4, type);
    }-*/;

  public static final native OrionKeyBindingOverlay createKeySequence(
      JsArray<OrionKeyStrokeOverlay> keys, JavaScriptObject keyBindingModule) /*-{
        return new keyBindingModule.KeySequence(keys);
    }-*/;
}
