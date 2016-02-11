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
package org.eclipse.che.vfs.impl.fs;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.ContentTypeGuesser;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.LazyIterator;
import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.VirtualFileVisitor;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Folder;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.commons.lang.Pair;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of VirtualFile which uses java.io.File.
 *
 * @author andrew00x
 */
public class VirtualFileImpl implements VirtualFile {
    private final java.io.File ioFile;
    private final String       id;
    private final Path         path;
    private final FSMountPoint mountPoint;

    VirtualFileImpl(java.io.File ioFile, Path path, String id, FSMountPoint mountPoint) {
        this.ioFile = ioFile;
        this.path = path;
        this.id = id;
        this.mountPoint = mountPoint;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return path.getName();
    }

    @Override
    public String getPath() {
        return path.toString();
    }

    @Override
    public Path getVirtualFilePath() {
        return path;
    }

    @Override
    public boolean exists() {
        return getIoFile().exists();
    }

    @Override
    public boolean isRoot() {
        return path.isRoot();
    }

    @Override
    public boolean isFile() {
        return getIoFile().isFile();
    }

    @Override
    public boolean isFolder() {
        return getIoFile().isDirectory();
    }

    @Override
    public VirtualFile getParent() {
        return mountPoint.getParent(this);
    }

    @Override
    public LazyIterator<VirtualFile> getChildren(VirtualFileFilter filter) throws ServerException {
        return mountPoint.getChildren(this, filter);
    }

    @Override
    public VirtualFile getChild(String name) throws ForbiddenException, ServerException {
        return mountPoint.getChild(this, name);
    }

    @Override
    public ContentStream getContent() throws ForbiddenException, ServerException {
        return mountPoint.getContent(this);
    }

    @Override
    public VirtualFile updateContent(InputStream content, String lockToken) throws ForbiddenException, ServerException {
        mountPoint.updateContent(this, content, lockToken);
        return this;
    }

    @Override
    public String getMediaType() throws ServerException {
        String mediaType = mountPoint.getPropertyValue(this, "vfs:mimeType");
        if (mediaType == null) {
            // If media type is not set then item may be file or regular folder and cannot be a project.
            mediaType = isFile() ? ContentTypeGuesser.guessContentType(ioFile) : Folder.FOLDER_MIME_TYPE;
        }
        return mediaType;
    }

    //    @Override
    public VirtualFile setMediaType(String mediaType) throws ServerException {
        mountPoint.setProperty(this, "vfs:mimeType", mediaType);
        return this;
    }

    @Override
    public long getCreationDate() {
        // Creation date may not be available from underlying file system.
        return -1;
    }

    @Override
    public long getLastModificationDate() {
        return getIoFile().lastModified();
    }

    @Override
    public long getLength() throws ServerException {
        return getIoFile().length();
    }

    //

    @Override
    public List<Property> getProperties(PropertyFilter filter) throws ServerException {
        if (PropertyFilter.NONE_FILTER == filter) {
            // Do not 'disturb' backend if we already know result is always empty.
            return Collections.emptyList();
        }
        return mountPoint.getProperties(this, filter);
    }

    @Override
    public VirtualFile updateProperties(List<Property> properties, String lockToken) throws ForbiddenException, ServerException {
        mountPoint.updateProperties(this, properties, lockToken);
        return this;
    }

    @Override
    public String getPropertyValue(String name) throws ServerException {
        return mountPoint.getPropertyValue(this, name);
    }

    @Override
    public String[] getPropertyValues(String name) throws ServerException {
        return mountPoint.getPropertyValues(this, name);
    }

    //

    @Override
    public String getVersionId() {
        return mountPoint.getVersionId(this);
    }

    @Override
    public LazyIterator<VirtualFile> getVersions(VirtualFileFilter filter) throws ForbiddenException, ServerException {
        return mountPoint.getVersions(this, filter);
    }

    @Override
    public VirtualFile getVersion(String versionId) throws NotFoundException, ForbiddenException, ServerException {
        return mountPoint.getVersion(this, versionId);
    }

    //
    @Override
    public VirtualFileImpl copyTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException {
        return copyTo(parent, null, false); // default behaviour
    }

    public VirtualFileImpl copyTo(VirtualFile parent, String name, boolean overWrite) throws ForbiddenException, ConflictException, ServerException {
        return mountPoint.copy(this, (VirtualFileImpl) parent, name, overWrite);
    }

    @Override
    public VirtualFileImpl moveTo(VirtualFile parent, String lockToken) throws ForbiddenException, ConflictException, ServerException {
        return moveTo(parent, null, false, lockToken);
    }

    public VirtualFileImpl moveTo(VirtualFile parent, String name, boolean overWrite, String lockToken) throws ForbiddenException, ConflictException, ServerException {
        return mountPoint.move(this, (VirtualFileImpl) parent, name, overWrite, lockToken);
    }

    @Override
    public VirtualFile rename(String newName, String newMediaType, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        return mountPoint.rename(this, newName, newMediaType, lockToken);
    }

    @Override
    public void delete(String lockToken) throws ForbiddenException, ServerException {
        mountPoint.delete(this, lockToken);
    }

    //

    @Override
    public ContentStream zip(VirtualFileFilter filter) throws ForbiddenException, ServerException {
        return mountPoint.zip(this, filter);
    }

    @Override
    public void unzip(InputStream zipped, boolean overwrite, int stripNumber) throws ForbiddenException, ConflictException, ServerException {
        mountPoint.unzip(this, zipped, overwrite, stripNumber);
    }

    //

    @Override
    public String lock(long timeout) throws ForbiddenException, ConflictException, ServerException {
        return mountPoint.lock(this, timeout);
    }

    @Override
    public VirtualFile unlock(String lockToken) throws ForbiddenException, ConflictException, ServerException {
        mountPoint.unlock(this, lockToken);
        return this;
    }

    @Override
    public boolean isLocked() throws ServerException {
        return mountPoint.isLocked(this);
    }

    //

    @Override
    public Map<Principal, Set<String>> getPermissions() throws ServerException {
        return mountPoint.getACL(this).getPermissionMap();
    }

    @Override
    public List<AccessControlEntry> getACL() throws ServerException {
        return mountPoint.getACL(this).getEntries();
    }

    @Override
    public VirtualFile updateACL(List<AccessControlEntry> acl, boolean override, String lockToken)
            throws ForbiddenException, ServerException {
        mountPoint.updateACL(this, acl, override, lockToken);
        return this;
    }

    //

    @Override
    public VirtualFile createFile(String name, InputStream content)
            throws ForbiddenException, ConflictException, ServerException {
        return mountPoint.createFile(this, name, content);
    }

    @Override
    public VirtualFile createFolder(String name) throws ForbiddenException, ConflictException, ServerException {
        return mountPoint.createFolder(this, name);
    }

    //

    @Override
    public FSMountPoint getMountPoint() {
        return mountPoint;
    }

    @Override
    public void accept(VirtualFileVisitor visitor) throws ServerException {
        visitor.visit(this);
    }

    @Override
    public LazyIterator<Pair<String, String>> countMd5Sums() throws ServerException {
        return mountPoint.countMd5Sums(this);
    }

    @Override
    public int compareTo(VirtualFile other) {
        // To get nice order of items:
        // 1. Regular folders
        // 2. Files
        if (other == null) {
            throw new NullPointerException();
        }
        if (isFolder()) {
            return other.isFolder() ? getName().compareTo(other.getName()) : -1;
        } else if (other.isFolder()) {
            return 1;
        }
        return getName().compareTo(other.getName());
    }

    @Override
    public final java.io.File getIoFile() {
        return ioFile;
    }
}
