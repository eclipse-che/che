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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

public class OrionTextThemeOverlay extends JavaScriptObject {

  protected OrionTextThemeOverlay() {}

  public static final void setDefaultTheme(String className, String themeHref) {
    setTheme("default", className, themeHref);
  }

  public static final OrionTextThemeOverlay getDefautTheme() {
    return getTheme("default");
  }

  public static final native void setTheme(
      String themeName, String className, String themeHref) /*-{
        $wnd.require([ "orion/editor/textTheme" ], function(mTextTheme) {
            var basePath = @com.google.gwt.core.client.GWT::getModuleBaseForStaticFiles()();
            var theme = mTextTheme.TextTheme.getTheme(themeName);
            theme.setThemeClass(className, { href : basePath + themeHref });
        });
    }-*/;

  /**
   * Returns the instance of TextTheme by name. If it doesn't exist, it is created.
   *
   * @param themeName the name of the theme
   * @return a javascript object describing the theme
   */
  public static final native OrionTextThemeOverlay getTheme(String themeName) /*-{
        $wnd.require([ "orion/editor/textTheme" ], function(mTextTheme) {
            var theme = mTextTheme.TextTheme.getTheme(themeName);
            return theme;
        });
    }-*/;
}
