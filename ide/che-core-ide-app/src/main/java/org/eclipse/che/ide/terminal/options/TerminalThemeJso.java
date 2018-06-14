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

import org.eclipse.che.ide.collections.Jso;

/**
 * xterm.js theme definition.
 *
 * @author Oleksandr Andriienko
 */
public class TerminalThemeJso extends Jso {

  protected TerminalThemeJso() {}

  public static native TerminalThemeJso create() /*-{
        return {
            cursor: "white",
            background: "black",
            foreground: "white"
        }
    }-*/;

  public final native void setCursor(String cursorColor) /*-{
        this.cursor = cursorColor;
    }-*/;

  public final native void setBackGround(String backGroundColor) /*-{
        this.background = backGroundColor;
    }-*/;

  public final native void setForeGround(String foreGroundColor) /*-{
        this.foreground = foreGroundColor;
    }-*/;
}
