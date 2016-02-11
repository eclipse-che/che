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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.VirtualFile;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File entry.
 *
 * @author andrew00x
 */
public class FileEntry extends VirtualFileEntry {

    public FileEntry(String workspace, VirtualFile virtualFile) {
        super(workspace, virtualFile);
    }

    @Override
    public FileEntry copyTo(String newParent) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        return copyTo(newParent, getName(), false);
    }

    @Override
    public FileEntry copyTo(String newParent, String newName, boolean override) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if (Path.fromString(newParent).isRoot()) {
            throw new ServerException(String.format("Invalid path %s. Can't create file outside of project.", newParent));
        }
        final VirtualFile vf = getVirtualFile();
        final MountPoint mp = vf.getMountPoint();
        return new FileEntry(getWorkspace(), vf.copyTo(mp.getVirtualFile(newParent), newName, override));
    }

    @Override
    public void moveTo(String newParent) throws ConflictException, NotFoundException, ForbiddenException, ServerException {
        moveTo(newParent,null,false);
    }

    @Override
    public void moveTo(String newParent, String name, boolean overWrite) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if (Path.fromString(newParent).isRoot()) {
            throw new ServerException(String.format("Invalid path %s. Can't move this item outside of project.", newParent));
        }
        super.moveTo(newParent, name, overWrite); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Gets media type of this file.
     *
     * @throws ServerException
     *         if an error occurs
     * @see org.eclipse.che.api.vfs.server.VirtualFile#getMediaType()
     */
    public String getMediaType() throws ServerException {
        return getVirtualFile().getMediaType();
    }

    /**
     * Updates media type of this file.
     *
     * @param mediaType
     *         new media type
     * @throws ServerException
     *         if an error occurs
     * @see org.eclipse.che.api.vfs.server.VirtualFile#setMediaType(String)
     */
    public void setMediaType(String mediaType) throws ServerException {
        getVirtualFile().setMediaType(mediaType);
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
        return getContentStream().getStream();
    }

    /**
     * Gets content of file as array of bytes.
     *
     * @return content of file as stream
     * @throws IOException
     *         if an i/o error occurs
     * @throws ServerException
     *         if other error occurs
     */
    public byte[] contentAsBytes() throws IOException, ServerException {
        final ContentStream contentStream = getContentStream();
        final int contentLength = (int)contentStream.getLength();
        if (contentLength == 0) {
            return new byte[0];
        }
        try (InputStream stream = contentStream.getStream()) {
            if (contentLength < 0) {
                return ByteStreams.toByteArray(stream);
            }
            final byte[] b = new byte[contentLength];
            ByteStreams.readFully(stream, b);
            return b;
        }
    }

    private ContentStream getContentStream() throws ServerException {
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

    /**
     * Renames this file and update its media type.
     *
     * @param newName
     *         new name
     * @param newMediaType
     *         new media type
     * @throws ForbiddenException
     *         if rename operation is forbidden
     * @throws ConflictException
     *         if rename operation causes name conflict
     * @throws ServerException
     *         if other error occurs
     */
    public void rename(String newName, String newMediaType) throws ConflictException, ForbiddenException, ServerException {
        final VirtualFile rVf = getVirtualFile().rename(newName, newMediaType, null);
        setVirtualFile(rVf);
    }
}
