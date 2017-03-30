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

/**
 * @author Evgen Vidolob
 */
public class OrionProblemOverlay extends JavaScriptObject {
    protected OrionProblemOverlay() {
    }

    public final native void setDescription(String description) /*-{
        this["description"] = description;
    }-*/;

    public final native void setId(String id) /*-{
        this["id"] = id;
    }-*/;

    public final native void setStart(int offset) /*-{
        this["start"]= offset;
    }-*/;

    public final native void setEnd(int offset) /*-{
        this["end"]= offset;
    }-*/;

    public final native void setSeverity(String severity) /*-{
        this["severity"]= severity;
    }-*/;


}
