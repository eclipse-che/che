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
            command: "",
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

  /**
   * @param command initial command what will be executed immediately after connection to the
   *     terminal will established
   * @return
   */
  public final native TerminalOptionsJso withCommand(String command) /*-{
        this.command = command;
        return this;
    }-*/;
}
