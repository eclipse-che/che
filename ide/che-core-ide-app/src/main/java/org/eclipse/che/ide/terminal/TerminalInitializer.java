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
package org.eclipse.che.ide.terminal;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.lib.terminal.client.TerminalResources;
import org.eclipse.che.requirejs.RequireJsLoader;

/** Terminal entry point. */
@Singleton
public class TerminalInitializer {

  private final PerspectiveManager perspectiveManager;

  @Inject
  public TerminalInitializer(
      final TerminalResources terminalResources,
      final PerspectiveManager perspectiveManager,
      final TerminalInitializePromiseHolder terminalModule,
      final RequireJsLoader requireJsLoader) {
    this.perspectiveManager = perspectiveManager;
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
        new String[] {"term/xterm"},
        new String[] {"Xterm"});
  }
}
