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

public class ConfigConfigProperty extends JavaScriptObject {

  protected ConfigConfigProperty() {}

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
