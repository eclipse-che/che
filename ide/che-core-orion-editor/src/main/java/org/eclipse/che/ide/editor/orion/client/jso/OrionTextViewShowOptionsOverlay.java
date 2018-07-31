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

public class OrionTextViewShowOptionsOverlay extends JavaScriptObject {

  public enum ViewAnchorValue {
    TOP("top"),
    BOTTOM("bottom"),
    CENTER("center");

    private final String value;

    ViewAnchorValue(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  protected OrionTextViewShowOptionsOverlay() {}

  public final native String getScrollPolicy() /*-{
        return this.scrollPolicy;
    }-*/;

  public final native void setScrollPolicy(final String newValue) /*-{
        this.scrollPolicy = newValue;
    }-*/;

  public final native String getSelectionAnchor() /*-{
        return this.selectionAnchor;
    }-*/;

  public final native void setSelectionAnchor(final String newValue) /*-{
        this.selectionAnchor = newValue;
    }-*/;

  public final native String getViewAnchor() /*-{
        return this.viewAnchor;
    }-*/;

  public final native void setViewAnchor(final String newValue) /*-{
        this.viewAnchor = newValue;
    }-*/;

  public final native String getViewAnchorOffset() /*-{
        return this.viewAnchorOffset;
    }-*/;

  public final native void setViewAnchorOffset(final String newValue) /*-{
        this.viewAnchorOffset = newValue;
    }-*/;

  public static native OrionTextViewShowOptionsOverlay create() /*-{
        return {};
    }-*/;
}
