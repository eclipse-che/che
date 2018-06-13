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
package org.eclipse.che.ide.terminal.options;

import com.google.gwt.core.client.JavaScriptObject;

import org.eclipse.che.ide.collections.Jso;

/** @author Evgen Vidolob */
public class TerminalOptionsJso extends Jso {
  protected TerminalOptionsJso() {}

  public static native TerminalOptionsJso create() /*-{
        return {
            cols: 80,
            rows: 24,
            screenKeys: true
        }
    }-*/;

    public native final void setTheme(JavaScriptObject theme) /*-{
        this.theme = theme;
    }-*/;
}
