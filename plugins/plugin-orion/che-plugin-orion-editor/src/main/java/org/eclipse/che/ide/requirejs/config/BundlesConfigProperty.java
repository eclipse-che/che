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

/**
 * A bundle configuration object for requirejs.
 *
 * @author "Mickaël Leduque"
 */
public final class BundlesConfigProperty extends JavaScriptObject {
    protected BundlesConfigProperty() {
    }

    public final static native BundlesConfigProperty create() /*-{
        return {};
    }-*/;

    public final native void addBundle(String mainModule, JsArrayString bundlesModules) /*-{
        this[mainModule] = bundlesModules;
    }-*/;

    public final native JsArrayString getBundle(String mainModule) /*-{
        return this[mainModule];
    }-*/;

    public final native JsArrayString getKeys() /*-{
        return this.getOwnPropertyNames();
    }-*/;
}
