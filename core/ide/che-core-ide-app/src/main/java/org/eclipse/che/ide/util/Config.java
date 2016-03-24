/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.util;

/**
 * A smattering of useful methods.
 *
 * @author Dmytro Nochevnov
 * @author Vitaliy Guliy
 * @author Dmitry Shnurenko
 */

public class Config {

    public static native String getCheExtensionPath() /*-{
        try {
            return $wnd.IDE.config.cheExtensionPath;
        } catch (e) {
            return null;
        }
    }-*/;
}
