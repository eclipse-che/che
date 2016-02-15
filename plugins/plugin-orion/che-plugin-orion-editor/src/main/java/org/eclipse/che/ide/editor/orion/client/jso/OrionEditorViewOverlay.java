/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
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
import com.google.gwt.dom.client.Element;

import org.eclipse.che.api.promises.client.Promise;

/**
 * JavaScript overlay over Orion EditorView object.
 *
 * @author Artem Zatsarynnyi
 */
public class OrionEditorViewOverlay extends JavaScriptObject {

    protected OrionEditorViewOverlay() {
    }

    public final static native Promise<OrionEditorViewOverlay> createEditor(final Element element,
                                                                            final JavaScriptObject options) /*-{
        options.parent = element;
        return this.create(options);
    }-*/;

    public final native void setContents(final String contents, final String contentType) /*-{
        this.setContents(contents, contentType);
    }-*/;

    public final native OrionEditorOverlay getEditor() /*-{
        return this.editor;
    }-*/;

    public final native void updateSettings(JavaScriptObject settings) /*-{
        return this.updateSettings(settings);
    }-*/;
}
