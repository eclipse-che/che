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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

public class OrionFindOptionsOverlay extends JavaScriptObject {

  protected OrionFindOptionsOverlay() {}

  public final native boolean getCaseInsensitive() /*-{
        return this.caseInsensitive;
    }-*/;

  /* default : false */
  public final native void setCaseInsensitive(final boolean newValue) /*-{
        this.caseInsensitive = newValue;
    }-*/;

  public final native int getEnd() /*-{
        return this.end;
    }-*/;

  /* default : charcount */
  public final native void setEnd(final boolean newValue) /*-{
        this.end = newValue;
    }-*/;

  public final native boolean getRegex() /*-{
        return this.regex;
    }-*/;

  /* default false */
  public final native void setRegex(final boolean newValue) /*-{
        this.regex = newValue;
    }-*/;

  public final native boolean getReverse() /*-{
        return this.reverse;
    }-*/;

  /* default false */
  public final native void setReverse(final boolean newValue) /*-{
        this.reverse = newValue;
    }-*/;

  public final native int getStart() /*-{
        return this.start;
    }-*/;

  /* default : 0 */
  public final native void setStart(final boolean newValue) /*-{
        this.start = newValue;
    }-*/;

  public final native String getString() /*-{
        return this.string;
    }-*/;

  public final native void setString(final String newValue) /*-{
        this.string = newValue;
    }-*/;

  public final native boolean getWholeWord() /*-{
        return this.wholeWord;
    }-*/;

  /* default false */
  public final native void setWholeWord(final boolean newValue) /*-{
        this.wholeWord = newValue;
    }-*/;

  public final native boolean getWrap() /*-{
        return this.wrap;
    }-*/;

  /* default false */
  public final native void setWrap(final boolean newValue) /*-{
        this.wrap = newValue;
    }-*/;
}
