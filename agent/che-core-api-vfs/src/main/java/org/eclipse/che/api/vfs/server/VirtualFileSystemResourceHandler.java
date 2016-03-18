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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * URLStreamHandler for 'ide+vfs' protocol.
 *
 * @author andrew00x
 */
public final class VirtualFileSystemResourceHandler extends URLStreamHandler {
    private final VirtualFileSystemRegistry registry;

    /**
     * @param registry
     *         virtual file system registry
     */
    public VirtualFileSystemResourceHandler(VirtualFileSystemRegistry registry) {
        this.registry = registry;
    }

    /** @see java.net.URLStreamHandler#openConnection(java.net.URL) */
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new VirtualFileSystemURLConnection(url, registry);
    }
}
