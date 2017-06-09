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
package org.eclipse.che.ide.terminal;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Custom keyDown handler for {@link TerminalJso}
 *
 * @author Alexander Andrienko
 */
public class CustomKeyDownTerminalHandler extends JavaScriptObject {
    protected CustomKeyDownTerminalHandler() {
    }

    public static native CustomKeyDownTerminalHandler create() /*-{
        return function(ev) {
            var C = 67;
            var V = 86;
            if (ev.ctrlKey && !(ev.shiftKey || ev.metaKey || ev.altKey)) {

                //handle Ctrl + V
                if (ev.keyCode === V) {
                    return false;
                }

                var selection = this.document.getSelection(),
                    collapsed = selection.isCollapsed,
                    isRange = typeof collapsed === 'boolean' ? !collapsed : selection.type === 'Range';

                //handle Ctrl + C
                if (ev.keyCode === C && isRange) {
                    return false;
                }
            }
        }
    }-*/;
}
