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

public class AssocitativeJsObject<T> extends JavaScriptObject {

  protected AssocitativeJsObject() {}

  public final native JsArrayString getKeys() /*-{
        return this.getOwnPropertyNames();
    }-*/;

  public final native void put(String key, T value) /*-{
        this[key] = value;
    }-*/;

  public final native T get(String key) /*-{
        return this[key];
    }-*/;
}
