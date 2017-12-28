/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.console;

import elemental.events.KeyboardEvent;

import org.eclipse.che.ide.terminal.Terminal;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_C;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_DOWN;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_END;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_HOME;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_PAGEDOWN;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_PAGEUP;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_UP;

/**
 * Custom keyDown handler for output console to support common hotKeys.
 *
 * @author Alexander Andrienko
 */
public final class OutputConsoleCustomKeyDownHandler implements Terminal.CustomKeyDownHandler {

    private static final int HORIZONTAL_SCROLL_LINES = 4;

    private final Terminal terminal;

    public OutputConsoleCustomKeyDownHandler(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public boolean keyDown(KeyboardEvent ev) {
        if (ev.isCtrlKey() && !(ev.isShiftKey() || ev.isMetaKey() || ev.isAltKey())) {
            // handle Ctrl + C.
            return ev.getKeyCode() != KEY_C || !terminal.hasSelection();
        }

        if (!(ev.isCtrlKey() || ev.isShiftKey() || ev.isMetaKey() || ev.isAltKey())) {

            if (ev.getKeyCode() == KEY_UP) {
               terminal.scrollDisp(-HORIZONTAL_SCROLL_LINES);
               return false;
            }

            if (ev.getKeyCode() == KEY_DOWN) {
                terminal.scrollDisp(HORIZONTAL_SCROLL_LINES);
                return false;
            }

            if (ev.getKeyCode() == KEY_PAGEUP) {
                terminal.scrollDisp(-(terminal.getRows() - 1));
                return false;
            }

            if (ev.getKeyCode() == KEY_PAGEDOWN) {
                terminal.scrollDisp(terminal.getRows() - 1);
                return false;
            }

            if (ev.getKeyCode() == KEY_HOME) {
                terminal.scrollHome();
                return false;
            }

            if (ev.getKeyCode() == KEY_END) {
                terminal.scrollEnd();
                return false;
            }

            //todo implement vertical scroll by KEY_LEFT and KEY_RIGHT
        }

        return true;
    }
}
