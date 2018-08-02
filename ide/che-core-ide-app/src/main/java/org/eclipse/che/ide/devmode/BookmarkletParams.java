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
