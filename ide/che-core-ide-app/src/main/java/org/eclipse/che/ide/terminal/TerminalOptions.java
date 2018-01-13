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
package org.eclipse.che.ide.terminal;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Terminal options. See more {@link Terminal}.
 *
 * @author Evgen Vidolob
 * @author Oleksandr Andriienko
 */
@JsType(namespace = JsPackage.GLOBAL)
public class TerminalOptions {
  /** Default terminal options constructor */
  @JsIgnore
  public TerminalOptions() {
    setTheme("default");
    setConvertEol(false);
    setTermName("xterm");
    setGeometry(new int[] {80, 24});
    setCursorBlink(false);
    setCursorStyle("block");
    setVisualBell(false);
    setPopOnBell(false);
    setScrollBack(1000);
    setScreenKeys(false);
    setDebug(false);
    setCancelEvents(false);
    setDisableStdin(false);
    setUseFlowControl(false);
    setTabStopWidth(8);

    setFocusOnOpen(true);
    setReadOnly(false);
  }

  @JsProperty(name = "theme")
  public native void setTheme(String theme);

  @JsProperty(name = "theme")
  public native String getTheme();

  @JsProperty(name = "convertEol")
  public native void setConvertEol(boolean convertEol);

  @JsProperty(name = "convertEol")
  public native boolean getConvertEol();

  @JsProperty(name = "termName")
  public native void setTermName(String termName);

  @JsProperty(name = "termName")
  public native String getTermName();

  @JsProperty(name = "geometry")
  public native void setGeometry(int[] geometry);

  @JsProperty(name = "geometry")
  public native int[] getGeometry();

  @JsProperty(name = "cursorBlink")
  public native void setCursorBlink(boolean cursorBlink);

  @JsProperty(name = "cursorBlink")
  public native boolean getCursorBlink();

  @JsProperty(name = "cursorStyle")
  public native void setCursorStyle(String cursorStyle);

  @JsProperty(name = "cursorStyle")
  public native String getCursorStyle();

  @JsProperty(name = "visualBell")
  public native void setVisualBell(boolean visualBell);

  @JsProperty(name = "visualBell")
  public native boolean getVisualBell();

  @JsProperty(name = "popOnBell")
  public native void setPopOnBell(boolean popOnBell);

  @JsProperty(name = "popOnBell")
  public native boolean getPopOnBell();

  @JsProperty(name = "scrollback")
  public native void setScrollBack(int scrollBack);

  @JsProperty(name = "scrollback")
  public native int getScrollBack();

  @JsProperty(name = "screenKeys")
  public native void setScreenKeys(boolean screenKeys);

  @JsProperty(name = "screenKeys")
  public native boolean getScreenKeys();

  @JsProperty(name = "debug")
  public native void setDebug(boolean debug);

  @JsProperty(name = "debug")
  public native boolean getDebug();

  @JsProperty(name = "cancelEvents")
  public native void setCancelEvents(boolean cancelEvents);

  @JsProperty(name = "cancelEvents")
  public native boolean getCancelEvents();

  @JsProperty(name = "disableStdin")
  public native void setDisableStdin(boolean disableStdin);

  @JsProperty(name = "disableStdin")
  public native boolean getDisableStdin();

  @JsProperty(name = "useFlowControl")
  public native void setUseFlowControl(boolean useFlowControl);

  @JsProperty(name = "useFlowControl")
  public native boolean getUseFlowControl();

  @JsProperty(name = "tabStopWidth")
  public native void setTabStopWidth(int tabStopWidth);

  @JsProperty(name = "tabStopWidth")
  public native int getTabStopWidth();

  @JsProperty(name = "focusOnOpen")
  public native void setFocusOnOpen(boolean focusOnOpen);

  @JsProperty(name = "focusOnOpen")
  public native boolean getFocusOnOpen();

  @JsProperty(name = "readOnly")
  public native void setReadOnly(boolean readOnly);

  @JsProperty(name = "readOnly")
  public native boolean getReadOnly();
}
