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
package org.eclipse.che.ide.requirejs;

import org.eclipse.che.ide.requirejs.config.RequirejsConfig;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * JSO over requirejs function.
 *
 * @author "Mickaël Leduque"
 */
public class Requirejs extends JavaScriptObject {

    protected Requirejs() {
    }

    public static final native Requirejs get() /*-{
        return $wnd.require;
    }-*/;

    public static final native Requirejs config(RequirejsConfig config) /*-{
        var localRequire = $wnd.require.config(config);
        localRequire.require = function(deps, callback, errback) {
            return localRequire(deps, callback, errback);
        }
        return localRequire;
    }-*/;

    public final void require(JsArrayString deps, RequirejsCallback callback) {
        require(deps, callback, null);
    }

    public final native void require(JsArrayString deps, RequirejsCallback callback, RequirejsErrorHandler errorHandler) /*-{
        var realCallback = function() {
            var param = [];
            var args = Array.prototype.slice.call(arguments);
            args.forEach(function(module) {
                param.push(module);
            });
            callback.@org.eclipse.che.ide.requirejs.RequirejsCallback::onReady(Lcom/google/gwt/core/client/JsArray;)(param);
        };
        var realErrHandler = function(err) {
            if (errorHandler) {
                errorHandler.@org.eclipse.che.ide.requirejs.RequirejsErrorHandler::onError(Lorg/eclipse/che/ide/requirejs/RequirejsErrorHandler$RequireError;)(err);
            } else {
                $wnd.require.onError(err);
            }
        };
        this.require(deps, realCallback, realErrHandler);
    }-*/;


    public final native String toUrl(String moduleNamePlusExt) /*-{
        return this.tourl(moduleNamePlusExt);
    }-*/;

    public final native String undef(String module) /*-{
        return this.undef(module);
    }-*/;

    public final native boolean defined(String module) /*-{
        return this.defined(module);
    }-*/;

    public final native boolean specified(String module) /*-{
        return this.specified(module);
    }-*/;

    public final native String version() /*-{
        return this.version;
    }-*/;

    public final native void setOnError(JavaScriptObject onError) /*-{
        this.onError = onError;
    }-*/;

    public final native void setOnError(RequirejsErrorHandler handler) /*-{
        this.onError = function(err) {
            handler.@org.eclipse.che.ide.requirejs.RequirejsErrorHandler::onError(Lorg/eclipse/che/ide/requirejs/RequirejsErrorHandler$RequireError;)(err);
        };
    }-*/;
}