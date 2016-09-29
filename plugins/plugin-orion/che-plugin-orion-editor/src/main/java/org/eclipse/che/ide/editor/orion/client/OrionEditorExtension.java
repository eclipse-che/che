/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextThemeOverlay;
import org.eclipse.che.ide.editor.orion.client.signature.SignatureHelpResources;
import org.eclipse.che.ide.requirejs.RequireJsLoader;
import org.eclipse.che.ide.requirejs.RequirejsErrorHandler.RequireError;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

@Extension(title = "Orion Editor", version = "1.1.0")
@Singleton
public class OrionEditorExtension {

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(OrionEditorExtension.class.getSimpleName());

    private final NotificationManager notificationManager;
    private final RequireJsLoader     requireJsLoader;
    private final OrionResource       orionResource;
    private final MessageLoader       loader;
    private final String              waitEditorMessage;

    private boolean initFailedWarnedOnce = false;

    @Inject
    public OrionEditorExtension(final NotificationManager notificationManager,
                                final RequireJsLoader requireJsLoader,
                                final EditorInitializePromiseHolder editorModule,
                                final OrionResource orionResource,
                                final LoaderFactory loaderFactory,
                                final EditorLocalizationConstants constants,
                                final SignatureHelpResources signatureHelpResources) {
        this.notificationManager = notificationManager;
        this.requireJsLoader = requireJsLoader;
        this.orionResource = orionResource;
        this.loader = loaderFactory.newLoader();
        this.waitEditorMessage = constants.waitEditorInitMessage();
        signatureHelpResources.css().ensureInjected();

        Promise<Void> promise = AsyncPromiseHelper.createFromAsyncRequest(new AsyncPromiseHelper.RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                injectOrion(callback);
            }
        });
        editorModule.setInitializerPromise(promise);

        KeyMode.init();
    }

    private void injectOrion(final AsyncCallback<Void> callback) {
        loader.setMessage(waitEditorMessage);
        final String[] scripts = new String[]{
                "built-codeEdit-12.0/code_edit/built-codeEdit-amd",
                "orion/CheContentAssistMode"
        };

        requireJsLoader.require(new Callback<JavaScriptObject[], Throwable>() {
            @Override
            public void onSuccess(final JavaScriptObject[] result) {
                requireOrion(callback);
            }

            @Override
            public void onFailure(final Throwable e) {
                if (e instanceof JavaScriptException) {
                    final JavaScriptException jsException = (JavaScriptException)e;
                    final Object nativeException = jsException.getThrown();
                    if (nativeException instanceof RequireError) {
                        final RequireError requireError = (RequireError)nativeException;
                        final String errorType = requireError.getRequireType();
                        String message = "Orion injection failed: " + errorType;
                        final JsArrayString modules = requireError.getRequireModules();
                        if (modules != null) {
                            message += modules.join(",");
                        }
                        Log.debug(OrionEditorExtension.class, message);
                    }
                }
                initializationFailed(callback, "Failed to inject Orion editor", e);
            }
        }, scripts, new String[0]);

        injectCssLink(GWT.getModuleBaseForStaticFiles() + "built-codeEdit-12.0/code_edit/built-codeEdit.css");
    }

    private static void injectCssLink(final String url) {
        final LinkElement link = Document.get().createLinkElement();
        link.setRel("stylesheet");
        link.setHref(url);
        Document.get().getHead().appendChild(link);
    }

    private void requireOrion(final AsyncCallback<Void> callback) {
        requireJsLoader.require(new Callback<JavaScriptObject[], Throwable>() {

            @Override
            public void onFailure(final Throwable reason) {
                LOG.log(Level.SEVERE, "Unable to initialize Orion ", reason);
                initializationFailed(callback, "Failed to initialize Orion editor", reason);
            }

            @Override
            public void onSuccess(final JavaScriptObject[] result) {
                endConfiguration(callback);
            }
        },
         new String[]{"orion/codeEdit",
                      "orion/editor/emacs",
                      "orion/editor/vi",
                      "orion/keyBinding",
                      "che/editor/contentAssist",
                      "orion/editor/eventTarget",
                      "orion/uiUtils"},
         new String[]{"CodeEditWidget",
                      "OrionEmacs",
                      "OrionVi",
                      "OrionKeyBinding",
                      "CheContentAssistMode",
                      "OrionEventTarget",
                      "UiUtils"});
    }

    private void endConfiguration(final AsyncCallback<Void> callback) {
        defineDefaultTheme();
        loader.hide();
        callback.onSuccess(null);
    }

    private void defineDefaultTheme() {
        // The codenvy theme uses both an orion css file and a CssResource
        orionResource.editorStyle().ensureInjected();
        OrionTextThemeOverlay.setDefaultTheme("orionCodenvy", "orion-codenvy-theme.css");
    }

    private void initializationFailed(final AsyncCallback<Void> callback, final String errorMessage, Throwable e) {
        if (initFailedWarnedOnce) {
            return;
        }
        loader.hide();
        initFailedWarnedOnce = true;
        notificationManager.notify(errorMessage, StatusNotification.Status.FAIL, FLOAT_MODE);
        LOG.log(Level.SEVERE, errorMessage + " - ", e);
        callback.onFailure(e);
    }
}
