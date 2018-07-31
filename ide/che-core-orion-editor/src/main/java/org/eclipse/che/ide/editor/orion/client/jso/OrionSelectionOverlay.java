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

public class OrionSelectionOverlay extends JavaScriptObject {

  protected OrionSelectionOverlay() {}

  // not sure it's an int
  public final native int getStart() /*-{
        return this.start;
    }-*/;

  // not sure it's an int
  public final native void setStart(final int newValue) /*-{
        this.start = newValue;
    }-*/;

  // not sure it's an int
  public final native int getEnd() /*-{
        return this.end;
    }-*/;

  // not sure it's an int
  public final native void setEnd(final int newValue) /*-{
        this.end = newValue;
    }-*/;
}
