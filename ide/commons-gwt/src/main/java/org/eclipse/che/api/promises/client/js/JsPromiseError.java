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
package org.eclipse.che.api.promises.client.js;

import com.google.gwt.core.client.JavaScriptObject;
import org.eclipse.che.api.promises.client.PromiseError;

public class JsPromiseError extends JavaScriptObject implements PromiseError {

  /** JSO mandated protected constructor. */
  protected JsPromiseError() {}

  public static final native JsPromiseError create() /*-{
        return new Error();
    }-*/;

  public static final native JsPromiseError create(
      String message, String filename, String linenumber) /*-{
        return new Error(message, filename, linenumber);
    }-*/;

  public static final native JsPromiseError create(String message, String filename) /*-{
        return new Error(message, filename);
    }-*/;

  public static final native JsPromiseError create(String message) /*-{
        return new Error(message);
    }-*/;

  public static final native JsPromiseError create(JavaScriptObject object) /*-{
        return object;
    }-*/;

  public static final JsPromiseError create(final Throwable e) {
    if (e == null) {
      return create();
    } else {
      return createFromThrowable(e);
    }
  }

  private static final native JsPromiseError createFromThrowable(final Throwable e) /*-{
        var message = e.@java.lang.Throwable::getMessage()();
        var result = new Error(message);
        result.cause = e;
        return result;
    }-*/;

  public final native String getMessage() /*-{
        return this.message;
    }-*/;

  public final native String getName() /*-{
        return this.name;
    }-*/;

  @Override
  public final native Throwable getCause() /*-{
        return this.cause;
    }-*/;
}
