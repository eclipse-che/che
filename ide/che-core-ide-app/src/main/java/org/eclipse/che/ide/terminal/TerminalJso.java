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
package org.eclipse.che.ide.terminal;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import org.eclipse.che.api.promises.client.Operation;

/**
 * GWT binding to term.js script
 *
 * @author Evgen Vidolob
 * @author Alexander Andrienko
 */
class TerminalJso extends JavaScriptObject {
  protected TerminalJso() {}

  public static native TerminalJso create(
      JavaScriptObject termJSO, TerminalOptionsJso options) /*-{
        return new termJSO(options);
    }-*/;

  public final native void open(Element element) /*-{
        this.open(element);
    }-*/;

  public final native void attachCustomKeyDownHandler(JavaScriptObject customKeyDownHandler) /*-{
        this.attachCustomKeydownHandler(customKeyDownHandler);
    }-*/;

  public final native Element getElement() /*-{
        return this.element;
    }-*/;

  public final native TerminalGeometryJso proposeGeometry() /*-{
        return this.proposeGeometry();
    }-*/;

  public final native void on(String event, Operation<String> operation) /*-{
        this.on(event, $entry(function (data) {
            operation.@org.eclipse.che.api.promises.client.Operation::apply(*)(data);
        }));
    }-*/;

  public final native void resize(int x, int y) /*-{
        this.resize(x, y);
    }-*/;

  public final native void write(String data) /*-{
        this.write(data);
    }-*/;

  public final native void focus() /*-{
        this.focus();
    }-*/;

  public final native void blur() /*-{
        this.blur();
    }-*/;

  public final native boolean hasSelection() /*-{
      return this.hasSelection();
    }-*/;
}
