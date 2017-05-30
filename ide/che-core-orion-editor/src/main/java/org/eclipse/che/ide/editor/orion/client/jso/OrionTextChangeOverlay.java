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

public class OrionTextChangeOverlay extends JavaScriptObject {

    protected OrionTextChangeOverlay() {
    }

    public final native int getStart() /*-{
        return this.start;
    }-*/;


    public final native int getEnd() /*-{
        return this.end;
    }-*/;


    public final native String getText() /*-{
        return this.text;
    }-*/;

    public final native void setStart(int start) /*-{
        this.start = start;
    }-*/;


    public final native void setEnd(int end) /*-{
        this.end = end;
    }-*/;


    public final native void setText(String text) /*-{
        this.text = text;
    }-*/;

    public static final native OrionTextChangeOverlay create() /*-{
        return {};
    }-*/;
}
