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

package org.eclipse.che.ide.js.plugin.model;

import com.google.gwt.core.client.JavaScriptObject;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.js.api.context.PluginContext;

/** @author Yevhen Vydolob */
@JsType(isNative = true)
public interface PluginEntryPoint {

  @JsOverlay
  static PluginEntryPoint of(JavaScriptObject o) {
    return o.cast();
  }

  void activate(PluginContext ctx);

  void deactivate(PluginContext ctx);
}
