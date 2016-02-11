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
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for VirtualFile.
 *
 * @author andrew00x
 */
public abstract class VirtualFileEntry {
    private final String      workspace;
    private       VirtualFile virtualFile;
    protected Map<String, String> attributes;

    public VirtualFileEntry(String workspace, VirtualFile virtualFile) {
        this.workspace = workspace;
        this.virtualFile = virtualFile;
        this.attributes = new HashMap<>();
    }

    /**
     *
     * @return creation date
     */
    public long getCreated() {
        return virtualFile.getCreationDate();
    }

    /**
     *
     * @return last modification date
     */
    public long getModified() {
        return virtualFile.getLastModificationDate();
    }

    /** Gets id of workspace which file belongs to. */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Tests whether this item is a regular file.
     *
     * @see org.eclipse.che.api.vfs.server.VirtualFile#isFile()
     */
    public boolean isFile() {
        return virtualFile.isFile();
    }

    /**
     * Tests whether this item is a folder.
     *
     * @see org.eclipse.che.api.vfs.server.VirtualFile#isFolder()
     */
    public boolean isFolder() {
        return virtualFile.isFolder();
    }

    /**
     * Gets name.
     *
     * @see org.eclipse.che.api.vfs.server.VirtualFile#getName()
     */
    public String getName() {
        return virtualFile.getName();
    }

    /**
     * Gets path.
     *
     * @see org.eclipse.che.api.vfs.server.VirtualFile#getPath()
     */
    public String getPath() {
        return virtualFile.getPath();
    }

    /**
     * Gets parent folder. If this item is root folder this method always returns {@code null}.
     *
     * @see org.eclipse.che.api.vfs.server.VirtualFile#getParent()
     * @see org.eclipse.che.api.vfs.server.VirtualFile#isRoot()
     */
    public FolderEntry getParent() {
        if (virtualFile.isRoot()) {
            return null;
        }
        return new FolderEntry(workspace, virtualFile.getParent());
    }

    /**
     * Deletes this item.
     *
     * @throws ForbiddenException
     *         if delete operation is forbidden
     * @throws ServerException
     *         if other error occurs
     */
    public void remove() throws ServerException, ForbiddenException {
        virtualFile.delete(null);
    }

    /**
     * Creates copy of this item in new parent.
     *
     * @param newParent
     *         path of new parent
     * @throws NotFoundException
     *         if {@code newParent} doesn't exist
     * @throws ForbiddenException
     *         if copy operation is forbidden
     * @throws ConflictException
     *         if copy operation causes conflict, e.g. name conflict
     * @throws ServerException
     *         if other error occurs
     */
    public abstract VirtualFileEntry copyTo(String newParent)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Creates copy of this item in new parent.
     *
     * @param newParent path of new parent
     * @param name new name for destination
     * @param override true to overwrite destination
     * @throws NotFoundException if {@code newParent} doesn't exist
     * @throws ForbiddenException if copy operation is forbidden
     * @throws ConflictException if copy operation causes conflict, e.g. name
     * conflict
     * @throws ServerException if other error occurs
     */
    public abstract VirtualFileEntry copyTo(String newParent, String name, boolean override)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Moves this item to the new parent.
     *
     * @param newParent
     *         path of new parent
     * @throws NotFoundException
     *         if {@code newParent} doesn't exist
     * @throws ForbiddenException
     *         if move operation is forbidden
     * @throws ConflictException
     *         if move operation causes conflict, e.g. name conflict
     * @throws ServerException
     *         if other error occurs
     */
    public void moveTo(String newParent) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        moveTo(newParent, null, false);
    }

    /**
     * Moves this item to the new parent.
     *
     * @param newParent path of new parent
     * @param name new name for destination
     * @param overWrite true to overwrite destination
     * @throws NotFoundException if {@code newParent} doesn't exist
     * @throws ForbiddenException if move operation is forbidden
     * @throws ConflictException if move operation causes conflict, e.g. name
     * conflict
     * @throws ServerException if other error occurs
     */
    public void moveTo(String newParent, String name, boolean overWrite) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final MountPoint mp = virtualFile.getMountPoint();
        virtualFile = virtualFile.moveTo(mp.getVirtualFile(newParent), name, overWrite, null);
    }

    /**
     * Renames this item.
     *
     * @param newName
     *         new name
     * @throws ForbiddenException
     *         if rename operation is forbidden
     * @throws ConflictException
     *         if rename operation causes name conflict
     * @throws ServerException
     *         if other error occurs
     */
    public void rename(String newName) throws ConflictException, ForbiddenException, ServerException {
        virtualFile = virtualFile.rename(newName, null, null);
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    void setVirtualFile(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
    }
}
