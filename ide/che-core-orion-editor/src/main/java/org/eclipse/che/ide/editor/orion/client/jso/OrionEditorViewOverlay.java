/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * JavaScript overlay over Orion EditorView object.
 *
 * @author Artem Zatsarynnyi
 */
public class OrionEditorViewOverlay extends JavaScriptObject {

  protected OrionEditorViewOverlay() {}

  public final native void setContents(final String contents, final String contentType) /*-{
        this.setContents(contents, contentType);
    }-*/;

  public final native OrionEditorOverlay getEditor() /*-{
        return this.editor;
    }-*/;

  public final native void updateSettings(JavaScriptObject settings) /*-{
        return this.updateSettings(settings);
    }-*/;

  public final native void setReadonly(final boolean readonly) /*-{
        this.readonly = readonly;
    }-*/;
}
