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
package org.eclipse.che.ide.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;

/** Performs initial startup of the CHE IDE application. */
@Singleton
public class IdeBootstrap {

  private final DialogFactory dialogFactory;

  @Inject
  IdeBootstrap(DialogFactory dialogFactory) {
    this.dialogFactory = dialogFactory;
  }

  @Inject
  void bootstrap(
      ExtensionInitializer extensionInitializer,
      IdeInitializationStrategyProvider initializationStrategyProvider) {
    try {
      IdeInitializationStrategy strategy = initializationStrategyProvider.get();

      strategy
          .init()
          .then(
              aVoid -> {
                extensionInitializer.startExtensions();
              })
          .catchError(handleError())
          .catchError(handleErrorFallback());
    } catch (Exception e) {
      onInitializationFailed("IDE initialization failed. " + e.getMessage());
    }
  }

  /** Handle an error with IDE UI. */
  private Operation<PromiseError> handleError() {
    return err -> {
      dialogFactory.createMessageDialog("IDE initialization failed", err.getMessage(), null).show();
      Log.error(IdeBootstrap.class, err);
    };
  }

  /** Handle an error without IDE UI, as a fallback (when DialogFactory can't be used). */
  private Operation<PromiseError> handleErrorFallback() {
    return err -> onInitializationFailed(err.getMessage());
  }

  /**
   * Tries to call initializationFailed function which is defined in IDE.jsp for handling IDE
   * initialization errors.
   */
  private native void onInitializationFailed(String reason) /*-{
        try {
            $wnd.IDE.eventHandlers.initializationFailed(reason);
        } catch (e) {
            console.log(e.message);
        }
    }-*/;
}
