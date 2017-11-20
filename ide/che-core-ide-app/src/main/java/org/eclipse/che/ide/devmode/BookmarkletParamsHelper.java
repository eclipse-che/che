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
package org.eclipse.che.ide.devmode;

import static jsinterop.annotations.JsPackage.GLOBAL;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Helps to set the necessary parameters for GWT Code Server . */
class BookmarkletParamsHelper {

  private static final String IDE_GWT_APP_SHORT_NAME = "_app";

  static void setParams(String codeServerURL) {
    BookmarkletParams params = new BookmarkletParams();
    params.setServerURL(codeServerURL);
    params.setModuleName(IDE_GWT_APP_SHORT_NAME);

    Window.setParams(params);
  }

  @JsType(isNative = true, name = "window", namespace = JsPackage.GLOBAL)
  private static class Window {

    @JsProperty(name = "__gwt_bookmarklet_params")
    static native void setParams(Object message);
  }

  @JsType(isNative = true, name = "Object", namespace = GLOBAL)
  private static class BookmarkletParams {

    @JsProperty(name = "server_url")
    native void setServerURL(String serverURL);

    @JsProperty(name = "module_name")
    native void setModuleName(String moduleName);
  }
}
