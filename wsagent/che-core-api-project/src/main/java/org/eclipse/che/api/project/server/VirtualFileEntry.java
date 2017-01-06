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
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for {@link VirtualFile}.
 *
 * @author andrew00x
 */
public abstract class VirtualFileEntry {

    private   VirtualFile         virtualFile;
    protected Map<String, String> attributes;
    protected ProjectRegistry     projectRegistry;

    VirtualFileEntry(VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        this.attributes = new HashMap<>();
    }

    VirtualFileEntry(VirtualFile virtualFile, ProjectRegistry projectRegistry) throws ServerException {
        this.virtualFile = virtualFile;
        this.attributes = new HashMap<>();
        this.projectRegistry = projectRegistry;
    }

    /**
     * @return last modification date
     */
    public long getModified() {
        return virtualFile.getLastModificationDate();
    }

    /**
     * Tests whether this item is a regular file.
     *
     * @see org.eclipse.che.api.vfs.VirtualFile#isFile()
     */
    public boolean isFile() {
        return virtualFile.isFile();
    }

    /**
     * Tests whether this item is a folder.
     *
     * @see org.eclipse.che.api.vfs.VirtualFile#isFolder()
     */
    public boolean isFolder() {
        return virtualFile.isFolder();
    }

    /**
     * Gets name.
     *
     * @see org.eclipse.che.api.vfs.VirtualFile#getName()
     */
    public String getName() {
        return virtualFile.getName();
    }

    /**
     * Gets path.
     *
     * @see org.eclipse.che.api.vfs.VirtualFile#getPath()
     */
    public Path getPath() {
        return virtualFile.getPath();
    }

    /**
     * @return project this item belongs to
     */
    public String getProject() {
        if (projectRegistry == null) {
            return null;
        }

        final RegisteredProject parentProject = projectRegistry.getParentProject(getPath().toString());
        if (parentProject == null) {
            return null;
        }

        return parentProject.getPath();
    }

    /**
     * @return whether the item is project
     */
    public boolean isProject() {
        // root
        if (projectRegistry == null || getProject() == null) {
            return false;
        }

        return getProject().equals(getPath().toString());
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

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
}
