/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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

/**
 * The 'Hover Object' for Orion hover
 * See <a href="https://wiki.eclipse.org/Orion/Documentation/Developer_Guide/Plugging_into_the_editor#orion.edit.hover">Hover Object</a>
 *
 * @author Evgen Vidolob
 */
public class OrionHoverOverlay extends JavaScriptObject {
    protected OrionHoverOverlay() {}

    public static native OrionHoverOverlay create() /*-{
        return {};
    }-*/;

    /**
     * @param title The title to use to identify this section. This may be either raw text or formatted as markdown.
     */
    public final native void setTite(String title) /*-{
        this.title = title;
    }-*/;

    /**
     * @param content The content to show. This combined with the defined 'type' determine the eventual output in the tooltip.
     */
    public final native void setContent(String content) /*-{
        this.content = content;
    }-*/;

    /**
     * @param type  Defines the type of the 'content'. This is used to render the correct information into the tooltip's contents.
     *              if this field is undefined then both 'title' and 'content' are rendered as raw text.
     *              The type 'markdown' will render the content and title text as markdown (sanitized to remove HTML tags).
     *              The type 'html' will render the content text as HTML inside of an iframe.
     */
    public final native void setType(String type) /*-{
        this.type = type;
    }-*/;

    /**
     * @param offset Along with offsetEnd, specifies the text offset range associated with the returned hover content.
     *               If not provided, the closest word to the offset will be used.
     */
    public final native void setOffsetStart(int offset) /*-{
        this.offsetStart = offset;
    }-*/;

    /**
     * @param offset  Along with offsetStart, specifies the text offset range associated with the returned hover content.
     *                If not provided, the closest word to the offset will be used.
     */
    public final native void setOffsetEnd(int offset) /*-{
        this.offsetEnd = offset;
    }-*/;

    /**
     * @param width When the content 'type' is 'html', this string is set as the 'width' style of the iframe containing the content (ex. "200px").
     */
    public final native void setWidth(String width) /*-{
        this.width = width;
    }-*/;

    /**
     * @param height When the content 'type' is 'html', this string is set as the 'height' style of the iframe containing the content (ex. "200px").
     */
    public final native void setHeight(String height) /*-{
        this.height = height;
    }-*/;

    /**
     * @param allowFullWidth By default the tooltip width is restricted to half of the editor width.
     *                       This allows long contents to not cover the entire editor and instead wrap and scroll.
     *                       This behavior can be overridden by setting the allowFullWidth property to true.
     *                       When true, the tooltip can open as wide as the editor.
     *
     */
    public final native void setAllowFullWidth(boolean allowFullWidth) /*-{
        this.allowFullWidth = allowFullWidth;
    }-*/;
}
