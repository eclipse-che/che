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

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import elemental.js.JsBrowser;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;

class DevModeScriptInjector {

  private static final String DEV_MODE_SCRIPT_NAME = "dev_mode_on.js";

  /** Injects JS script that tells the Code Server to recompile GWT app. */
  static Promise<Void> injectScript(String codeServerURL) {

    return CallbackPromiseHelper.createFromCallback(
        promiseCallback ->
            ScriptInjector.fromUrl(codeServerURL + DEV_MODE_SCRIPT_NAME)
                .setWindow(JsBrowser.getWindow())
                .setCallback(getScriptInjectionCallback(promiseCallback))
                .inject());
  }

  private static Callback<Void, Exception> getScriptInjectionCallback(
      Callback<Void, Throwable> promiseCallback) {

    return new Callback<Void, Exception>() {
      @Override
      public void onSuccess(Void result) {
        promiseCallback.onSuccess(result);
      }

      @Override
      public void onFailure(Exception reason) {
        promiseCallback.onFailure(reason);
      }
    };
  }
}
