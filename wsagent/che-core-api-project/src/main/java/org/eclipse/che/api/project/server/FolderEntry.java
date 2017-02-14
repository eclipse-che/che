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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Folder entry.
 *
 * @author andrew00x
 */
public class FolderEntry extends VirtualFileEntry {

    private static final VirtualFileFilter FOLDER_FILTER      = VirtualFile::isFolder;
    private static final VirtualFileFilter FILES_FILTER       = VirtualFile::isFile;
    private static final VirtualFileFilter FILE_FOLDER_FILTER = file -> (file.isFile() || file.isFolder());

    /**
     * Project's folder
     *
     * @param virtualFile
     */
    public FolderEntry(VirtualFile virtualFile) {
        super(virtualFile);
    }

    public FolderEntry(VirtualFile virtualFile, ProjectRegistry registry) throws ServerException {
        super(virtualFile, registry);
    }

    /**
     * Get child by relative path.
     *
     * @param path
     *         relative path
     * @return child
     * @throws ServerException
     *         if other error occurs
     */
    public VirtualFileEntry getChild(String path) throws ServerException {
        final VirtualFile child = getVirtualFile().getChild(Path.of(path));
        if (child == null) {
            return null;
        }

        if (child.isFile()) {
            return new FileEntry(child, projectRegistry);
        } else {
            return new FolderEntry(child, projectRegistry);
        }
    }

    /**
     * Get child by relative path.
     *
     * @param path
     *         relative path
     * @return child folder if found
     * @throws ServerException
     *         if other error occurs
     */
    public FolderEntry getChildFolder(String path) throws ServerException {
        final VirtualFile child = getVirtualFile().getChild(Path.of(path));

        if (child == null || child.isFile()) {
            return null;
        } else {
            return new FolderEntry(child, projectRegistry);
        }
    }

    /**
     * Get children of this folder. If current user doesn't have read access to some child they aren't added in result list.
     *
     * @throws ServerException
     *         if an error occurs
     */
    public List<VirtualFileEntry> getChildren() throws ServerException {
        return getChildren(VirtualFileFilter.ACCEPT_ALL);
    }

    /**
     * Get child files of this folder. If current user doesn't have read access to some child they aren't added in result list.
     *
     * @throws ServerException
     *         if an error occurs
     */
    public List<FileEntry> getChildFiles() throws ServerException {
        List<VirtualFile> vfChildren = getVirtualFile().getChildren(FILES_FILTER);
        final List<FileEntry> children = new ArrayList<>();
        for (VirtualFile c : vfChildren) {
            children.add(new FileEntry(c, projectRegistry));
        }
        return children;
    }

    /**
     * Gets child folders of this folder. If current user doesn't have read access to some child they aren't added in result list.
     *
     * @throws ServerException
     *         if an error occurs
     */
    public List<FolderEntry> getChildFolders() throws ServerException {
        List<VirtualFile> vfChildren = getVirtualFile().getChildren(FOLDER_FILTER);
        final List<FolderEntry> children = new ArrayList<>();
        for (VirtualFile c : vfChildren) {
            children.add(new FolderEntry(c, projectRegistry));
        }
        return children;
    }

    /**
     * Gets child folders and files of this folder. If current user doesn't have read access to some child they aren't added in result
     * list.
     *
     * @throws ServerException
     *         if an error occurs
     */
    public List<VirtualFileEntry> getChildFoldersFiles() throws ServerException {
        return getChildren(FILE_FOLDER_FILTER);
    }

    public List<VirtualFileEntry> getChildren(VirtualFileFilter filter) throws ServerException {
        final List<VirtualFile> vfChildren = getVirtualFile().getChildren(filter);

        final List<VirtualFileEntry> children = new ArrayList<>();
        for (VirtualFile vf : vfChildren) {
            if (vf.isFile()) {
                children.add(new FileEntry(vf, projectRegistry));
            } else {
                children.add(new FolderEntry(vf, projectRegistry));
            }
        }
        return children;
    }

    /**
     * Creates new file in this folder.
     *
     * @param name
     *         name
     * @param content
     *         content. In case of {@code null} empty file is created
     * @return newly create VirtualFile
     * @throws ForbiddenException
     *         if copy operation is forbidden
     * @throws ConflictException
     *         if operation causes conflict, e.g. name conflict
     * @throws ServerException
     *         if other error occurs
     * @see VirtualFile#createFile(String, InputStream)
     */
    public FileEntry createFile(String name, byte[] content) throws ForbiddenException, ConflictException, ServerException {
        if (isRoot(getVirtualFile())) {
            throw new ForbiddenException("Can't create file in root folder.");
        }
        return createFile(name, content == null ? null : new ByteArrayInputStream(content));
    }

    /**
     * Creates new file in this folder.
     *
     * @param name
     *         name
     * @param content
     *         content. In case of {@code null} empty file is created
     * @return newly create VirtualFile
     * @throws ForbiddenException
     *         if copy operation is forbidden
     * @throws ConflictException
     *         if operation causes conflict, e.g. name conflict
     * @throws ServerException
     *         if other error occurs
     * @see VirtualFile#createFile(String, InputStream)
     */
    public FileEntry createFile(String name, InputStream content) throws ForbiddenException, ConflictException, ServerException {
        if (isRoot(getVirtualFile())) {
            throw new ForbiddenException("Can't create file in root folder.");
        }
        return new FileEntry(getVirtualFile().createFile(name, content), projectRegistry);
    }

    /**
     * Creates new VirtualFile which denotes folder and use this one as parent folder.
     *
     * @param name
     *         name. If name is string separated by '/' all nonexistent parent folders must be created.
     * @return newly create VirtualFile that denotes folder
     * @throws ForbiddenException
     *         if copy operation is forbidden
     * @throws ConflictException
     *         if item with specified {@code name} already exists
     * @throws ServerException
     *         if other error occurs
     */
    public FolderEntry createFolder(String name) throws ConflictException, ServerException, ForbiddenException {
        return new FolderEntry(getVirtualFile().createFolder(name), projectRegistry);
    }

    private boolean isRoot(VirtualFile virtualFile) {
        return virtualFile.isRoot();
    }
}
