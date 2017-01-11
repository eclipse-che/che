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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.vfs.AbstractVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.Archiver;
import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.HashSumsCounter;
import org.eclipse.che.api.vfs.LockedFileFinder;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.PathLockFactory;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.api.vfs.util.DeleteOnCloseFileInputStream;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.che.api.vfs.VirtualFileFilters.dotGitFilter;
import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

/**
 * Local filesystem implementation of VirtualFileSystem.
 *
 * @author andrew00x
 */
public class LocalVirtualFileSystem implements VirtualFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(LocalVirtualFileSystem.class);

    static final int MAX_BUFFER_SIZE = 200 * 1024; // 200k

    private static final long WAIT_FOR_FILE_LOCK_TIMEOUT = 60000; // 60 seconds
    private static final int  FILE_LOCK_MAX_THREADS      = 1024;

    private static final String   VFS_SERVICE_DIR        = ".vfs";
    private static final String   FILE_LOCKS_DIR         = VFS_SERVICE_DIR + File.separatorChar + "locks";
    private static final String   LOCK_FILE_SUFFIX       = "_lock";
    private static final FileLock NO_LOCK                = new FileLock("no_lock", 0);
    private static final String   FILE_PROPERTIES_DIR    = VFS_SERVICE_DIR + File.separatorChar + "props";
    private static final String   PROPERTIES_FILE_SUFFIX = "_props";

    private static final FilenameFilter DOT_VFS_DIR_FILTER = (dir, name) -> !(VFS_SERVICE_DIR.equals(name));

    private static final FilenameFilter VFS_LOCK_FILTER =
            (dir, name) -> !(dir.getAbsolutePath().endsWith(FILE_LOCKS_DIR) || name.endsWith(LOCK_FILE_SUFFIX));

    private class LockTokenCacheLoader extends CacheLoader<Path, FileLock> {
        @Override
        public FileLock load(Path path) throws Exception {
            final File lockIoFile = getFileLockIoFile(path);
            if (lockIoFile.exists()) {
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(lockIoFile)))) {
                    return locksSerializer.read(dis);
                }
            }
            return NO_LOCK;
        }
    }

    private class FilePropertiesCacheLoader extends CacheLoader<Path, Map<String, String>> {
        @Override
        public Map<String, String> load(Path path) throws Exception {
            final File metadataIoFile = getMetadataIoFile(path);
            if (metadataIoFile.exists()) {
                try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(metadataIoFile)))) {
                    return ImmutableMap.copyOf(metadataSerializer.read(dis));
                }
            }
            return emptyMap();
        }
    }

    private final File                                            ioRoot;
    private final ArchiverFactory                                 archiverFactory;
    private final SearcherProvider                                searcherProvider;
    private final AbstractVirtualFileSystemProvider.CloseCallback closeCallback;

    /* NOTE -- This does not related to virtual file system locking in any kind. -- */
    private final PathLockFactory pathLockFactory;

    private final LocalVirtualFile root;

    private final FileLockSerializer           locksSerializer;
    private final LoadingCache<Path, FileLock> lockTokensCache;

    private final FileMetadataSerializer                  metadataSerializer;
    private final LoadingCache<Path, Map<String, String>> metadataCache;

    @SuppressWarnings("unchecked")
    public LocalVirtualFileSystem(File ioRoot,
                                  ArchiverFactory archiverFactory,
                                  SearcherProvider searcherProvider,
                                  AbstractVirtualFileSystemProvider.CloseCallback closeCallback) {
        this.ioRoot = ioRoot;
        this.archiverFactory = archiverFactory;
        this.searcherProvider = searcherProvider;
        this.closeCallback = closeCallback;

        root = new LocalVirtualFile(ioRoot, Path.ROOT, this);
        pathLockFactory = new PathLockFactory(FILE_LOCK_MAX_THREADS);

        locksSerializer = new FileLockSerializer();
        lockTokensCache = CacheBuilder.newBuilder()
                                      .concurrencyLevel(8)
                                      .maximumSize(256)
                                      .expireAfterAccess(10, MINUTES)
                                      .build(new LockTokenCacheLoader());

        metadataSerializer = new FileMetadataSerializer();
        metadataCache = CacheBuilder.newBuilder()
                                    .concurrencyLevel(8)
                                    .maximumSize(256)
                                    .expireAfterAccess(10, MINUTES)
                                    .build(new FilePropertiesCacheLoader());
    }

    @Override
    public LocalVirtualFile getRoot() {
        return root;
    }

    @Override
    public SearcherProvider getSearcherProvider() {
        return searcherProvider;
    }

    @Override
    public void close() throws ServerException {
        cleanUpCaches();
        if (searcherProvider != null) {
            Searcher searcher = searcherProvider.getSearcher(this, false);
            if (searcher != null) {
                searcher.close();
            }
        }
        if (closeCallback != null) {
            closeCallback.onClose();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o instanceof LocalVirtualFileSystem)) {
            LocalVirtualFileSystem other = (LocalVirtualFileSystem)o;
            return Objects.equals(ioRoot, other.ioRoot);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ioRoot);
    }

    private void cleanUpCaches() {
        lockTokensCache.invalidateAll();
        metadataCache.invalidateAll();
    }

    /** Used in tests. Need this to check state of PathLockFactory. All locks MUST be released at the end of request lifecycle. */
    PathLockFactory getPathLockFactory() {
        return pathLockFactory;
    }


    LocalVirtualFile getParent(LocalVirtualFile virtualFile) {
        if (virtualFile.isRoot()) {
            return null;
        }
        final Path parentPath = virtualFile.getPath().getParent();
        return new LocalVirtualFile(new File(ioRoot, toIoPath(parentPath)), parentPath, this);
    }


    LocalVirtualFile getChild(LocalVirtualFile parent, Path path) {
        if (isVfsServicePath(path)) {
            return null;
        }
        if (parent.isFolder()) {
            final Path childPath = parent.getPath().newPath(path);
            final LocalVirtualFile child = new LocalVirtualFile(new File(ioRoot, toIoPath(childPath)), childPath, this);
            if (child.exists()) {
                return child;
            }
        }
        return null;
    }

    private boolean isVfsServicePath(Path path) {
        return newArrayList(path.elements()).contains(".vfs");
    }

    synchronized List<VirtualFile> getChildren(LocalVirtualFile parent, VirtualFileFilter filter) throws ServerException {
        if (parent.isFolder()) {
            final List<VirtualFile> children = doGetChildren(parent, DOT_VFS_DIR_FILTER, filter);
            Collections.sort(children);
            return children;
        }
        return emptyList();
    }


    private List<VirtualFile> doGetChildren(LocalVirtualFile parent, FilenameFilter ioFileFilter, VirtualFileFilter vfsFilter)
            throws ServerException {
        if (ioFileFilter == null) {
            ioFileFilter = IoUtil.ANY_FILTER;
        }

        final String[] names = parent.toIoFile().list(ioFileFilter);
        if (names == null) {
            throw new ServerException(String.format("Unable get children of '%s'", parent.getPath()));
        }

        if (vfsFilter == null) {
            vfsFilter = VirtualFileFilter.ACCEPT_ALL;
        }

        final List<VirtualFile> children = newArrayListWithCapacity(names.length);
        for (String name : names) {
            final Path childPath = parent.getPath().newPath(name);
            final LocalVirtualFile child = new LocalVirtualFile(new File(ioRoot, toIoPath(childPath)), childPath, this);
            if (vfsFilter.accept(child)) {
                children.add(child);
            }
        }

        return children;
    }


    LocalVirtualFile createFile(LocalVirtualFile parent, String name, InputStream content)
            throws ForbiddenException, ConflictException, ServerException {
        checkName(name);

        if (Path.of(name).length() > 1) {
            throw new ServerException(String.format("Invalid name '%s'", name));
        }

        if (parent.isFolder()) {
            final Path newPath = parent.getPath().newPath(name);
            final File newIoFile = new File(ioRoot, toIoPath(newPath));

            try {
                if (!newIoFile.createNewFile()) {
                    throw new ConflictException(String.format("Item '%s' already exists", newPath));
                }
            } catch (IOException e) {
                String errorMessage = String.format("Unable create new file '%s'", newPath);
                LOG.error(errorMessage + "\n" + e.getMessage(), e);
                throw new ServerException(errorMessage);
            }

            final LocalVirtualFile newVirtualFile = new LocalVirtualFile(newIoFile, newPath, this);

            if (content != null) {
                doUpdateContent(newVirtualFile, content);
            }

            addInSearcher(newVirtualFile);

            return newVirtualFile;
        } else {
            throw new ForbiddenException("Unable create new file. Item specified as parent is not a folder");
        }
    }


    LocalVirtualFile createFolder(LocalVirtualFile parent, String name)
            throws ForbiddenException, ConflictException, ServerException {
        checkName(name);

        if (parent.isFolder()) {
            final Path newPath = parent.getPath().newPath(name);
            final File newIoFile = new File(ioRoot, toIoPath(newPath));
            if (!newIoFile.mkdirs()) {
                if (newIoFile.exists()) {
                    throw new ConflictException(String.format("Item '%s' already exists", newPath));
                }
            }

            return new LocalVirtualFile(newIoFile, newPath, this);
        } else {
            throw new ForbiddenException("Unable create folder. Item specified as parent is not a folder");
        }
    }


    LocalVirtualFile copy(LocalVirtualFile source, LocalVirtualFile parent, String name, boolean overwrite)
            throws ForbiddenException, ConflictException, ServerException {
        if (source.getPath().equals(parent.getPath())) {
            throw new ForbiddenException("Item cannot be copied to itself");
        }
        if (parent.isFolder()) {
            final String newName = isNullOrEmpty(name) ? source.getName() : name;
            LocalVirtualFile destination = (LocalVirtualFile)parent.getChild(Path.of(newName));
            if (destination != null) {
                if (overwrite) {
                    delete(destination, null);
                } else {
                    throw new ConflictException(String.format("Item '%s' already exists", destination.getPath()));
                }
            } else {
                final Path newPath = parent.getPath().newPath(newName);
                final File newIoFile = new File(ioRoot, toIoPath(newPath));
                destination = new LocalVirtualFile(newIoFile, newPath, this);
            }

            doCopy(source, destination);

            addInSearcher(destination);

            return destination;
        } else {
            throw new ForbiddenException("Unable copy item. Item specified as parent is not a folder");
        }
    }


    private void doCopy(LocalVirtualFile from, LocalVirtualFile to) throws ServerException {
        try {
            // First copy metadata (properties) for source. If we do in this way and fail cause to any i/o or other error client
            // will see error and may try to copy again. But if we successfully copy tree (or single file) and then fail to copy
            // metadata client may not try to copy again because copy destination already exists.

            final File fromMetadataFile = getMetadataIoFile(from.getPath());
            final File toMetadataFile = getMetadataIoFile(to.getPath());
            if (fromMetadataFile.exists()) {
                IoUtil.copy(fromMetadataFile, toMetadataFile, null);
            }

            IoUtil.copy(from.toIoFile(), to.toIoFile(), VFS_LOCK_FILTER);
        } catch (IOException e) {
            String errorMessage = String.format("Unable copy '%s' to '%s'", from, to);
            LOG.error(errorMessage + "\n" + e.getMessage(), e);
            throw new ServerException(errorMessage);
        }
    }


    LocalVirtualFile rename(LocalVirtualFile virtualFile, String newName, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        checkName(newName);

        if (virtualFile.isRoot()) {
            throw new ForbiddenException("Unable rename root folder");
        }

        if (virtualFile.isFile()) {
            if (fileIsLockedAndLockTokenIsInvalid(virtualFile, lockToken)) {
                throw new ForbiddenException(String.format("Unable rename file '%s'. File is locked", virtualFile.getPath()));
            }
        } else {
            final List<VirtualFile> lockedFiles = new LockedFileFinder(virtualFile).findLockedFiles();
            if (!lockedFiles.isEmpty()) {
                throw new ForbiddenException(
                        String.format("Unable rename folder '%s'. Child items '%s' are locked", virtualFile.getPath(), lockedFiles));
            }
        }

        if (newName.equals(virtualFile.getName())) {
            return virtualFile;
        } else {
            final Path newPath = virtualFile.getPath().getParent().newPath(newName);
            final LocalVirtualFile
                    newVirtualFile = new LocalVirtualFile(new File(ioRoot, toIoPath(newPath)), newPath, this);
            if (newVirtualFile.exists()) {
                throw new ConflictException(String.format("Item '%s' already exists", newVirtualFile.getName()));
            }

            doCopy(virtualFile, newVirtualFile);
            addInSearcher(newVirtualFile);

            final Path path = virtualFile.getPath();
            final boolean isFile = virtualFile.isFile();
            doDelete(virtualFile, lockToken);
            deleteInSearcher(path, isFile);

            return newVirtualFile;
        }
    }


    LocalVirtualFile move(LocalVirtualFile virtualFile, LocalVirtualFile parent, String name,
                          boolean overwrite, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        if (virtualFile.isRoot()) {
            throw new ForbiddenException("Unable move root folder");
        }
        if (virtualFile.getPath().equals(parent.getPath())) {
            throw new ForbiddenException("Item cannot be moved to itself");
        }
        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable move. Item specified as parent is not a folder");
        }
        final Path sourcePath = virtualFile.getPath();
        final Path parentPath = parent.getPath();
        if (virtualFile.isFolder() && parent.getPath().isChild(virtualFile.getPath())) {
            throw new ForbiddenException(String.format("Unable move item '%s' to '%s'. Item may not have itself as parent",
                                                       sourcePath, parentPath));
        }

        if (virtualFile.isFile()) {
            if (fileIsLockedAndLockTokenIsInvalid(virtualFile, lockToken)) {
                throw new ForbiddenException(String.format("Unable move file '%s'. File is locked", sourcePath));
            }
        } else {
            final List<VirtualFile> lockedFiles = new LockedFileFinder(virtualFile).findLockedFiles();
            if (!lockedFiles.isEmpty()) {
                throw new ForbiddenException(
                        String.format("Unable move folder '%s'. Child items '%s' are locked", virtualFile, lockedFiles));
            }
        }

        String newName = isNullOrEmpty(name) ? virtualFile.getName() : name;
        final Path newPath = parent.getPath().newPath(newName);
        LocalVirtualFile newVirtualFile = new LocalVirtualFile(new File(ioRoot, toIoPath(newPath)), newPath, this);

        if (newVirtualFile.exists()) {
            if (overwrite) {
                delete(newVirtualFile, null);
            } else {
                throw new ConflictException(String.format("Item '%s' already exists", newPath));
            }
        }

        doCopy(virtualFile, newVirtualFile);
        addInSearcher(newVirtualFile);

        final Path path = virtualFile.getPath();
        final boolean isFile = virtualFile.isFile();
        doDelete(virtualFile, lockToken);
        deleteInSearcher(path, isFile);

        return newVirtualFile;
    }


    InputStream getContent(LocalVirtualFile virtualFile) throws ForbiddenException, ServerException {
        if (virtualFile.isFile()) {
            final PathLockFactory.PathLock lock = pathLockFactory.getLock(virtualFile.getPath(), false).acquire(WAIT_FOR_FILE_LOCK_TIMEOUT);
            File spoolFile = null;
            try {
                final File ioFile = virtualFile.toIoFile();
                final long fileLength = ioFile.length();
                if (fileLength <= MAX_BUFFER_SIZE) {
                    return new ByteArrayInputStream(Files.toByteArray(ioFile));
                }
                // Copy this file to be able release the file lock before leave this method.
                spoolFile = File.createTempFile("spool_file", null);
                Files.copy(ioFile, spoolFile);
                return new DeleteOnCloseFileInputStream(spoolFile);
            } catch (IOException e) {
                if (spoolFile != null) {
                    FileCleaner.addFile(spoolFile);
                }
                String errorMessage = String.format("Unable get content of '%s'", virtualFile.getPath());
                LOG.error(errorMessage + "\n" + e.getMessage(), e);
                throw new ServerException(errorMessage);
            } finally {
                lock.release();
            }
        } else {
            throw new ForbiddenException(String.format("Unable get content. Item '%s' is not a file", virtualFile.getPath()));
        }
    }


    void updateContent(LocalVirtualFile virtualFile, InputStream content, String lockToken)
            throws ForbiddenException, ServerException {
        if (virtualFile.isFile()) {
            if (fileIsLockedAndLockTokenIsInvalid(virtualFile, lockToken)) {
                throw new ForbiddenException(String.format("Unable update content of file '%s'. File is locked", virtualFile.getPath()));
            }
            final PathLockFactory.PathLock lock = pathLockFactory.getLock(virtualFile.getPath(), true).acquire(WAIT_FOR_FILE_LOCK_TIMEOUT);
            try {
                doUpdateContent(virtualFile, content);
            } finally {
                lock.release();
            }
            updateInSearcher(virtualFile);
        } else {
            throw new ForbiddenException(String.format("Unable update content. Item '%s' is not file", virtualFile.getPath()));
        }
    }


    private void doUpdateContent(LocalVirtualFile virtualFile, InputStream content) throws ServerException {
        try (FileOutputStream fileOut = new FileOutputStream(virtualFile.toIoFile())) {
            ByteStreams.copy(content, fileOut);
        } catch (IOException e) {
            String errorMessage = String.format("Unable set content of '%s'", virtualFile.getPath());
            LOG.error(errorMessage + "\n" + e.getMessage(), e);
            throw new ServerException(errorMessage);
        }
    }

    void delete(LocalVirtualFile virtualFile, String lockToken) throws ForbiddenException, ServerException {
        if (virtualFile.isRoot()) {
            throw new ForbiddenException("Unable delete root folder");
        }

        final Path path = virtualFile.getPath();
        final boolean isFile = virtualFile.isFile();

        doDelete(virtualFile, lockToken);

        deleteInSearcher(path, isFile);
    }

    private void doDelete(LocalVirtualFile virtualFile, String lockToken) throws ForbiddenException, ServerException {
        if (virtualFile.isFolder()) {
            final List<VirtualFile> lockedFiles = new LockedFileFinder(virtualFile).findLockedFiles();
            if (!lockedFiles.isEmpty()) {
                throw new ForbiddenException(
                        String.format("Unable delete folder '%s'. Child items '%s' are locked", virtualFile.getPath(), lockedFiles));
            }
        } else if (fileIsLockedAndLockTokenIsInvalid(virtualFile, lockToken)) {
            throw new ForbiddenException(String.format("Unable delete file '%s'. File is locked", virtualFile.getPath()));
        }

        cleanUpCaches();

        final File fileLockIoFile = getFileLockIoFile(virtualFile.getPath());
        if (fileLockIoFile.delete()) {
            if (fileLockIoFile.exists()) {
                LOG.error("Unable delete lock file {}", fileLockIoFile);
                throw new ServerException(String.format("Unable delete item '%s'", virtualFile.getPath()));
            }
        }

        final File metadataIoFile = getMetadataIoFile(virtualFile.getPath());
        if (metadataIoFile.delete()) {
            if (metadataIoFile.exists()) {
                LOG.error("Unable delete metadata file {}", metadataIoFile);
                throw new ServerException(String.format("Unable delete item '%s'", virtualFile.getPath()));
            }
        }

        if (!deleteRecursive(virtualFile.toIoFile())) {
            LOG.error("Unable delete file {}", virtualFile.toIoFile());
            throw new ServerException(String.format("Unable delete item '%s'", virtualFile.getPath()));
        }
    }


    InputStream zip(LocalVirtualFile folder) throws ForbiddenException, ServerException {
        if(archiverFactory == null)
            throw new ServerException("VFS: Could not create zip archiver. Archiver Factory is not properly configured (is null)");

        if (folder.isFolder()) {
            return compress(archiverFactory.createArchiver(folder, "zip"));
        } else {
            throw new ForbiddenException(String.format("Unable export to zip. Item '%s' is not a folder", folder.getPath()));
        }
    }


    void unzip(LocalVirtualFile parent, InputStream zipped, boolean overwrite, int stripNumber)
            throws ForbiddenException, ConflictException, ServerException {
        if(archiverFactory == null)
            throw new ServerException("VFS: Could not create zip archiver. Archiver Factory is not properly configured (is null)");

        if (parent.isFolder()) {
            extract(archiverFactory.createArchiver(parent, "zip"), zipped, overwrite, stripNumber);
            addInSearcher(parent);
        } else {
            throw new ForbiddenException(String.format("Unable import zip content. Item '%s' is not a folder", parent.getPath()));
        }
    }


    InputStream tar(LocalVirtualFile folder) throws ForbiddenException, ServerException {
        if(archiverFactory == null)
            throw new ServerException("VFS: Could not create tar archiver. Archiver Factory is not properly configured (is null)");

        if (folder.isFolder()) {
            return compress(archiverFactory.createArchiver(folder, "tar"));
        } else {
            throw new ForbiddenException(String.format("Unable export to tar archive. Item '%s' is not a folder", folder.getPath()));
        }
    }

    void untar(LocalVirtualFile parent, InputStream tarArchive, boolean overwrite, int stripNumber)
            throws ForbiddenException, ConflictException, ServerException {
        if(archiverFactory == null)
            throw new ServerException("VFS: Could not create tar archiver. Archiver Factory is not properly configured (is null)");

        if (parent.isFolder()) {
            extract(archiverFactory.createArchiver(parent, "tar"), tarArchive, overwrite, stripNumber);
            addInSearcher(parent);
        } else {
            throw new ForbiddenException(String.format("Unable import tar archive. Item '%s' is not a folder", parent.getPath()));
        }
    }

    private InputStream compress(Archiver archiver) throws ForbiddenException, ServerException {
        File archive = null;
        try {
            archive = File.createTempFile("export", ".arc");
            try (FileOutputStream fileOut = new FileOutputStream(archive)) {
                archiver.compress(fileOut, dotGitFilter());
            }
            return new DeleteOnCloseFileInputStream(archive);
        } catch (IOException e) {
            if (archive != null) {
                FileCleaner.addFile(archive);
            }
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

    String lock(LocalVirtualFile virtualFile, long timeout) throws ForbiddenException, ConflictException, ServerException {
        if (virtualFile.isFile()) {
            final PathLockFactory.PathLock pathLock = pathLockFactory.getLock(virtualFile.getPath(), true).acquire(WAIT_FOR_FILE_LOCK_TIMEOUT);
            try {
                return doLock(virtualFile, timeout);
            } finally {
                pathLock.release();
            }
        } else {
            throw new ForbiddenException(String.format("Unable lock '%s'. Locking allowed for files only", virtualFile.getPath()));
        }
    }

    private String doLock(LocalVirtualFile virtualFile, long timeout) throws ConflictException, ServerException {
        try {
            if (NO_LOCK == lockTokensCache.get(virtualFile.getPath())) {
                final FileLock lock = createLock(timeout);
                final File fileLockIoFile = getFileLockIoFile(virtualFile.getPath());
                fileLockIoFile.getParentFile().mkdirs();
                try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileLockIoFile)))) {
                    locksSerializer.write(dos, lock);
                }
                lockTokensCache.put(virtualFile.getPath(), lock);
                return lock.getLockToken();
            }
            throw new ConflictException(String.format("Unable lock file '%s'. File already locked", virtualFile.getPath()));
        } catch (IOException | ExecutionException e) {
            String errorMessage = String.format("Unable lock file '%s'", virtualFile.getPath());
            if (e instanceof ExecutionException) {
                LOG.error(errorMessage + "\n" + e.getCause().getMessage(), e.getCause());
            } else {
                LOG.error(errorMessage + "\n" + e.getMessage(), e);
            }
            throw new ServerException(errorMessage);
        }
    }

    private FileLock createLock(long timeout) {
        final long expired = timeout > 0 ? (System.currentTimeMillis() + timeout) : Long.MAX_VALUE;
        return new FileLock(generateLockToken(), expired);
    }

    private String generateLockToken() {
        return NameGenerator.generate(null, 16);
    }

    void unlock(LocalVirtualFile virtualFile, String lockToken) throws ForbiddenException, ConflictException, ServerException {
        if (lockToken == null) {
            throw new ForbiddenException("Null lock token");
        }
        if (!virtualFile.isFile()) {
            throw new ConflictException(String.format("Item '%s' is not locked", virtualFile.getPath()));
        }
        final FileLock fileLock = getFileLock(virtualFile);
        if (NO_LOCK == fileLock) {
            throw new ConflictException(String.format("File '%s' is not locked", virtualFile.getPath()));
        }
        if (!fileLock.getLockToken().equals(lockToken)) {
            throw new ForbiddenException(String.format("Unable unlock file '%s'. Lock token does not match", virtualFile.getPath()));
        }

        final PathLockFactory.PathLock lockFilePathLock = pathLockFactory.getLock(virtualFile.getPath(), true).acquire(
                WAIT_FOR_FILE_LOCK_TIMEOUT);
        try {
            doUnlock(virtualFile);
        } finally {
            lockFilePathLock.release();
        }
    }

    private void doUnlock(LocalVirtualFile virtualFile) throws ForbiddenException, ServerException {
        try {
            final File fileLockIoFile = getFileLockIoFile(virtualFile.getPath());
            if (!fileLockIoFile.delete()) {
                if (fileLockIoFile.exists()) {
                    throw new IOException(String.format("Unable delete lock file %s", fileLockIoFile));
                }
            }
            lockTokensCache.put(virtualFile.getPath(), NO_LOCK);
        } catch (IOException e) {
            String errorMessage = String.format("Unable unlock file '%s'", virtualFile.getPath());
            LOG.error(errorMessage + "\n" + e.getMessage(), e);
            throw new ServerException(errorMessage);
        }
    }


    boolean isLocked(LocalVirtualFile virtualFile) throws ServerException {
        return virtualFile.isFile() && NO_LOCK != getFileLock(virtualFile);
    }


    private boolean fileIsLockedAndLockTokenIsInvalid(LocalVirtualFile virtualFile, String checkLockToken)
            throws ServerException {
        final FileLock lock = getFileLock(virtualFile);
        return !(NO_LOCK == lock || lock.getLockToken().equals(checkLockToken));
    }

    private FileLock getFileLock(LocalVirtualFile virtualFile) throws ServerException {
        final PathLockFactory.PathLock lockFilePathLock =
                pathLockFactory.getLock(virtualFile.getPath(), true).acquire(WAIT_FOR_FILE_LOCK_TIMEOUT);
        try {
            final FileLock lock;
            try {
                lock = lockTokensCache.get(virtualFile.getPath());
            } catch (ExecutionException e) {
                String errorMessage = String.format("Unable get lock of file '%s'", virtualFile.getPath());
                LOG.error(errorMessage + "\n" + e.getCause().getMessage(), e.getCause());
                throw new ServerException(errorMessage);
            }
            if (NO_LOCK == lock) {
                return lock;
            }
            if (lock.getExpired() < System.currentTimeMillis()) {
                final File fileLockIoFile = getFileLockIoFile(virtualFile.getPath());
                if (!fileLockIoFile.delete()) {
                    if (fileLockIoFile.exists()) {
                        FileCleaner.addFile(fileLockIoFile);
                        LOG.warn("Unable delete lock file %s", fileLockIoFile);
                    }
                }
                lockTokensCache.put(virtualFile.getPath(), NO_LOCK);
                return NO_LOCK;
            }
            return lock;
        } finally {
            lockFilePathLock.release();
        }
    }

    private File getFileLockIoFile(Path virtualFilePath) {
        final String fileLockFileName = virtualFilePath.getName() + LOCK_FILE_SUFFIX;
        final Path metadataFilePath;
        if (virtualFilePath.isRoot()) {
            metadataFilePath = virtualFilePath.newPath(FILE_LOCKS_DIR, fileLockFileName);
        } else {
            metadataFilePath = virtualFilePath.getParent().newPath(FILE_LOCKS_DIR, fileLockFileName);
        }
        return new File(ioRoot, toIoPath(metadataFilePath));
    }


    Map<String, String> getProperties(LocalVirtualFile virtualFile) throws ServerException {
        final PathLockFactory.PathLock metadataFilePathLock =
                pathLockFactory.getLock(virtualFile.getPath(), false).acquire(WAIT_FOR_FILE_LOCK_TIMEOUT);
        try {
            return newLinkedHashMap(metadataCache.get(virtualFile.getPath()));
        } catch (ExecutionException e) {
            String errorMessage = String.format("Unable read properties of file '%s'", virtualFile.getPath());
            LOG.error(errorMessage + "\n" + e.getCause().getMessage(), e.getCause());
            throw new ServerException(errorMessage);
        } finally {
            metadataFilePathLock.release();
        }
    }


    String getPropertyValue(LocalVirtualFile virtualFile, String name) throws ServerException {
        return getProperties(virtualFile).get(name);
    }


    void updateProperties(LocalVirtualFile virtualFile, Map<String, String> updates, String lockToken)
            throws ForbiddenException, ServerException {
        if (virtualFile.isFile() && fileIsLockedAndLockTokenIsInvalid(virtualFile, lockToken)) {
            throw new ForbiddenException(
                    String.format("Unable update properties of item '%s'. Item is locked", virtualFile.getPath()));
        }
        final PathLockFactory.PathLock pathLock = pathLockFactory.getLock(virtualFile.getPath(), true).acquire(WAIT_FOR_FILE_LOCK_TIMEOUT);
        try {
            doUpdateProperties(virtualFile, updates);
        } finally {
            pathLock.release();
        }
    }

    private void doUpdateProperties(LocalVirtualFile virtualFile, Map<String, String> updates) throws ServerException {
        try {
            final Map<String, String> properties = getProperties(virtualFile);
            for (Map.Entry<String, String> entry : updates.entrySet()) {
                if (entry.getValue() == null) {
                    properties.remove(entry.getKey());
                } else {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }

            final File metadataIoFile = getMetadataIoFile(virtualFile.getPath());
            if (properties.isEmpty()) {
                if (!metadataIoFile.delete()) {
                    if (metadataIoFile.exists()) {
                        LOG.error("Unable delete metadata file {}", metadataIoFile);
                        throw new IOException(String.format("Unable update properties of item '%s'", virtualFile.getPath()));
                    }
                }
            } else {
                metadataIoFile.getParentFile().mkdirs();
                try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metadataIoFile)))) {
                    metadataSerializer.write(dos, properties);
                }
            }

            metadataCache.put(virtualFile.getPath(), properties);

            if (!virtualFile.toIoFile().setLastModified(System.currentTimeMillis())) {
                LOG.warn("Unable to set timestamp to '{}'", virtualFile.toIoFile());
            }
        } catch (IOException e) {
            String errorMessage = String.format("Unable lock file '%s'", virtualFile.getPath());
            LOG.error(errorMessage + "\n" + e.getMessage(), e);
            throw new ServerException(errorMessage);
        }
    }


    void setProperty(LocalVirtualFile virtualFile, String name, String value, String lockToken)
            throws ForbiddenException, ServerException {
        updateProperties(virtualFile, singletonMap(name, value), lockToken);
    }


    private File getMetadataIoFile(Path virtualFilePath) {
        final String metadataFileName = virtualFilePath.getName() + PROPERTIES_FILE_SUFFIX;
        final Path metadataFilePath;
        if (virtualFilePath.isRoot()) {
            metadataFilePath = virtualFilePath.newPath(FILE_PROPERTIES_DIR, metadataFileName);
        } else {
            metadataFilePath = virtualFilePath.getParent().newPath(FILE_PROPERTIES_DIR, metadataFileName);
        }
        return new File(ioRoot, toIoPath(metadataFilePath));
    }


    List<Pair<String, String>> countMd5Sums(LocalVirtualFile virtualFile) throws ServerException {
        if (virtualFile.isFile()) {
            return emptyList();
        }
        return new HashSumsCounter(virtualFile, Hashing.md5()).countHashSums();
    }


    private String toIoPath(Path vfsPath) {
        if (vfsPath.isRoot()) {
            return "";
        }
        if ('/' == File.separatorChar) {
            return vfsPath.toString();
        }
        return vfsPath.join(File.separatorChar);
    }


    private void checkName(String name) throws ServerException {
        if (name == null || name.trim().isEmpty()) {
            throw new ServerException("Item's name is not set");
        }
    }


    private void addInSearcher(LocalVirtualFile newVirtualFile) {
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(this).add(newVirtualFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void updateInSearcher(LocalVirtualFile virtualFile) {
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(this).update(virtualFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void deleteInSearcher(Path path, boolean isFile) {
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(this).delete(path.toString(), isFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
