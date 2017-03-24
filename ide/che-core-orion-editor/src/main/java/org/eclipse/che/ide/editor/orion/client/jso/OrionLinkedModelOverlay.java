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
import com.google.gwt.core.client.JsArray;

import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
public class OrionLinkedModelOverlay extends JavaScriptObject implements LinkedModel {
    protected OrionLinkedModelOverlay() {
    }

    @Override
    public final void setGroups(List<LinkedModelGroup> groups) {
        JsArray<OrionLinkedModelGroupOverlay> arr = JavaScriptObject.createArray().cast();
        for (LinkedModelGroup group : groups) {
            if(group instanceof OrionLinkedModelGroupOverlay){
                arr.push((OrionLinkedModelGroupOverlay)group);
            } else {
                throw new IllegalArgumentException("This implementation supports only OrionLinkedModelGroupOverlay groups");
            }
        }
        setGroups(arr);
    }

    @Override
    public final native void setEscapePosition(int offset) /*-{
        this.escapePosition = offset;
    }-*/;

    public final native void setGroups(JsArray<OrionLinkedModelGroupOverlay> groups) /*-{
        this.groups = groups;
    }-*/;

    public static native OrionLinkedModelOverlay create() /*-{
        return {};
    }-*/;
}
