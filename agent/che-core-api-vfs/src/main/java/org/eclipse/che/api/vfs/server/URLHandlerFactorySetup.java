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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Setup {@link URLStreamHandlerFactory} to be able use URL for access to virtual file system. It is not possible to
 * provide
 * correct {@link URLStreamHandler} by system property 'java.protocol.handler.pkgs'. Bug in Oracle JDK:
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4648098
 *
 * @author andrew00x
 */
public class URLHandlerFactorySetup {
    private static final Logger LOG = LoggerFactory.getLogger(URLHandlerFactorySetup.class);

    public synchronized static void setup(VirtualFileSystemRegistry registry) {
        try {
            new URL("ide+vfs", "", "");
        } catch (MalformedURLException mue) {
            // URL with protocol 'ide+vfs' is not supported yet. Need register URLStreamHandlerFactory.

            if (LOG.isDebugEnabled()) {
                LOG.debug("--> Try setup URLStreamHandlerFactory for protocol 'ide+vfs'. ");
            }
            try {
                // Get currently installed URLStreamHandlerFactory.
                Field factoryField = URL.class.getDeclaredField("factory");
                factoryField.setAccessible(true);
                URLStreamHandlerFactory currentFactory = (URLStreamHandlerFactory)factoryField.get(null);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("--> Current instance of URLStreamHandlerFactory: "
                              + (currentFactory != null ? currentFactory.getClass().getName() : null));
                }

                //
                URLStreamHandlerFactory vfsURLFactory = new VirtualFileSystemURLHandlerFactory(currentFactory, registry);
                factoryField.set(null, vfsURLFactory);
            } catch (SecurityException | NoSuchFieldException | IllegalAccessException se) {
                throw new RuntimeException(se.getMessage(), se);
            }

            // Check 'ide+vfs' again. From now it should be possible to use such URLs.
            // At the same time we force URL to remember our protocol handler.
            // URL knows about it even if the URLStreamHandlerFactory is changed.

            try {
                new URL("ide+vfs", "", "");

                //
                if (LOG.isDebugEnabled()) {
                    LOG.debug("--> URLStreamHandlerFactory installed. ");
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    @Singleton
    static class Initializer {
        final VirtualFileSystemRegistry registry;

        @Inject
        Initializer(VirtualFileSystemRegistry registry) {
            this.registry = registry;
        }

        @PostConstruct
        void init() {
            URLHandlerFactorySetup.setup(registry);
        }
    }

    protected URLHandlerFactorySetup() {
    }
}
