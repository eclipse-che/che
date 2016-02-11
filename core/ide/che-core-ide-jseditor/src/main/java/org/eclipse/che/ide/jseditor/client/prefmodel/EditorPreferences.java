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
package org.eclipse.che.ide.jseditor.client.prefmodel;

import com.google.gwt.core.client.JavaScriptObject;

import elemental.js.util.JsMapFromStringToString;

/** Data object for the editor preferences. */
public class EditorPreferences extends JavaScriptObject {

	/** JSO required protected constructor. */
    protected EditorPreferences() {}

    /**
     * Returns the default editor.
     * @return the default editor
     */
    public final native String getDefaultEditor() /*-{
        return this.defaultEditor;
    }-*/;

    /**
     * Sets the default editor.
     * @param newValue the new choice
     */
    public final native void setDefaultEditor(String newValue) /*-{
        return this.defaultEditor = newValue;
    }-*/;

    /**
     * Returns the selected key bindings.
     * @return the key bindings
     */
    public final native JsMapFromStringToString getKeymaps() /*-{
        return this.keymaps;
    }-*/;

    /**
     * Creates a default instance.
     * @return an instance
     */
    public static final native EditorPreferences create() /*-{
        return {
                'keymaps': {}
               };
    }-*/;
}
