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
package org.eclipse.che.api.vfs.impl.memory;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Archiver;
import org.eclipse.che.api.vfs.HashSumsCounter;
import org.eclipse.che.api.vfs.LockedFileFinder;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileVisitor;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.singletonMap;

/**
 * In-memory implementation of VirtualFile.
 * <p/>
 * NOTE: This implementation is not thread safe.
 *
 * @author andrew00x
 */
public class MemoryVirtualFile implements VirtualFile {
    private static final Logger  LOG    = LoggerFactory.getLogger(MemoryVirtualFile.class);
    private static final boolean FILE   = false;
    private static final boolean FOLDER = true;

    static MemoryVirtualFile newFile(MemoryVirtualFile parent, String name, InputStream content) throws IOException {
        return new MemoryVirtualFile(parent, name, content == null ? new byte[0] : ByteStreams.toByteArray(content));
    }

    static MemoryVirtualFile newFile(MemoryVirtualFile parent, String name, byte[] content) {
        return new MemoryVirtualFile(parent, name, content == null ? new byte[0] : Arrays.copyOf(content, content.length));
    }

    static MemoryVirtualFile newFolder(MemoryVirtualFile parent, String name) {
        return new MemoryVirtualFile(parent, name);
    }

    //

    private final boolean                        type;
    private final Map<String, String>            properties;
    private final Map<String, MemoryVirtualFile> children;
    private final MemoryVirtualFileSystem        fileSystem;

    private String            name;
    private MemoryVirtualFile parent;
    private byte[]            content;
    private long              lastModificationDate;
    private LockHolder        lock;

    private boolean exists = true;

    // --- File ---
    private MemoryVirtualFile(MemoryVirtualFile parent, String name, byte[] content) {
        this.fileSystem = (MemoryVirtualFileSystem)parent.getFileSystem();
        this.parent = parent;
        this.type = FILE;
        this.name = name;
        this.properties = newHashMap();
        this.content = content;
        children = Collections.emptyMap();
    }

    // --- Folder ---
    private MemoryVirtualFile(MemoryVirtualFile parent, String name) {
        this.fileSystem = (MemoryVirtualFileSystem)parent.getFileSystem();
        this.parent = parent;
        this.type = FOLDER;
        this.name = name;
        this.properties = newHashMap();
        children = newHashMap();
    }

    // --- Root folder ---
    MemoryVirtualFile(VirtualFileSystem virtualFileSystem) {
        this.fileSystem = (MemoryVirtualFileSystem)virtualFileSystem;
        this.type = FOLDER;
        this.name = "";
        this.properties = newHashMap();
        children = newHashMap();
    }

    @Override
    public String getName() {
        checkExistence();
        return name;
    }

    @Override
    public Path getPath() {
        checkExistence();
        MemoryVirtualFile parent = this.parent;
        if (parent == null) {
            return Path.ROOT;
        }
        Path parentPath = parent.getPath();
        return parentPath.newPath(getName());
    }

    @Override
    public boolean isFile() {
        checkExistence();
        return type == FILE;
    }

