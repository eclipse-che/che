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

/** Overlay on the orion JS Annotation objects. */
public class OrionAnnotationOverlay extends JavaScriptObject {

  /** JSO mandated protected constructor. */
  protected OrionAnnotationOverlay() {}

  public final native String getType() /*-{
        return this.type;
    }-*/;

  public final native void setType(String type) /*-{
        this.type = type;
    }-*/;

  public final native int getStart() /*-{
        return this.start;
    }-*/;

  public final native void setStart(int offset) /*-{
        this.start = offset;
    }-*/;

  public final native int getEnd() /*-{
        return this.end;
    }-*/;

  public final native void setEnd(int offset) /*-{
        this.end = offset;
    }-*/;

  public final native String getHtml() /*-{
        return this.html;
    }-*/;

  public final native void setHtml(String html) /*-{
        this.html = html;
    }-*/;

  public final native String getTitle() /*-{
        return this.title;
    }-*/;

  public final native void setTitle(String title) /*-{
        this.title = title;
    }-*/;

  public final native OrionStyleOverlay getStyle() /*-{
        return this.style;
    }-*/;

  public final native void setStyle(OrionStyleOverlay style) /*-{
        this.style = style;
    }-*/;

  public final native OrionStyleOverlay getOverviewStyle() /*-{
        return this.overviewStyle;
    }-*/;

  public final native void setOverviewStyle(OrionStyleOverlay style) /*-{
        this.overviewStyle = style;
    }-*/;

  public final native OrionStyleOverlay getRangeStyle() /*-{
        return this.rangeStyle;
    }-*/;

  public final native void setRangeStyle(OrionStyleOverlay style) /*-{
        this.rangeStyle = style;
    }-*/;

  public final native OrionStyleOverlay getLineStyle() /*-{
        return this.lineStyle;
    }-*/;

  public final native void setLineStyle(OrionStyleOverlay style) /*-{
        this.lineStyle = style;
    }-*/;

  public static native OrionAnnotationOverlay create() /*-{
        return {};
    }-*/;
}
