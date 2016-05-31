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
import com.google.gwt.core.client.JsArrayString;

public class OrionHighlightingConfigurationOverlay extends JavaScriptObject {
    
    protected OrionHighlightingConfigurationOverlay() {
    }
    
    public static native OrionHighlightingConfigurationOverlay create() /*-{
        return {};
    }-*/;

    public final native void setId(String newValue) /*-{
        this.id = newValue;
    }-*/;
    
    public final void setContentTypes(String ... theContentTypes) {
        JsArrayString arr = JavaScriptObject.createArray().cast();
        for (String value : theContentTypes) {
            arr.push(value);
        }
        setContentTypes(arr);
    }
    
    protected final native void setContentTypes(JsArrayString theContentTypes) /*-{
        this.contentTypes = theContentTypes;
    }-*/;
    
    public final native void setPatterns(String patternsAsJsonArray) /*-{
        this.patterns = eval(patternsAsJsonArray);
    }-*/;
}