    @Override
    public boolean isFolder() {
        checkExistence();
        return type == FOLDER;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public boolean isRoot() {
        checkExistence();
        return parent == null;
    }

    @Override
    public long getLastModificationDate() {
        checkExistence();
        return lastModificationDate;
    }

    @Override
    public VirtualFile getParent() {
        checkExistence();
        return parent;
    }

    @Override
    public Map<String, String> getProperties() {
        checkExistence();
        return newHashMap(properties);
    }

    @Override
    public String getProperty(String name) {
        checkExistence();
        return properties.get(name);
    }

    @Override
    public VirtualFile updateProperties(Map<String, String> update, String lockToken) throws ForbiddenException {
        checkExistence();
        if (isFile() && fileIsLockedAndLockTokenIsInvalid(lockToken)) {
            throw new ForbiddenException(String.format("Unable update properties of item '%s'. Item is locked", getPath()));
        }
        for (Map.Entry<String, String> entry : update.entrySet()) {
            if (entry.getValue() == null) {
                properties.remove(entry.getKey());
            } else {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        lastModificationDate = System.currentTimeMillis();
        return this;
    }

    @Override
    public VirtualFile updateProperties(Map<String, String> properties) throws ForbiddenException, ServerException {
        return updateProperties(properties, null);
    }

    @Override
    public VirtualFile setProperty(String name, String value, String lockToken) throws ForbiddenException, ServerException {
        updateProperties(singletonMap(name, value), lockToken);
        return this;
    }

    @Override
    public VirtualFile setProperty(String name, String value) throws ForbiddenException, ServerException {
        return setProperty(name, value, null);
    }

    @Override
    public void accept(VirtualFileVisitor visitor) throws ServerException {
        checkExistence();
        visitor.visit(this);
    }

    @Override
    public List<Pair<String, String>> countMd5Sums() throws ServerException {
        checkExistence();
        if (isFile()) {
            return newArrayList();
        }

        return new HashSumsCounter(this, Hashing.md5()).countHashSums();
    }

    @Override
    public List<VirtualFile> getChildren(VirtualFileFilter filter) {
        checkExistence();
        if (isFolder()) {
            return doGetChildren(this).stream().filter(filter::accept).sorted().collect(Collectors.toList());
        }
        return newArrayList();
    }

    @Override
    public List<VirtualFile> getChildren() {
        checkExistence();
        if (isFolder()) {
            List<VirtualFile> children = doGetChildren(this);
            if (children.size() > 1) {
                Collections.sort(children);
            }
            return children;
        }
        return newArrayList();
    }

    private List<VirtualFile> doGetChildren(VirtualFile folder) {
        return newArrayList(((MemoryVirtualFile)folder).children.values());
    }

    @Override
    public boolean hasChild(Path path) throws ServerException {
        return getChild(path) != null;
    }

    @Override
    public VirtualFile getChild(Path path) throws ServerException {
        checkExistence();

        MemoryVirtualFile child = this;
        Iterator<String> pathSegments = newArrayList(path.elements()).iterator();
        while (pathSegments.hasNext() && child != null) {
            child = child.children.get(pathSegments.next());
        }
        if (pathSegments.hasNext()) {
            return null;
        }
        return child;
    }

    boolean addChild(MemoryVirtualFile child) {
        checkExistence();
        final String childName = child.getName();
        if (children.get(childName) == null) {
            children.put(childName, child);
            return true;
        }
        return false;
    }

    @Override
    public InputStream getContent() throws ForbiddenException {
        return new ByteArrayInputStream(getContentAsBytes());
    }

    @Override
    public byte[] getContentAsBytes() throws ForbiddenException {
        checkExistence();
        if (isFile()) {
            if (content == null) {
                content = new byte[0];
            }
            return Arrays.copyOf(content, content.length);
        }

        throw new ForbiddenException(String.format("We were unable to retrieve the content. Item '%s' is not a file", getPath()));
    }

    @Override
    public String getContentAsString() throws ForbiddenException {
        return new String(getContentAsBytes());
    }

    @Override
    public VirtualFile updateContent(InputStream content, String lockToken) throws ForbiddenException, ServerException {
        byte[] bytes;
        try {
            bytes = ByteStreams.toByteArray(content);
        } catch (IOException e) {
            throw new ServerException(String.format("We were unable to set the content of '%s'. Error: %s", getPath(), e.getMessage()));
        }
        doUpdateContent(bytes, lockToken);
        return this;
    }

    @Override
    public VirtualFile updateContent(byte[] content, String lockToken) throws ForbiddenException, ServerException {
        doUpdateContent(content, lockToken);
        return this;
    }

    @Override
    public VirtualFile updateContent(String content, String lockToken) throws ForbiddenException, ServerException {
        return updateContent(content.getBytes(), lockToken);
    }

    @Override
    public VirtualFile updateContent(byte[] content) throws ForbiddenException, ServerException {
        return updateContent(content, null);
    }

    @Override
    public VirtualFile updateContent(InputStream content) throws ForbiddenException, ServerException {
        return updateContent(content, null);
    }

    @Override
    public VirtualFile updateContent(String content) throws ForbiddenException, ServerException {
        return updateContent(content, null);
    }

    private void doUpdateContent(byte[] content, String lockToken) throws ForbiddenException, ServerException {
        checkExistence();

        if (isFile()) {
            if (fileIsLockedAndLockTokenIsInvalid(lockToken)) {
                throw new ForbiddenException(
                        String.format("We were unable to update the content of file '%s'. The file is locked", getPath()));
            }

            this.content = Arrays.copyOf(content, content.length);
            lastModificationDate = System.currentTimeMillis();

            updateInSearcher();
        } else {
            throw new ForbiddenException(String.format("We were unable to update the content. Item '%s' is not a file", getPath()));
        }
    }

    @Override
    public long getLength() {
        checkExistence();
        if (isFile()) {
            return content.length;
        }
        return 0;
    }

    @Override
    public VirtualFile copyTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException {
        return copyTo(parent, null, false);
    }

    @Override
    public VirtualFile copyTo(VirtualFile parent, String newName, boolean overwrite)
            throws ForbiddenException, ConflictException, ServerException {
        checkExistence();
        ((MemoryVirtualFile)parent).checkExistence();
        if (isRoot()) {
            throw new ServerException("Unable copy root folder");
        }
        if (newName == null || newName.trim().isEmpty()) {
            newName = this.getName();
        }
        if (parent.isFolder()) {
            VirtualFile copy = doCopy((MemoryVirtualFile)parent, newName, overwrite);
            addInSearcher(copy);
            return copy;
        } else {
            throw new ForbiddenException(String.format("Unable create copy of '%s'. Item '%s' specified as parent is not a folder.",
                                                       getPath(), parent.getPath()));
        }
    }

    private VirtualFile doCopy(MemoryVirtualFile parent, String newName, boolean overwrite)
            throws ConflictException, ForbiddenException, ServerException {
        if (overwrite) {
            MemoryVirtualFile existedItem = parent.children.get(newName);
            if (existedItem != null) {
                existedItem.delete();
            }
        }

        MemoryVirtualFile virtualFile;
        if (isFile()) {
            virtualFile = newFile(parent, newName, Arrays.copyOf(content, content.length));
        } else {
            virtualFile = newFolder(parent, newName);
            for (VirtualFile child : getChildren()) {
                child.copyTo(virtualFile);
            }
        }

        virtualFile.properties.putAll(this.properties);

        if (parent.addChild(virtualFile)) {
            return virtualFile;
        }
        throw new ConflictException(String.format("Item '%s' already exists", parent.getPath().newPath(newName)));
    }

    @Override
    public VirtualFile moveTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException {
        return moveTo(parent, null, false, null);
    }

    @Override
    public VirtualFile moveTo(VirtualFile parent, String newName, boolean overwrite, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        checkExistence();
        MemoryVirtualFile memoryParent = (MemoryVirtualFile)parent;
        memoryParent.checkExistence();
        if (isRoot()) {
            throw new ForbiddenException("Unable move root folder");
        }
        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable move item. Item specified as parent is not a folder");
        }
        if (newName == null || newName.trim().isEmpty()) {
            newName = this.getName();
        }
        final boolean isFile = isFile();
        final Path myPath = getPath();
        final Path newParentPath = parent.getPath();

        final boolean folder = isFolder();
        if (folder) {
            if (newParentPath.isChild(myPath)) {
                throw new ForbiddenException(
                        String.format("Unable move item %s to %s. Item may not have itself as parent", myPath, newParentPath));
            }
            final List<VirtualFile> lockedFiles = new LockedFileFinder(this).findLockedFiles();
            if (!lockedFiles.isEmpty()) {
                throw new ForbiddenException(
                        String.format("Unable move item '%s'. Child items '%s' are locked", getName(), lockedFiles));
            }
        } else if (fileIsLockedAndLockTokenIsInvalid(lockToken)) {
            throw new ForbiddenException(String.format("Unable move item %s. Item is locked", myPath));
        }

        if (overwrite) {
            MemoryVirtualFile existedItem = memoryParent.children.get(newName);
            if (existedItem != null) {
                existedItem.delete();
            }
        }

        if (memoryParent.children.containsKey(newName)) {
            throw new ConflictException(String.format("Item '%s' already exists", parent.getPath().newPath(newName)));
        }
        this.parent.children.remove(name);
        memoryParent.children.put(newName, this);
        this.parent = memoryParent;
        this.name = newName;
        lock = null;

        deleteFromSearcher(myPath, isFile);
        addInSearcher(this);
        return this;
    }

    @Override
    public VirtualFile rename(String newName, String lockToken) throws ForbiddenException, ConflictException, ServerException {
        checkExistence();
        checkName(newName);
        boolean isFile = isFile();
        if (isRoot()) {
            throw new ForbiddenException("We were unable to rename a root folder.");
        }
        final Path myPath = getPath();
        final boolean isFolder = isFolder();
        if (isFolder) {
            final List<VirtualFile> lockedFiles = new LockedFileFinder(this).findLockedFiles();
            if (!lockedFiles.isEmpty()) {
                throw new ForbiddenException(
                        String.format("Unable rename item '%s'. Child items '%s' are locked", getName(), lockedFiles));
            }
        } else {
            if (fileIsLockedAndLockTokenIsInvalid(lockToken)) {
                throw new ForbiddenException(String.format("We were unable to rename an item '%s'." +
                                                           " The item is currently locked by the system", getPath()));
            }
        }

        if (parent.children.get(newName) != null) {
            throw new ConflictException(String.format("Item '%s' already exists", newName));
        }
        parent.children.remove(name);
        parent.children.put(newName, this);
        name = newName;
        lock = null;

        lastModificationDate = System.currentTimeMillis();
        deleteFromSearcher(myPath, isFile);
        addInSearcher(this);
        return this;
    }

    @Override
    public VirtualFile rename(String newName) throws ForbiddenException, ConflictException, ServerException {
        return rename(newName, null);
    }

    @Override
    public void delete(String lockToken) throws ForbiddenException, ServerException {
        checkExistence();
        boolean isFile = isFile();
        if (isRoot()) {
            throw new ForbiddenException("Unable delete root folder");
        }
        final Path myPath = getPath();
        final boolean folder = isFolder();
        if (folder) {
            final List<VirtualFile> lockedFiles = new LockedFileFinder(this).findLockedFiles();
            if (!lockedFiles.isEmpty()) {
                throw new ForbiddenException(
                        String.format("Unable delete item '%s'. Child items '%s' are locked", getName(), lockedFiles));
            }
            for (VirtualFile virtualFile : getTreeAsList(this)) {
                ((MemoryVirtualFile)virtualFile).exists = false;
            }
        } else {
            if (fileIsLockedAndLockTokenIsInvalid(lockToken)) {
                throw new ForbiddenException(String.format("Unable delete item '%s'. Item is locked", getPath()));
            }
        }
        parent.children.remove(name);
        exists = false;
        parent = null;
        deleteFromSearcher(myPath, isFile);
    }

    List<VirtualFile> getTreeAsList(VirtualFile folder) throws ServerException {
        List<VirtualFile> list = newArrayList();
        folder.accept(new VirtualFileVisitor() {
            @Override
            public void visit(VirtualFile virtualFile) throws ServerException {
                if (virtualFile.isFolder()) {
                    for (VirtualFile child : virtualFile.getChildren()) {
                        child.accept(this);
                    }
                }
                list.add(virtualFile);
            }
        });
        return list;
    }

    @Override
    public void delete() throws ForbiddenException, ServerException {
        delete(null);
    }

    @Override
    public InputStream zip() throws ForbiddenException, ServerException {
        checkExistence();

        if (isFolder()) {
            return compress(fileSystem.getArchiverFactory().createArchiver(this, "zip"));
        } else {
            throw new ForbiddenException(String.format("Unable export to zip. Item '%s' is not a folder", getPath()));
        }
    }

    @Override
    public void unzip(InputStream zipped, boolean overwrite, int stripNumber)
            throws ForbiddenException, ServerException, ConflictException {
        checkExistence();

        if (isFolder()) {
            extract(fileSystem.getArchiverFactory().createArchiver(this, "zip"), zipped, overwrite, stripNumber);
            addInSearcher(this);
        } else {
            throw new ForbiddenException(String.format("Unable import zip. Item '%s' is not a folder", getPath()));
        }
    }

    @Override
    public InputStream tar() throws ForbiddenException, ServerException {
        checkExistence();

        if (isFolder()) {
            return compress(fileSystem.getArchiverFactory().createArchiver(this, "tar"));
        } else {
            throw new ForbiddenException(String.format("Unable export to tar archive. Item '%s' is not a folder", getPath()));
        }
    }

    @Override
    public void untar(InputStream tarArchive, boolean overwrite, int stripNumber)
            throws ForbiddenException, ConflictException, ServerException {
        checkExistence();

        if (isFolder()) {
            extract(fileSystem.getArchiverFactory().createArchiver(this, "tar"), tarArchive, overwrite, stripNumber);
            addInSearcher(this);
        } else {
            throw new ForbiddenException(String.format("Unable import tar archive. Item '%s' is not a folder", getPath()));
        }
    }

    private InputStream compress(Archiver archiver) throws ForbiddenException, ServerException {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            archiver.compress(byteOut);
            return new ByteArrayInputStream(byteOut.toByteArray());
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    private void extract(Archiver archiver, InputStream compressed, boolean overwrite, int stripNumber)
            throws ConflictException, ServerException, ForbiddenException {
        try {
            archiver.extract(compressed, overwrite, stripNumber);
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @Override
    public String lock(long timeout) throws ForbiddenException, ConflictException {
        checkExistence();
        if (isFile()) {
            if (this.lock != null) {
                throw new ConflictException("File already locked");
            }
            final String lockToken = NameGenerator.generate(null, 32);
            this.lock = new LockHolder(lockToken, timeout);
            lastModificationDate = System.currentTimeMillis();
            return lockToken;
        } else {
            throw new ForbiddenException(String.format("Unable lock '%s'. Locking allowed for files only", getPath()));
        }
    }

    @Override
    public VirtualFile unlock(String lockToken) throws ForbiddenException, ConflictException {
        checkExistence();
        if (isFile()) {
            final LockHolder theLock = lock;
            if (theLock == null) {
                throw new ConflictException("File is not locked");
            } else if (isExpired(theLock)) {
                lock = null;
                throw new ConflictException("File is not locked");
            }
            if (theLock.lockToken.equals(lockToken)) {
                lock = null;
                lastModificationDate = System.currentTimeMillis();
            } else {
                throw new ForbiddenException("Unable remove lock from file. Lock token does not match");
            }
            lastModificationDate = System.currentTimeMillis();
            return this;
        } else {
            throw new ForbiddenException(String.format("Unable unlock '%s'. Locking allowed for files only", getPath()));
        }
    }

    @Override
    public boolean isLocked() {
        checkExistence();
        final LockHolder myLock = lock;
        if (myLock != null) {
            if (isExpired(myLock)) {
                lock = null;
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isExpired(LockHolder lockHolder) {
        return lockHolder.expired < System.currentTimeMillis();
    }

    @Override
    public VirtualFile createFile(String name, InputStream content) throws ForbiddenException, ConflictException, ServerException {
        checkExistence();

        checkName(name);
        if (Path.of(name).length() > 1) {
            throw new ServerException(String.format("Invalid name '%s'", name));
        }

        if (isFolder()) {
            final MemoryVirtualFile newFile;
            try {
                newFile = newFile(this, name, content);
            } catch (IOException e) {
                throw new ServerException(String.format("Unable set content of '%s'. Error: %s", getPath(), e.getMessage()));
            }
            if (!addChild(newFile)) {
                throw new ConflictException(String.format("Item with the name '%s' already exists", name));
            }
            addInSearcher(newFile);
            return newFile;
        } else {
            throw new ForbiddenException("Unable create new file. Item specified as parent is not a folder");
        }
    }

    @Override
    public VirtualFile createFile(String name, byte[] content) throws ForbiddenException, ConflictException, ServerException {
        return createFile(name, new ByteArrayInputStream(content));
    }

    @Override
    public VirtualFile createFile(String name, String content) throws ForbiddenException, ConflictException, ServerException {
        return createFile(name, content.getBytes());
    }

    @Override
    public VirtualFile createFolder(String name) throws ForbiddenException, ConflictException, ServerException {
        checkExistence();
        checkName(name);
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }
        checkName(name);
        if (isFolder()) {
            MemoryVirtualFile newFolder = null;
            MemoryVirtualFile current = this;
            if (name.indexOf('/') > 0) {
                final Path internPath = Path.of(name);
                for (String element : internPath.elements()) {
                    MemoryVirtualFile folder = newFolder(current, element);
                    if (current.addChild(folder)) {
                        newFolder = folder;
                        current = folder;
                    } else {
                        current = current.children.get(element);
                    }
                }
                if (newFolder == null) {
                    throw new ConflictException(String.format("Item with the name '%s' already exists", name));
                }
            } else {
                newFolder = newFolder(this, name);
                if (!addChild(newFolder)) {
                    throw new ConflictException(String.format("Item with the name '%s' already exists", name));
                }
            }
            return newFolder;
        } else {
            throw new ForbiddenException("Unable create new folder. Item specified as parent is not a folder");
        }
    }

    @Override
    public java.io.File toIoFile() {
        return null;
    }

    @Override
    public VirtualFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public int compareTo(VirtualFile o) {
        // To get nice order of items:
        // 1. Regular folders
        // 2. Files
        if (o == null) {
            throw new NullPointerException();
        }
        if (isFolder()) {
            return o.isFolder() ? getName().compareTo(o.getName()) : -1;
        } else if (o.isFolder()) {
            return 1;
        }
        return getName().compareTo(o.getName());
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MemoryVirtualFile)) {
            return false;
        }
        MemoryVirtualFile other = (MemoryVirtualFile)o;
        return Objects.equals(fileSystem, other.fileSystem)
               && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSystem, getPath());
    }

    private void checkExistence() {
        if (!exists) {
            throw new RuntimeException(String.format("Item '%s' already removed", name));
        }
    }

    private void checkName(String name) throws ServerException {
        if (name == null || name.trim().isEmpty()) {
            throw new ServerException("Item's name is not set");
        }
    }

    private void addInSearcher(VirtualFile newFile) {
        SearcherProvider searcherProvider = fileSystem.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(fileSystem).add(newFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void updateInSearcher() {
        SearcherProvider searcherProvider = fileSystem.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(fileSystem).update(this);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void deleteFromSearcher(Path path, boolean isFile) {
        SearcherProvider searcherProvider = fileSystem.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(fileSystem).delete(path.toString(), isFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private boolean fileIsLockedAndLockTokenIsInvalid(String lockToken) {
        if (isLocked()) {
            final LockHolder myLock = lock;
            return myLock != null && !myLock.lockToken.equals(lockToken);
        }
        return false;
    }

    private static class LockHolder {
        final String lockToken;
        final long   expired;

        LockHolder(String lockToken, long timeout) {
            this.lockToken = lockToken;
            this.expired = timeout > 0 ? (System.currentTimeMillis() + timeout) : Long.MAX_VALUE;
        }
    }
}
