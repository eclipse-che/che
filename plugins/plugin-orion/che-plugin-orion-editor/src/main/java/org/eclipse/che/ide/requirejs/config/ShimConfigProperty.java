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

public class ShimConfigProperty extends JavaScriptObject {

    protected ShimConfigProperty() {
    }

    public final native ShimConfigProperty create() /*-{
        return {};
    }-*/;

    public final native void addShim(String module, ShimItem shim) /*-{
        this[module] = shim;
    }-*/;

    public final native ShimItem getShim(String module) /*-{
        return this[module];
    }-*/;

    public final native JsArrayString getKeys() /*-{
        return this.getOwnPropertyNames();
    }-*/;

}
