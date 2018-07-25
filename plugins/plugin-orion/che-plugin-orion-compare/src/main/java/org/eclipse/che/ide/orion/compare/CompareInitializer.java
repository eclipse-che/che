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
package org.eclipse.che.ide.orion.compare;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.requirejs.RequireJsLoader;

/** @author Mykola Morhun */
@Singleton
public class CompareInitializer {

  public static final String GIT_COMPARE_MODULE = "Compare";

  private final RequireJsLoader requireJsLoader;
  private final ThemeAgent themeAgent;

  @Inject
  CompareInitializer(final RequireJsLoader requireJsLoader, final ThemeAgent themeAgent) {
    this.requireJsLoader = requireJsLoader;
    this.themeAgent = themeAgent;
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
}
