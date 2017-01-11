/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.perspective.terminal;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

import org.eclipse.che.api.promises.client.Operation;

/**
 * GWT binding to term.js script
 *
 * @author Evgen Vidolob
 */
class TerminalJso extends JavaScriptObject {
    protected TerminalJso() {
    }

    public static native TerminalJso create(TerminalOptionsJso options) /*-{
        return new $wnd.Terminal(options);
    }-*/;

    public final native void open(Element element) /*-{
        this.open(element);
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
}
