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
import com.google.gwt.core.client.JsArrayString;

/**
 * The "Service attributes" for registering orion 'orion.edit.highlight' service. See <a
 * href="https://wiki.eclipse.org/Orion/Documentation/Developer_Guide/Plugging_into_the_editor#orion.edit.highlighter">Orion
 * documentation</a> for details.
 *
 * @author Sven Efftinge
 */
public class OrionHighlightingConfigurationOverlay extends JavaScriptObject {

  protected OrionHighlightingConfigurationOverlay() {}

  public static native OrionHighlightingConfigurationOverlay create() /*-{
        return {};
    }-*/;

  public final native void setId(String newValue) /*-{
        this.id = newValue;
    }-*/;

  public final void setContentTypes(String... theContentTypes) {
    JsArrayString arr = JavaScriptObject.createArray().cast();
    for (String value : theContentTypes) {
      arr.push(value);
    }
    setContentTypes(arr);
  }

  protected final native void setContentTypes(JsArrayString theContentTypes) /*-{
        this.contentTypes = theContentTypes;
    }-*/;

  /**
   * The proper grammar description. See <a
   * href="https://wiki.eclipse.org/Orion/Documentation/Developer_Guide/Plugging_into_the_editor#Pattern_objects">Orion
   * documentation</a> for details.
   *
   * @param patternsAsJsonArray
   */
  public final native void setPatterns(String patternsAsJsonArray) /*-{
        this.patterns = eval(patternsAsJsonArray);
  }-*/;

  public final native JsArray<JavaScriptObject> getPatterns() /*-{
      return this.patterns;
  }-*/;
}
