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
package org.eclipse.che.ide.terminal;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.lib.terminal.client.TerminalResources;
import org.eclipse.che.requirejs.RequireJsLoader;

/** Terminal entry point. */
@Singleton
public class TerminalInitializer {

  public static final String XTERM_JS_MODULE = "Xterm";
  public static final String FIT_ADDON = "Fit";

  @Inject
  public TerminalInitializer(
      final TerminalResources terminalResources,
      final TerminalInitializePromiseHolder terminalModule,
      final RequireJsLoader requireJsLoader) {
    terminalResources.getTerminalStyle().ensureInjected();

    Promise<Void> termInitPromise =
        AsyncPromiseHelper.createFromAsyncRequest(
            callback -> injectTerminal(requireJsLoader, callback));
    terminalModule.setInitializerPromise(termInitPromise);
  }

  private void injectTerminal(RequireJsLoader rJsLoader, final AsyncCallback<Void> callback) {
    rJsLoader.require(
        new Callback<JavaScriptObject[], Throwable>() {
          @Override
          public void onFailure(Throwable reason) {
            callback.onFailure(reason);
          }

          @Override
          public void onSuccess(JavaScriptObject[] result) {
            callback.onSuccess(null);
          }
        },
        new String[] {"term/xterm", "term/addons/fit/fit"},
        new String[] {XTERM_JS_MODULE, FIT_ADDON});
  }
}
