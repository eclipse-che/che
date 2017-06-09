/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/** Overlay on the orion JS Style objects. */
public class OrionStyleOverlay extends JavaScriptObject {

    /** JSO mandated protected constructor. */
    protected OrionStyleOverlay() {
    }

    public final native String getStyleClass() /*-{
        return this.styleClass;
    }-*/;

    public final native void setStyleClass(String styleclass) /*-{
        this.styleClass = styleclass;
    }-*/;

    public final native String getTagName() /*-{
        return this.tagName;
    }-*/;

    public final native void setTagName(String tagName) /*-{
        this.tagName = tagName;
    }-*/;

    public final native JavaScriptObject getAttributes() /*-{
        return this.attributes;
    }-*/;

    public final native void setAttributes(JavaScriptObject attributes) /*-{
        this.attributes = attributes;
    }-*/;

    public final native JavaScriptObject getStyle() /*-{
        return this.style;
    }-*/;

    public final native void setStyle(JavaScriptObject style) /*-{
        this.style = style;
    }-*/;

    public static native OrionStyleOverlay create() /*-{
        return {};
    }-*/;
}
