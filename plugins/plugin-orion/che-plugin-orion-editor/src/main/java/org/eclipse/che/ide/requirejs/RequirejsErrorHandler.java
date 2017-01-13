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
package org.eclipse.che.ide.requirejs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * Interface for requirejs error callbacks.
 *
 * @author "Mickaël Leduque"
 */
public interface RequirejsErrorHandler {

    /**
     * Called when a requirejs operation fails.
     *
     * @param error
     *         the error object
     */
    void onError(RequireError error);

    class RequireError extends JavaScriptObject {
        protected RequireError() {
        }

        public final native String getRequireType() /*-{
            return this.requireType;
        }-*/;

        public final native JsArrayString getRequireModules() /*-{
            return this.requireModules;
        }-*/;

        public final native String getMessage() /*-{
            return this.message;
        }-*/;
    }
}
