/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
