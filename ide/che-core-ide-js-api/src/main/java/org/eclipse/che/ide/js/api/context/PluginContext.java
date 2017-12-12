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

package org.eclipse.che.ide.js.api.context;

import elemental.js.util.JsArrayOf;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.api.Disposable;
import org.eclipse.che.ide.js.api.JsApi;

/**
 * The plugin context object. Passed as argument in plugin activate function
 *
 * @author Yevhen Vydolob
 */
@JsType
public class PluginContext {

  private JsApi api;
  private JsArrayOf<Disposable> disposables;

  public PluginContext(JsApi api) {
    this.api = api;
    this.disposables = JsArrayOf.create();
  }

  public JsApi getApi() {
    return api;
  }

  public void setApi(JsApi api) {
    this.api = api;
  }

  public void addDisposable(Disposable disposable) {
    disposables.push(disposable);
  }

  public JsArrayOf<Disposable> getDisposables() {
    return disposables;
  }
}
