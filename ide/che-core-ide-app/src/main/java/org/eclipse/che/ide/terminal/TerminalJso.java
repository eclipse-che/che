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
package org.eclipse.che.ide.terminal;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.terminal.options.TerminalOptionsJso;

/**
 * GWT binding to term.js script
 *
 * @author Evgen Vidolob
 * @author Oleksandr Andriienko
 */
class TerminalJso extends JavaScriptObject {
  protected TerminalJso() {}

  public static native TerminalJso create(
      JavaScriptObject termJSO, TerminalOptionsJso options) /*-{
        return {
            termJSO : termJSO,
            terminal : new termJSO(options)
        };
    }-*/;

  public final native void open(Element element) /*-{
        this.terminal.open(element);
    }-*/;

  public final native void attachCustomKeyEventHandler(
      CustomKeyEventTerminalHandler customKeyEventHandler) /*-{
      this.terminal.attachCustomKeyEventHandler(customKeyEventHandler);
    }-*/;

  public final native Element getElement() /*-{
        return  this.terminal.element;
    }-*/;

  public final native TerminalGeometryJso proposeGeometry() /*-{
        return this.terminal.proposeGeometry();
    }-*/;

  public final native <T> void on(String event, Operation<T> operation) /*-{
      this.terminal.on(event, $entry(function (data) {
            operation.@org.eclipse.che.api.promises.client.Operation::apply(*)(data);
        }));
    }-*/;

  public final native void resize(int x, int y) /*-{
      this.terminal.resize(x, y);
    }-*/;

  public final native void write(String data) /*-{
      this.terminal.write(data);
    }-*/;

  public final native void focus() /*-{
      this.terminal.focus();
    }-*/;

  public final native void blur() /*-{
      this.terminal.blur();
    }-*/;

  public final native boolean hasSelection() /*-{
      this.terminal.hasSelection();
    }-*/;

  public final native void destroy() /*-{
      this.terminal.destroy();
  }-*/;

  public final native void applyAddon(JavaScriptObject addon) /*-{
      this.termJSO.applyAddon(addon);
  }-*/;

  public final native String[] getRenderedLines() /*-{
     var start = this.terminal.buffer.ydisp;
     var rows = this.terminal.rows;

     var lines = [];
     for (var lineIndex = start; lineIndex < start + rows; lineIndex++) {
         var lineText = this.terminal.buffer.translateBufferLineToString(lineIndex, true);

         var bufferLine = this.terminal.buffer.lines.get(lineIndex);
         if (bufferLine.isWrapped) {
             lines[lines.length - 1] += lineText;
         } else {
             lines.push(lineText);
         }
     }

     return lines;
    }-*/;
}
