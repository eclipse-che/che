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

import org.eclipse.che.ide.collections.Jso;

/** @author Evgen Vidolob */
public class TerminalOptionsJso extends Jso {
  protected TerminalOptionsJso() {}

  public static native TerminalOptionsJso createDefault() /*-{
        return {
            cols: 80,
            rows: 24,
            screenKeys: true,
            focusOnOpen: true,
            command: ""
        }
    }-*/;

  /**
   * @param focusOnOpen set true it need to set focus on just opened terminal
   * @return
   */
  public final native TerminalOptionsJso withFocusOnOpen(boolean focusOnOpen) /*-{
        this.focusOnOpen = focusOnOpen;
        return this;
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
