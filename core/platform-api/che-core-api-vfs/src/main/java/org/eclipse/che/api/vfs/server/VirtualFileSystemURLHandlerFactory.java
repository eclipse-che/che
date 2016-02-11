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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Factory for URLStreamHandler to <code>ide+vfs</code> protocol.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 */
public final class VirtualFileSystemURLHandlerFactory implements URLStreamHandlerFactory {
    private final URLStreamHandlerFactory delegate;

    private final VirtualFileSystemRegistry registry;

    /**
     * @param delegate
     *         factory which we should ask to create URLStreamHandler if current factory does not support
     *         requested protocol.
     * @param registry
     *         set of all available virtual file systems
     */
    public VirtualFileSystemURLHandlerFactory(URLStreamHandlerFactory delegate, VirtualFileSystemRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    /** @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String) */
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("ide+vfs".equals(protocol)) {
            return new VirtualFileSystemResourceHandler(registry);
        } else if (delegate != null) {
            delegate.createURLStreamHandler(protocol);
        }
        return null;
    }
}
