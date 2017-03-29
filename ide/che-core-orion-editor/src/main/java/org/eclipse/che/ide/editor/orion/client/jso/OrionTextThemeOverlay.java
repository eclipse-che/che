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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

public class OrionTextThemeOverlay extends JavaScriptObject {

    protected OrionTextThemeOverlay() {
    }

    public final static void setDefaultTheme(String className, String themeHref) {
        setTheme("default", className, themeHref);
    }

    public final static OrionTextThemeOverlay getDefautTheme() {
        return getTheme("default");
    }

    public final static native void setTheme(String themeName, String className, String themeHref) /*-{
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
    public final static native OrionTextThemeOverlay getTheme(String themeName) /*-{
        $wnd.require([ "orion/editor/textTheme" ], function(mTextTheme) {
            var theme = mTextTheme.TextTheme.getTheme(themeName);
            return theme;
        });
    }-*/;
}
