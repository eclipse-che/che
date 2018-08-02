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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper for UiUtils that convert keyBinding to readable line
 *
 * @author Alexander Andrienko
 */
public class UiUtilsOverlay extends JavaScriptObject {
  protected UiUtilsOverlay() {}

  /**
   * Convert keyBindings to readable line
   *
   * @param uiUtilsModule uiUtilsModule that convert keyBindings to readable line
   * @param keyBinding target
   * @return readable line
   */
  public static final native String getUserKeyString(
      JavaScriptObject uiUtilsModule, OrionKeyBindingOverlay keyBinding) /*-{
        return uiUtilsModule.getUserKeyString(keyBinding);
    }-*/;
}
