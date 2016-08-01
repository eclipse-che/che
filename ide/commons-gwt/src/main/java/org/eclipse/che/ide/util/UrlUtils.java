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

import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;

public class UrlUtils {

    public static String fixHostName(String internalUrl) {
        if (GWT.isScript()) {
            String hostnameInBrowser = Window.Location.getHostName();
            return replaceHostNameInUrl(internalUrl, hostnameInBrowser);
        } else {
            return internalUrl;
        }
    }

    protected static String replaceHostNameInUrl(String url, String newHostName) {
        RegExp re = RegExp.compile("(\\://).+(?=[\\:])");
        return re.replace(url, "$1" + newHostName);
    }

}
