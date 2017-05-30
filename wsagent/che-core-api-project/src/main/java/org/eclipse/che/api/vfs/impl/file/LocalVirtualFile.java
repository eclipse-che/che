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
package org.eclipse.che.api.vfs.impl.file;

import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileVisitor;
import org.eclipse.che.commons.lang.Pair;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystem.MAX_BUFFER_SIZE;

/**
 * Implementation of VirtualFile which uses java.io.File.
 *
 * @author andrew00x
 */
public class LocalVirtualFile implements VirtualFile {
    private final java.io.File           ioFile;
    private final Path                   path;
    private final LocalVirtualFileSystem fileSystem;

    LocalVirtualFile(java.io.File ioFile, Path path, LocalVirtualFileSystem fileSystem) {
        this.ioFile = ioFile;
        this.path = path;
        this.fileSystem = fileSystem;
    }

    @Override
    public String getName() {
        return path.getName();
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public boolean exists() {
        return toIoFile().exists();
    }

    @Override
    public boolean isRoot() {
        return path.isRoot();
    }

    @Override
    public boolean isFile() {
        return toIoFile().isFile();
    }

    @Override
    public boolean isFolder() {
        return toIoFile().isDirectory();
    }

    @Override
    public VirtualFile getParent() {
        return fileSystem.getParent(this);
    }

    @Override
    public List<VirtualFile> getChildren(VirtualFileFilter filter) throws ServerException {
        return fileSystem.getChildren(this, filter);
    }

    @Override
    public List<VirtualFile> getChildren() throws ServerException {
        return fileSystem.getChildren(this, VirtualFileFilter.ACCEPT_ALL);
    }

    @Override
    public boolean hasChild(Path path) throws ServerException {
        return getChild(path) != null;
    }

    @Override
    public VirtualFile getChild(Path path) throws ServerException {
        return fileSystem.getChild(this, path);
    }

    @Override
    public InputStream getContent() throws ForbiddenException, ServerException {
        return fileSystem.getContent(this);
    }

    @Override
    public byte[] getContentAsBytes() throws ForbiddenException, ServerException {
        if (getLength() > MAX_BUFFER_SIZE) {
            throw new ForbiddenException("File is too big and might not be retrieved as bytes");
        }
        try (InputStream content = getContent()) {
            return ByteStreams.toByteArray(content);
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public String getContentAsString() throws ForbiddenException, ServerException {
        return new String(getContentAsBytes());
    }

    @Override
    public VirtualFile updateContent(InputStream content, String lockToken) throws ForbiddenException, ServerException {
        fileSystem.updateContent(this, content, lockToken);
        return this;
    }

    @Override
    public VirtualFile updateContent(InputStream content) throws ForbiddenException, ServerException {
        return updateContent(content, null);
    }

    @Override
    public VirtualFile updateContent(byte[] content, String lockToken) throws ForbiddenException, ServerException {
        return updateContent(new ByteArrayInputStream(content), lockToken);
    }

    @Override
    public VirtualFile updateContent(byte[] content) throws ForbiddenException, ServerException {
        return updateContent(content, null);
    }

    @Override
    public VirtualFile updateContent(String content, String lockToken) throws ForbiddenException, ServerException {
        return updateContent(content.getBytes(), lockToken);
    }

    @Override
    public VirtualFile updateContent(String content) throws ForbiddenException, ServerException {
        return updateContent(content, null);
    }

    @Override
    public long getLastModificationDate() {
        return toIoFile().lastModified();
    }

    @Override
    public long getLength() throws ServerException {
        if (isFolder()) {
            return 0;
        }
        return toIoFile().length();
    }

    @Override
    public Map<String, String> getProperties() throws ServerException {
        return fileSystem.getProperties(this);
    }

    @Override
    public String getProperty(String name) throws ServerException {
        return fileSystem.getPropertyValue(this, name);
    }

    @Override
    public VirtualFile updateProperties(Map<String, String> properties, String lockToken) throws ForbiddenException, ServerException {
        fileSystem.updateProperties(this, properties, lockToken);
        return this;
    }

    @Override
    public VirtualFile updateProperties(Map<String, String> properties) throws ForbiddenException, ServerException {
        return updateProperties(properties, null);
    }

    @Override
    public VirtualFile setProperty(String name, String value, String lockToken) throws ForbiddenException, ServerException {
        fileSystem.setProperty(this, name, value, lockToken);
        return this;
    }

    @Override
    public VirtualFile setProperty(String name, String value) throws ForbiddenException, ServerException {
        return setProperty(name, value, null);
    }

    @Override
    public LocalVirtualFile copyTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException {
        return copyTo(parent, null, false);
    }

    public LocalVirtualFile copyTo(VirtualFile parent, String name, boolean overwrite)
            throws ForbiddenException, ConflictException, ServerException {
        return fileSystem.copy(this, (LocalVirtualFile)parent, name, overwrite);
    }

    @Override
    public VirtualFile moveTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException {
        return moveTo(parent, null, false, null);
    }

    public LocalVirtualFile moveTo(VirtualFile parent, String name, boolean overwrite, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        return fileSystem.move(this, (LocalVirtualFile)parent, name, overwrite, lockToken);
    }

    @Override
    public VirtualFile rename(String newName, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        return fileSystem.rename(this, newName, lockToken);
    }

    @Override
    public VirtualFile rename(String newName) throws ForbiddenException, ConflictException, ServerException {
        return rename(newName, null);
    }

    @Override
    public void delete(String lockToken) throws ForbiddenException, ServerException {
        fileSystem.delete(this, lockToken);
    }

    @Override
    public void delete() throws ForbiddenException, ServerException {
        delete(null);
    }

    @Override
    public InputStream zip() throws ForbiddenException, ServerException {
        return fileSystem.zip(this);
    }

    @Override
    public void unzip(InputStream zipped, boolean overwrite, int stripNumber)
            throws ForbiddenException, ConflictException, ServerException {
        fileSystem.unzip(this, zipped, overwrite, stripNumber);
    }

    @Override
    public InputStream tar() throws ForbiddenException, ServerException {
        return fileSystem.tar(this);
    }

    @Override
    public void untar(InputStream tarArchive, boolean overwrite, int stripNumber)
            throws ForbiddenException, ConflictException, ServerException {
        fileSystem.untar(this, tarArchive, overwrite, stripNumber);
    }

    @Override
    public String lock(long timeout) throws ForbiddenException, ConflictException, ServerException {
        return fileSystem.lock(this, timeout);
    }

    @Override
    public VirtualFile unlock(String lockToken) throws ForbiddenException, ConflictException, ServerException {
        fileSystem.unlock(this, lockToken);
        return this;
    }

    @Override
    public boolean isLocked() throws ServerException {
        return fileSystem.isLocked(this);
    }

    @Override
    public VirtualFile createFile(String name, InputStream content) throws ForbiddenException, ConflictException, ServerException {
        return fileSystem.createFile(this, name, content);
    }

    @Override
    public VirtualFile createFile(String name, byte[] content) throws ForbiddenException, ConflictException, ServerException {
        return createFile(name, content == null ? null : new ByteArrayInputStream(content));
    }

    @Override
    public VirtualFile createFile(String name, String content) throws ForbiddenException, ConflictException, ServerException {
        return createFile(name, content == null ? null : content.getBytes());
    }

    @Override
    public VirtualFile createFolder(String name) throws ForbiddenException, ConflictException, ServerException {
        return fileSystem.createFolder(this, name);
    }

    @Override
    public LocalVirtualFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public void accept(VirtualFileVisitor visitor) throws ServerException {
        visitor.visit(this);
    }

    @Override
    public List<Pair<String, String>> countMd5Sums() throws ServerException {
        return fileSystem.countMd5Sums(this);
    }

    @Override
    public File toIoFile() {
        return ioFile;
    }

    @Override
    public int compareTo(VirtualFile other) {
        // To get nice order of items:
        // 1. Folders
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o instanceof LocalVirtualFile)) {
            LocalVirtualFile other = (LocalVirtualFile)o;
            return Objects.equals(path, other.path)
                   && Objects.equals(fileSystem, other.fileSystem);
        }
        return false;

    }

    @Override
    public int hashCode() {
        return Objects.hash(path, fileSystem);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
