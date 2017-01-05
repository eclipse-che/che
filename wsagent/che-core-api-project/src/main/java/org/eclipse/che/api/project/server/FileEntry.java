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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.VirtualFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File entry.
 *
 * @author andrew00x
 */
public class FileEntry extends VirtualFileEntry {

    public FileEntry(VirtualFile virtualFile, ProjectRegistry registry) throws ServerException {
        super(virtualFile, registry);
    }

    /**
     * Gets content of file as stream.
     *
     * @return content of file as stream
     * @throws IOException
     *         if an i/o error occurs
     * @throws ServerException
     *         if other error occurs
     */
    public InputStream getInputStream() throws IOException, ServerException {
        return getContentStream();
    }

    /**
     * Gets content of file as array of bytes.
     *
     * @return content of file as stream
     * @throws ServerException
     *         if other error occurs
     */
    public byte[] contentAsBytes() throws ServerException {
        try {
            return getVirtualFile().getContentAsBytes();
        } catch (ForbiddenException e) {
            // A ForbiddenException might be thrown if backend VirtualFile isn't regular file but folder. This isn't expected here.
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private InputStream getContentStream() throws ServerException {
        try {
            return getVirtualFile().getContent();
        } catch (ForbiddenException e) {
            // A ForbiddenException might be thrown if backend VirtualFile isn't regular file but folder. This isn't expected here.
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Updates content of file.
     *
     * @param content
     *         new content
     * @throws ForbiddenException
     *         if update operation is forbidden
     * @throws ServerException
     *         if other error occurs
     */
    public void updateContent(byte[] content) throws ForbiddenException, ServerException {
        updateContent(new ByteArrayInputStream(content));
    }

    /**
     * Updates content of file.
     *
     * @param content
     *         new content
     * @throws ForbiddenException
     *         if update operation is forbidden
     * @throws ServerException
     *         if other error occurs
     */
    public void updateContent(InputStream content) throws ForbiddenException, ServerException {
        getVirtualFile().updateContent(content, null);
    }
}
