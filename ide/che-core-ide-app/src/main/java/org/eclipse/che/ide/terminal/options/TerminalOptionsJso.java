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
 * Options to configure xterm.js.
 *
 * @author Evgen Vidolob
 * @author Oleksandr Andriienko
 */
public class TerminalOptionsJso extends Jso {
  protected TerminalOptionsJso() {}

  public static native TerminalOptionsJso create() /*-{
        return {
            cols: 80,
            rows: 24,
            screenKeys: true,
            fontSize: 12,
            fontFamily: "DejaVu Sans Mono"
        }
    }-*/;

  public final native TerminalThemeJso getTheme() /*-{
      return this.theme;
    }-*/;

  public final native void setTheme(TerminalThemeJso theme) /*-{
        this.theme = theme;
    }-*/;
}
