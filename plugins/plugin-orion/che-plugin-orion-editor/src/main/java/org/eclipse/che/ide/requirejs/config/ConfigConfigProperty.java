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

public class ConfigConfigProperty extends JavaScriptObject {

    protected ConfigConfigProperty() {
    }

    public final native ConfigConfigProperty create() /*-{
        return {};
    }-*/;

    public final native ConfigItem getMap(String prefix) /*-{
        return this[prefix];
    }-*/;

    public final native void setMap(String prefix, ConfigItem map) /*-{
        this[prefix] = map;
    }-*/;

    public final native JsArrayString getPrefixes() /*-{
        return this.getOwnPropertyNames();
    }-*/;
}
