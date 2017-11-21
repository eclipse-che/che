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
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;

/** Allows to inject JavaScript for enabling Super DevMode on a host page. */
@Singleton
class DevModeScriptInjector {

  private static final String DEV_MODE_SCRIPT_NAME = "dev_mode_on.js";

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

  /**
   * Injects the JS script, that communicates with a Code Server, into a host page.
   *
   * @param url host URL where script is located. Usually should be a Code Server URL
   * @return promise that may be resolved if script has been injected successfully or rejected in
   *     case of any error while script injection
   */
  Promise<Void> inject(String url) {
    return CallbackPromiseHelper.createFromCallback(
        promiseCallback ->
            ScriptInjector.fromUrl(url + DEV_MODE_SCRIPT_NAME)
                .setWindow(JsBrowser.getWindow())
                .setCallback(getScriptInjectionCallback(promiseCallback))
                .inject());
  }
}
