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
 * Configuration object JS overlay for requirejs.
 *
 * @author "Mickaël Leduque"
 */
public class RequirejsConfig extends JavaScriptObject {

    protected RequirejsConfig() {
    }

    /**
     * Create a new configuration object.
     *
     * @return a configuration object
     */
    public static final native RequirejsConfig create() /*-{
        return {};
    }-*/;

    public final native String getBaseUrl() /*-{
        return this.baseUrl;
    }-*/;

    public final native void setBaseUrl(String baseUrl) /*-{
        this.baseUrl = baseUrl;
    }-*/;

    public final native PathsConfigProperty getPaths() /*-{
        return this.paths;
    }-*/;

    public final native void setPaths(PathsConfigProperty paths) /*-{
        this.paths = paths;
    }-*/;

    public final native BundlesConfigProperty getBundles() /*-{
        return this.bundles;
    }-*/;

    public final native void setBundles(BundlesConfigProperty bundles) /*-{
        this.bundles = bundles;
    }-*/;

    public final native ShimConfigProperty getShim() /*-{
        return this.shim;
    }-*/;

    public final native void setShim(ShimConfigProperty shim) /*-{
        this.shim = shim;
    }-*/;

    public final native MapConfigProperty getMap() /*-{
        return this.map;
    }-*/;

    public final native void setMap(MapConfigProperty map) /*-{
        this.map = map;
    }-*/;

    public final native JavaScriptObject getConfig() /*-{
        return this.config;
    }-*/;

    public final native void setConfig(JavaScriptObject config) /*-{
        this.config = config;
    }-*/;

    public final native JsArrayString getPackages() /*-{
        return this.packages;
    }-*/;

    public final native void setPackages(JsArrayString packages) /*-{
        this.packages = packages;
    }-*/;

    /**
     * Returns the number of seconds to wait for a module load.<br>
     * Default to 7s.
     *
     * @return module load timeout
     */
    public final native int getWaitSeconds() /*-{
        return this.waitSeconds;
    }-*/;

    public final native void setWaitSeconds(int waitSeconds) /*-{
        this.waitSeconds = waitSeconds;
    }-*/;

    public final native String getContext() /*-{
        return this.context;
    }-*/;

    public final native void setContext(String context) /*-{
        this.context = context;
    }-*/;

    public final native JsArrayString getDeps() /*-{
        return this.deps;
    }-*/;

    public final native void setDeps(JsArrayString deps) /*-{
        this.deps = deps;
    }-*/;

    public final native JavaScriptObject getCallback() /*-{
        return this.callback;
    }-*/;

    public final native void setCallback(JavaScriptObject callback) /*-{
        this.callback = callback;
    }-*/;

    public final native boolean getEnforceDefine() /*-{
        return this.enforceDefine;
    }-*/;

    public final native void setEnforceDefine(boolean enforceDefine) /*-{
        this.enforceDefine = enforceDefine;
    }-*/;

    public final native boolean getXhtml() /*-{
        return this.xhtml;
    }-*/;

    public final native void setXhtml(boolean xhtml) /*-{
        this.xhtml = xhtml;
    }-*/;

    public final native String getUrlArgs() /*-{
        return this.urlArgs;
    }-*/;

    public final native void setUrlArgs(String urlArgs) /*-{
        this.urlArgs = urlArgs;
    }-*/;

    public final native String getScriptType() /*-{
        return this.scriptType;
    }-*/;

    public final native void setScriptType(String scriptType) /*-{
        this.scriptType = scriptType;
    }-*/;

    public final native boolean getSkipDataMain() /*-{
        return this.skipDataMain;
    }-*/;

    public final native void setSkipDataMain(boolean skipDataMain) /*-{
        this.skipDataMain = skipDataMain;
    }-*/;
}
