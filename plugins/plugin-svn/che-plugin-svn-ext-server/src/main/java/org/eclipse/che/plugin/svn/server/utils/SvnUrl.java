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
package org.eclipse.che.plugin.svn.server.utils;

/**
 * Subversion utilities.
 */
public class SvnUrl {

    private SvnUrl() { }

    /**
     * Indicates if uri represents ssh connection.
     *
     * @param uri
     *      the url to svn repository
     */
    public static boolean isSSH(String uri) {
        return uri != null && uri.startsWith("svn+ssh://");
    }
}
