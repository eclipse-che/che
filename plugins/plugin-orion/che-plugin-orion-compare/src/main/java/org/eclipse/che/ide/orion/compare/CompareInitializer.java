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
package org.eclipse.che.ide.orion.compare;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.requirejs.RequireJsLoader;

/** @author Mykola Morhun */
@Singleton
public class CompareInitializer {

  public static final String GIT_COMPARE_MODULE = "Compare";

  private final RequireJsLoader requireJsLoader;
  private final ThemeAgent themeAgent;
  private DialogFactory dialogFactory;

  @Inject
  CompareInitializer(
      final RequireJsLoader requireJsLoader,
      final ThemeAgent themeAgent,
      final DialogFactory dialogFactory) {
    this.requireJsLoader = requireJsLoader;
    this.themeAgent = themeAgent;
    this.dialogFactory = dialogFactory;
  }

  public Promise<Void> injectCompareWidget(final AsyncCallback<Void> callback) {
    loadCompareTheme();
    return AsyncPromiseHelper.createFromAsyncRequest(
        call ->
            requireJsLoader.require(
                new Callback<JavaScriptObject[], Throwable>() {
                  @Override
                  public void onFailure(Throwable reason) {
                    callback.onFailure(reason);
                  }

                  @Override
                  public void onSuccess(JavaScriptObject[] result) {
                    callback.onSuccess(null);
                    registerPromptFunction();
                  }
                },
                new String[] {"built-compare/built-compare-amd.min"},
                new String[] {GIT_COMPARE_MODULE}));
  }

  /**
   * Dynamically loads theme for compare widget. This is done here to not to load big css when user
   * doesn't need it.
   */
  private void loadCompareTheme() {
    String themeUrl =
        GWT.getModuleBaseURL()
            + '/'
            + themeAgent.getCurrentThemeId()
            + "-built-compare-codenvy.css";

    Element styleSheetLink = Document.get().createElement("link");
    styleSheetLink.setAttribute("rel", "stylesheet");
    styleSheetLink.setAttribute("href", themeUrl);
    Document.get().getElementsByTagName("head").getItem(0).appendChild(styleSheetLink);
  }

  /**
   * Registers global prompt function to be accessible directly from JavaScript.
   *
   * <p>Function promptIdeCompareWidget(title, text, defaultValue, callback) title Dialog title text
   * The text to display in the dialog box defaultValue The default value callback function(value)
   * clicking "OK" will return input value clicking "Cancel" will return null
   */
  private native int registerPromptFunction() /*-{
        if (!$wnd["promptIdeCompareWidget"]) {
            var instance = this;
            $wnd["promptIdeCompareWidget"] = function (title, text, defaultValue, callback) {
                instance.@org.eclipse.che.ide.orion.compare.CompareInitializer::askLineNumber(*)(title,
                    text, defaultValue, callback);
            };
        }
    }-*/;

  /** Custom callback to pass given value to native javascript function. */
  private class InputCallback implements org.eclipse.che.ide.ui.dialogs.input.InputCallback {

    private JavaScriptObject callback;

    public InputCallback(JavaScriptObject callback) {
      this.callback = callback;
    }

    @Override
    public void accepted(String value) {
      Scheduler.get().scheduleDeferred((Command) () -> acceptedNative(value));
    }

    private native void acceptedNative(String value) /*-{
            var callback = this.@org.eclipse.che.ide.orion.compare.CompareInitializer.InputCallback::callback;
            callback(value);
        }-*/;
  }

  private void askLineNumber(
      String title, String text, String defaultValue, final JavaScriptObject callback) {
    if (defaultValue == null) {
      defaultValue = "";
    } else {
      // It's strange situation defaultValue.length() returns 'undefined' but must return a number.
      // Reinitialise the variable resolves the problem.
      defaultValue = "" + defaultValue;
    }

    dialogFactory
        .createInputDialog(
            title, text, defaultValue, 0, defaultValue.length(), new InputCallback(callback), null)
        .show();
  }
}
