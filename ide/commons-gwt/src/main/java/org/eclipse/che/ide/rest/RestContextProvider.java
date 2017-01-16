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
package org.eclipse.che.ide.rest;

import com.google.inject.Provider;

/**
 * Getting setting by native JS
 * $wnd.IDE.config.restContext
 *
 * @author Vitaly Parfonov
 */
@Deprecated
public class RestContextProvider implements Provider<String> {


    @Override
    public String get() {
        return getRestContext();
    }

    private static native String getRestContext() /*-{
        if ($wnd.IDE && $wnd.IDE.config) {
            return $wnd.IDE.config.restContext;
        } else {
            return null;
        }
    }-*/;
}
