/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.requirejs.conf;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * A bundle configuration object for requirejs.
 *
 * @author "Mickaël Leduque"
 */
public final class BundlesConfigProperty extends JavaScriptObject {
  protected BundlesConfigProperty() {}

  public static final native BundlesConfigProperty create() /*-{
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
