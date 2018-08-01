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
package org.eclipse.che.requirejs.conf;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ShimConfigProperty extends JavaScriptObject {

  protected ShimConfigProperty() {}

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
