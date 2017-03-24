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

public class OrionTextModelOverlay extends JavaScriptObject {

    protected OrionTextModelOverlay() {
    }

    // lines

    public final native int getLineAtOffset(int index) /*-{
        return this.getLineAtOffset(index);
    }-*/;

    public final native int getLineStart(int line) /*-{
        return this.getLineStart(line);
    }-*/;

    public final native int getLineEnd(int line) /*-{
        return this.getLineEnd(line);
    }-*/;

    public final native int getLineCount() /*-{
        return this.getLineCount();
    }-*/;

    public final native String getLine(int line) /*-{
        return this.getLine(line);
    }-*/;

    // get/set text

    public final native String getText() /*-{
        return this.getText();
    }-*/;

    public final native String getText(int startOffset) /*-{
        return this.getText(startOffset);
    }-*/;

    public final native String getText(int startOffset, int endOffset) /*-{
        return this.getText(startOffset, endOffset);
    }-*/;

    public final native void setText(String newText, int startOffset, int endOffset) /*-{
        this.setText(newText, startOffset, endOffset);
    }-*/;

    public final native void setText(String newText, int startOffset) /*-{
        this.setText(newText, startOffset);
    }-*/;

    public final native void setText(String newText) /*-{
        this.setText(newText);
    }-*/;

    // char count

    public final native int getCharCount() /*-{
        return this.getCharCount();
    }-*/;

    // line delimiter


    public final native String getLineDelimiter() /*-{
        return this.getLineDelimiter();
    }-*/;

    public final native void setLineDelimiter(String delimiter) /*-{
        return this.setLineDelimiter(delimiter);
    }-*/;

    public final native void setLineDelimiter(String delimiter, boolean change) /*-{
        return this.setLineDelimiter(delimiter, change);
    }-*/;

    public final native void setAutoLineDelimiter(boolean change) /*-{
        return this.setLineDelimiter("auto", change);
    }-*/;

    // find

    public final native OrionFindIteratorOverlay find(OrionFindOptionsOverlay options) /*-{
        return this.find(options);
    }-*/;

    public final native <T extends OrionEventOverlay> void addEventListener(String eventType,
                                                                            EventHandler<T> handler,
                                                                            boolean useCapture) /*-{
        var func = function (param) {
            handler.@org.eclipse.che.ide.editor.orion.client.jso.OrionTextModelOverlay.EventHandler::onEvent(*)(param);
        };

        this.addEventListener(eventType, func, useCapture);
    }-*/;

    public interface EventHandler<T extends OrionEventOverlay> {
        void onEvent(T parameter);
    }
}
