/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.bootstrap;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.workspace.CurrentWorkspaceManager;

/** Performs initial startup of the CHE IDE application. */
@Singleton
public class IdeBootstrap {

    private final DialogFactory dialogFactory;

    @Inject
    IdeBootstrap(DialogFactory dialogFactory) {
        this.dialogFactory = dialogFactory;
    }

    @Inject
    void bootstrap(ExtensionInitializer extensionInitializer, CurrentWorkspaceManager wsManager, IdeInitializer ideInitializer) {
        ideInitializer.init()
                      .then(aVoid -> {
                          extensionInitializer.startExtensions();
                          Scheduler.get().scheduleDeferred(this::notifyShowIDE);
                          wsManager.handleWorkspaceState();
                      })
                      .catchError(handleError())
                      .catchError(handleErrorFallback());
    }

    /** Handle an error with IDE UI. */
    private Operation<PromiseError> handleError() {
        return err -> dialogFactory.createMessageDialog("IDE initialization failed", err.getMessage(), null).show();
    }

    /** Handle an error without IDE UI, as a fallback (when DialogFactory can't be used). */
    private Operation<PromiseError> handleErrorFallback() {
        return err -> onInitializationFailed(err.getMessage());
    }

    /** Informs parent window (e.g. Dashboard) that IDE application is ready to be shown. */
    private native void notifyShowIDE() /*-{
        $wnd.parent.postMessage("show-ide", "*");
    }-*/;

    /**
     * Tries to call initializationFailed function which is defined in
     * IDE.jsp for handling IDE initialization errors.
     */
    private native void onInitializationFailed(String reason) /*-{
        try {
            $wnd.IDE.eventHandlers.initializationFailed(reason);
            this.@org.eclipse.che.ide.bootstrap.IdeBootstrap::notifyShowIDE()();
        } catch (e) {
            console.log(e.message);
        }
    }-*/;
}
