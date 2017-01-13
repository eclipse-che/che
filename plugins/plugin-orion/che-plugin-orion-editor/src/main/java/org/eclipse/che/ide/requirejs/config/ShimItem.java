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

public class ShimItem extends JavaScriptObject {

    protected ShimItem() {
    }

    public final native ShimItem create() /*-{
        return {};
    }-*/;

    public final native JsArrayString getDeps() /*-{
        return this.deps;
    }-*/;

    public final native void setDeps(JsArrayString deps) /*-{
        this.deps = deps;
    }-*/;

    public final native String getExports() /*-{
        return this.exports;
    }-*/;

    public final native void setExports(String exports) /*-{
        this.exports = exports;
    }-*/;

    public final native JavaScriptObject getInit() /*-{
        return this.init;
    }-*/;

    public final native void setInit(InitFunction init) /*-{
        this.init = function (param) {
            init.@org.eclipse.che.ide.requirejs.config.ShimItem.InitFunction::init()(param);
        };
    }-*/;

    public interface InitFunction {
        String init();
    }
}
