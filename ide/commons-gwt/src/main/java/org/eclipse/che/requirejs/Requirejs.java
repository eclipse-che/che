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
package org.eclipse.che.requirejs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import org.eclipse.che.requirejs.conf.RequirejsConfig;

/**
 * JSO over requirejs function.
 *
 * @author "Mickaël Leduque"
 */
public class Requirejs extends JavaScriptObject {

  protected Requirejs() {}

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

  public final native void require(
      JsArrayString deps, RequirejsCallback callback, RequirejsErrorHandler errorHandler) /*-{
        var realCallback = function() {
            var param = [];
            var args = Array.prototype.slice.call(arguments);
            args.forEach(function(module) {
                param.push(module);
            });
            callback.@org.eclipse.che.requirejs.RequirejsCallback::onReady(Lcom/google/gwt/core/client/JsArray;)(param);
        };
        var realErrHandler = function(err) {
            if (errorHandler) {
                errorHandler.@org.eclipse.che.requirejs.RequirejsErrorHandler::onError(Lorg/eclipse/che/requirejs/RequirejsErrorHandler$RequireError;)(err);
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
            handler.@org.eclipse.che.requirejs.RequirejsErrorHandler::onError(Lorg/eclipse/che/requirejs/RequirejsErrorHandler$RequireError;)(err);
        };
    }-*/;
}
