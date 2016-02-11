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
package org.eclipse.che.api.vfs.server;

import javax.servlet.http.HttpServletRequest;

/**
 * Prevent access to VirtualFileSystem REST API from outside the IDE.
 *
 * @author andrew00x
 */
public final class RefererHeaderValidator implements RequestValidator {
    @Override
    public void validate(HttpServletRequest request) {
        String requestURL = request.getScheme() + "://" + request.getServerName();
        int port = request.getServerPort();
        if (port != 80 && port != 443) {
            requestURL += (":" + port);
        }
        String referer = request.getHeader("Referer");
        if (referer == null || !referer.startsWith(requestURL)) {
            throw new RuntimeException("Access forbidden from outside of IDE. ");
        }
    }
}
