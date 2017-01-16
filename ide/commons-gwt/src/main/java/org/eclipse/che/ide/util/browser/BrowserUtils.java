// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.browser;


import org.eclipse.che.ide.util.StringUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;

/** Utility methods relating to the browser. */
public abstract class BrowserUtils {

    private static final BrowserUtils INSTANCE = GWT.create(BrowserUtils.class);

    abstract boolean isFFox();

    static class Chrome extends BrowserUtils {
        Chrome() {
        }

        @Override
        boolean isFFox() {
            return false;
        }
    }

    static class Firefox extends BrowserUtils {
        Firefox() {
        }

        @Override
        boolean isFFox() {
            return true;
        }
    }

    public static boolean isFirefox() {
        return INSTANCE.isFFox();
    }

    public static boolean isChromeOs() {
        return Window.Navigator.getUserAgent().contains(" CrOS ");
    }

    public static boolean isIPad() {
        return Window.Navigator.getAppVersion().contains("iPad");
    }

    public static boolean isIphone() {
        return Window.Navigator.getAppVersion().contains("iPhone");
    }

    public static boolean isAndroid() {
        return Window.Navigator.getAppVersion().contains("Android");
    }

    public static boolean hasUrlParameter(String parameter) {
        return Location.getParameter(parameter) != null;
    }

    public static boolean hasUrlParameter(String parameter, String value) {
        return StringUtils.equalNonEmptyStrings(Location.getParameter(parameter), value);
    }

    /**
     * Wrapper for calling {@link Location#reload()} with ability to force reloading page.
     * <p>
     * By default, the reload() method reloads the page from the cache, but you can force
     * it to reload the page from the server by setting the forceGet parameter to true: location.reload(true).
     * <p>
     * See more details about argument {@code forceGet} here: http://www.w3schools.com/jsref/met_loc_reload.asp
     *
     * @param forceGet
     *         reloads the current page from the server
     */
    public static native void reloadPage(boolean forceGet) /*-{
        $wnd.location.reload(forceGet);
    }-*/;

    private BrowserUtils() {
    }
}
