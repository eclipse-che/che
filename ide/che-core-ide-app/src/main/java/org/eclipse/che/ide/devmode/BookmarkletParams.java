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

import javax.inject.Singleton;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/** Helps to set-up the parameters for GWT Super DevMode. */
@Singleton
class BookmarkletParams {

  /** Sets-up URL of the Code Server and GWT module name to recompile. */
  void setParams(String codeServerURL, String moduleName) {
    Params params = new Params();
    params.setServerURL(codeServerURL);
    params.setModuleName(moduleName);

    Window.setParams(params);
  }

  @JsType(isNative = true, name = "window", namespace = JsPackage.GLOBAL)
  private static class Window {

    @JsProperty(name = "__gwt_bookmarklet_params")
    static native void setParams(Object message);
  }

  @JsType(isNative = true, name = "Object", namespace = GLOBAL)
  private static class Params {

    @JsProperty(name = "server_url")
    native void setServerURL(String serverURL);

    @JsProperty(name = "module_name")
    native void setModuleName(String moduleName);
  }
}
