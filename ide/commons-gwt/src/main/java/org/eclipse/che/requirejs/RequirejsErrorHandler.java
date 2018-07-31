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
package org.eclipse.che.requirejs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Interface for requirejs error callbacks.
 *
 * @author "Mickaël Leduque"
 */
public interface RequirejsErrorHandler {

  /**
   * Called when a requirejs operation fails.
   *
   * @param error the error object
   */
  void onError(RequireError error);

  class RequireError extends JavaScriptObject {
    protected RequireError() {}

    public final native String getRequireType() /*-{
            return this.requireType;
        }-*/;

    public final native JsArrayString getRequireModules() /*-{
            return this.requireModules;
        }-*/;

    public final native String getMessage() /*-{
            return this.message;
        }-*/;
  }
}
