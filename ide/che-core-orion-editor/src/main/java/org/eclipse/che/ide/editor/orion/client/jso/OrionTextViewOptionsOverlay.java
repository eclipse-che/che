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

public class OrionTextViewOptionsOverlay extends JavaScriptObject {

  protected OrionTextViewOptionsOverlay() {}

  public final native OrionTextThemeOverlay getTheme() /*-{
        return this.theme;
    }-*/;

  public final native boolean isReadOnly() /*-{
        return this.readonly;
    }-*/;

  public final native void setReadOnly(final boolean newValue) /*-{
        this.readonly = newValue;
    }-*/;

  public final native boolean getWrapMode() /*-{
        return this.wrapMode;
    }-*/;

  public final native void setWrapMode(final boolean newValue) /*-{
        this.wrapMode = newValue;
    }-*/;

  public final native int getWrapOffset() /*-{
        return this.wrapOffset;
    }-*/;

  public final native void setWrapOffset(final int newValue) /*-{
        this.wrapOffset = newValue;
    }-*/;

  public final native boolean getTabMode() /*-{
        return this.tabMode;
    }-*/;

  public final native void setTabMode(final boolean newValue) /*-{
        this.tabMode = newValue;
    }-*/;

  public final native boolean getExpandTab() /*-{
        return this.expandTab;
    }-*/;

  public final native void setExpandTab(final boolean newValue) /*-{
        this.expandTab = newValue;
    }-*/;

  public final native int getTabSize() /*-{
        return this.tabSize;
    }-*/;

  public final native void setTabSize(final int newValue) /*-{
        this.tabSize = newValue;
    }-*/;

  public final native boolean getOverwriteMode() /*-{
        return this.overwriteMode;
    }-*/;

  public final native void setOverwriteMode(final boolean newValue) /*-{
        this.overwriteMode = newValue;
    }-*/;

  public final native boolean getWordWrap() /*-{
        return this.wordWrap;
    }-*/;

  public final native void setWordWrap(final boolean newValue) /*-{
        this.wordWrap = newValue;
    }-*/;
}
