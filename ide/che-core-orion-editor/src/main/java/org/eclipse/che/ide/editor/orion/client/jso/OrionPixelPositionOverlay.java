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

public class OrionPixelPositionOverlay extends JavaScriptObject {

  protected OrionPixelPositionOverlay() {}

  public final native int getX() /*-{
        return this.x;
    }-*/;

  public final native void setX(final int newValue) /*-{
        this.x = newValue;
    }-*/;

  public final native int getY() /*-{
        return this.y;
    }-*/;

  public final native void setY(final int newValue) /*-{
        this.y = newValue;
    }-*/;

  public static final native OrionPixelPositionOverlay create(int x, int y) /*-{
        return {"x": x, "y": y};
    }-*/;
}
