/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
