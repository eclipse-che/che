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
package org.eclipse.che.ide.requirejs.config;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class AssocitativeJsObject<T> extends JavaScriptObject {

    protected AssocitativeJsObject() {
    }

    public final native JsArrayString getKeys() /*-{
        return this.getOwnPropertyNames();
    }-*/;

    public final native void put(String key, T value) /*-{
        this[key] = value;
    }-*/;

    public final native T get(String key) /*-{
        return this[key];
    }-*/;

}
