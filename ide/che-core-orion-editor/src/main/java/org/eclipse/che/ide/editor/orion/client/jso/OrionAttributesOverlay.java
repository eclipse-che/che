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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/** Overlay on the orion JS Attributes objects. */
public class OrionAttributesOverlay extends JavaScriptObject {

  /** JSO mandated protected constructor. */
  protected OrionAttributesOverlay() {}

  public final native void setAttribute(String name, String value) /*-{
        this[name] = value;
    }-*/;

  public final native String getAttribute(String name) /*-{
        return this[name];
    }-*/;

  public static native OrionAttributesOverlay create() /*-{
        return {};
    }-*/;
}
