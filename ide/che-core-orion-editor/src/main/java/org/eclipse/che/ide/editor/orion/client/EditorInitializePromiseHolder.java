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
package org.eclipse.che.ide.editor.orion.client;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextThemeOverlay;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.requirejs.RequireJsLoader;
import org.eclipse.che.requirejs.RequirejsErrorHandler.RequireError;

/** Holds promise that resolve when all editor js scripts are loaded ad initialized */
@Singleton
public class EditorInitializePromiseHolder {

  /** The logger. */
  private static final Logger LOG =
      Logger.getLogger(EditorInitializePromiseHolder.class.getSimpleName());

  private final NotificationManager notificationManager;
  private final RequireJsLoader requireJsLoader;
  private final OrionResource orionResource;
  private final MessageLoader loader;
  private final String waitEditorMessage;

  private boolean initFailedWarnedOnce = false;
  private boolean cssLinkInjected = false;

  @Inject
  public EditorInitializePromiseHolder(
      final NotificationManager notificationManager,
      final RequireJsLoader requireJsLoader,
      final OrionResource orionResource,
      final LoaderFactory loaderFactory,
      final EditorLocalizationConstants constants) {
    this.notificationManager = notificationManager;
    this.requireJsLoader = requireJsLoader;
    this.orionResource = orionResource;
    this.loader = loaderFactory.newLoader();
    this.waitEditorMessage = constants.waitEditorInitMessage();
  }

  public Promise<Void> getInitializerPromise() {
    return AsyncPromiseHelper.createFromAsyncRequest(
        new AsyncPromiseHelper.RequestCall<Void>() {
          @Override
          public void makeCall(AsyncCallback<Void> callback) {
            injectOrion(callback);
          }
        });
  }

  private void injectOrion(final AsyncCallback<Void> callback) {
    loader.setMessage(waitEditorMessage);
    final String[] scripts =
        new String[] {"built-codeEdit/code_edit/built-codeEdit-amd", "orion/CheContentAssistMode"};

    requireJsLoader.require(
        new Callback<JavaScriptObject[], Throwable>() {
          @Override
          public void onSuccess(final JavaScriptObject[] result) {
            requireOrion(callback);
          }

          @Override
          public void onFailure(final Throwable e) {
            if (e instanceof JavaScriptException) {
              final JavaScriptException jsException = (JavaScriptException) e;
              final Object nativeException = jsException.getThrown();
              if (nativeException instanceof RequireError) {
                final RequireError requireError = (RequireError) nativeException;
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
        },
        scripts,
        new String[0]);

    injectCssLink(
        GWT.getModuleBaseForStaticFiles() + "built-codeEdit/code_edit/built-codeEdit.css");
  }

  private void injectCssLink(final String url) {
    // Avoid injecting built-codeEdit.css more than once as it may override
    // orion-codenvy-theme.css
    if (!cssLinkInjected) {
      final LinkElement link = Document.get().createLinkElement();
      link.setRel("stylesheet");
      link.setHref(url);
      Document.get().getHead().appendChild(link);
      cssLinkInjected = true;
    }
  }

  private void requireOrion(final AsyncCallback<Void> callback) {
    requireJsLoader.require(
        new Callback<JavaScriptObject[], Throwable>() {

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
        new String[] {
          "orion/codeEdit",
          "orion/editor/emacs",
          "orion/editor/vi",
          "orion/keyBinding",
          "che/editor/contentAssist",
          "orion/editor/eventTarget",
          "orion/uiUtils",
          "orion/editor/annotations"
        },
        new String[] {
          "CodeEditWidget",
          "OrionEmacs",
          "OrionVi",
          "OrionKeyBinding",
          "CheContentAssistMode",
          "OrionEventTarget",
          "UiUtils",
          "OrionAnnotations"
        });
  }

  private void endConfiguration(final AsyncCallback<Void> callback) {
    defineDefaultTheme();
    loader.hide();
    callback.onSuccess(null);
  }

  private void defineDefaultTheme() {
    // The codenvy theme uses both an orion css file and a CssResource
    orionResource.editorStyle().ensureInjected();
    orionResource.getIncrementalFindStyle().ensureInjected();
    OrionTextThemeOverlay.setDefaultTheme("orionCodenvy", "orion-codenvy-theme.css");
  }

  private void initializationFailed(
      final AsyncCallback<Void> callback, final String errorMessage, Throwable e) {
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
