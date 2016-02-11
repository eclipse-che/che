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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.shared.ItemType;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.File;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ItemList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Connection to virtual file system. Instances of this class are not safe to be used by multiple concurrent threads.
 *
 * @author <a href="mailto:aparfonov@exoplatform.com">Andrey Parfonov</a>
 */
public final class VirtualFileSystemURLConnection extends URLConnection {
    private static final Logger LOG = LoggerFactory.getLogger(VirtualFileSystemURLConnection.class);

    private final VirtualFileSystemRegistry registry;

    private VirtualFileSystem vfs;

    private Item item;

    /**
     * @param url
     *         the URL
     * @param registry
     *         virtual file system registry
     */
    public VirtualFileSystemURLConnection(URL url, VirtualFileSystemRegistry registry) {
        super(check(url)); // Be sure URL is correct.
        this.registry = registry;
    }

    private static URL check(URL url) {
        if (!"ide+vfs".equals(url.getProtocol())) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        return url;
    }

    /** @see java.net.URLConnection#connect() */
    @Override
    public void connect() throws IOException {
        final URI theUri = URI.create(getURL().toString());
        final String path = theUri.getPath();
        final String vfsId =
                (path == null || "/".equals(path)) ? null : (path.startsWith("/")) ? path.substring(1) : path;
        try {
            vfs = registry.getProvider(vfsId).newInstance(null);
            final String itemIdentifier = theUri.getFragment();
            item = (itemIdentifier.startsWith("/")) //
                   ? vfs.getItemByPath(itemIdentifier, null, false, PropertyFilter.NONE_FILTER) //
                   : vfs.getItem(itemIdentifier, false, PropertyFilter.NONE_FILTER);
        } catch (ForbiddenException | NotFoundException | ServerException e) {
            throw new IOException(e.getMessage(), e);
        }
        connected = true;
    }

    public void disconnect() {
        item = null;
        vfs = null;
        connected = false;
    }

    /** @see java.net.URLConnection#getContentLength() */
    @Override
    public int getContentLength() {
        try {
            if (!connected) {
                connect();
            }
            if (item.getItemType() == ItemType.FILE) {
                return (int)((File)item).getLength();
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }
        return -1;
    }

    /** @see java.net.URLConnection#getContentType() */
    @Override
    public String getContentType() {
        try {
            if (!connected) {
                connect();
            }
            return item.getMimeType();
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }
        return null;
    }

    /** @see java.net.URLConnection#getLastModified() */
    @Override
    public long getLastModified() {
        try {
            if (!connected) {
                connect();
            }
            if (item.getItemType() == ItemType.FILE) {
                return ((File)item).getLastModificationDate();
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(e.getMessage(), e);
            }
        }
        return 0;
    }

    /** @see java.net.URLConnection#getContent() */
    @Override
    public Object getContent() throws IOException {
        if (!connected) {
            connect();
        }
        return item;
    }

    /** @see java.net.URLConnection#getContent(java.lang.Class[]) */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getContent(Class[] classes) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** @see java.net.URLConnection#getInputStream() */
    @Override
    public InputStream getInputStream() throws IOException {
        if (!connected) {
            connect();
        }
        try {
            if (item.getItemType() == ItemType.FILE) {
                ContentStream content = vfs.getContent(item.getId());
                return content.getStream();
            }
            // Folder. Show plain list of child.
            ItemList children = vfs.getChildren(item.getId(), -1, 0, null, false, PropertyFilter.NONE_FILTER);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Writer w = new OutputStreamWriter(out);
            for (Item i : children.getItems()) {
                w.write(i.getName());
                w.write('\n');
            }
            w.flush();
            w.close();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (ForbiddenException | ConflictException | NotFoundException | ServerException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}