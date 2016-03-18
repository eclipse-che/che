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

import com.google.common.annotations.Beta;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.LazyIterator;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.PathLockFactory;
import org.eclipse.che.api.vfs.server.SystemPathsFilter;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.VirtualFileVisitor;
import org.eclipse.che.api.vfs.server.observation.CreateEvent;
import org.eclipse.che.api.vfs.server.observation.DeleteEvent;
import org.eclipse.che.api.vfs.server.observation.MoveEvent;
import org.eclipse.che.api.vfs.server.observation.RenameEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateACLEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateContentEvent;
import org.eclipse.che.api.vfs.server.observation.UpdatePropertiesEvent;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.server.util.DeleteOnCloseFileInputStream;
import org.eclipse.che.api.vfs.server.util.NotClosableInputStream;
import org.eclipse.che.api.vfs.server.util.ZipContent;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.cache.Cache;
import org.eclipse.che.commons.lang.cache.LoadingValueSLRUCache;
import org.eclipse.che.commons.lang.cache.SynchronizedCache;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Collections.singletonList;
import static org.eclipse.che.commons.lang.IoUtil.GIT_FILTER;
import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;
import static org.eclipse.che.commons.lang.IoUtil.nioCopy;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Local filesystem implementation of MountPoint.
 *
 * @deprecated  VFS is deprecated in 4.0
 * @author andrew00x
 */
@Deprecated
public class FSMountPoint implements MountPoint {
    private static final Logger LOG = LoggerFactory.getLogger(FSMountPoint.class);

    /*
     * Configuration parameters for caches.
     * Caches are split to the few partitions to reduce lock contention.
     * Use SLRU cache algorithm here.
     * This is required some additional parameters, e.g. protected and probationary size.
     * See details about SLRU algorithm: http://en.wikipedia.org/wiki/Cache_algorithms#Segmented_LRU
     */
    private static final int CACHE_PARTITIONS_NUM        = 1 << 3;
    private static final int CACHE_PROTECTED_SIZE        = 100;
    private static final int CACHE_PROBATIONARY_SIZE     = 200;
    private static final int MASK                        = CACHE_PARTITIONS_NUM - 1;
    private static final int PARTITION_PROTECTED_SIZE    = CACHE_PROTECTED_SIZE / CACHE_PARTITIONS_NUM;
    private static final int PARTITION_PROBATIONARY_SIZE = CACHE_PROBATIONARY_SIZE / CACHE_PARTITIONS_NUM;
    // end cache parameters

    private static final int MAX_BUFFER_SIZE  = 200 * 1024; // 200k
    private static final int COPY_BUFFER_SIZE = 8 * 1024; // 8k

    private static final long LOCK_FILE_TIMEOUT     = 60000; // 60 seconds
    private static final int  FILE_LOCK_MAX_THREADS = 1024;

    static final String SERVICE_DIR = ".vfs";

    static final String ACL_DIR         = SERVICE_DIR + java.io.File.separatorChar + "acl";
    static final String ACL_FILE_SUFFIX = "_acl";

    static final String LOCKS_DIR        = SERVICE_DIR + java.io.File.separatorChar + "locks";
    static final String LOCK_FILE_SUFFIX = "_lock";

    static final String PROPS_DIR              = SERVICE_DIR + java.io.File.separatorChar + "props";
    static final String PROPERTIES_FILE_SUFFIX = "_props";


    /** Hide .vfs directory. */
    private static final java.io.FilenameFilter SERVICE_DIR_FILTER = new java.io.FilenameFilter() {
        @Override
        public boolean accept(java.io.File dir, String name) {
            return !(SERVICE_DIR.equals(name));
        }
    };

    /** Hide .vfs and .git directories. */
    private static final java.io.FilenameFilter SERVICE_GIT_DIR_FILTER = new OrFileNameFilter(SERVICE_DIR_FILTER, GIT_FILTER);

    private static class OrFileNameFilter implements java.io.FilenameFilter {
        private final java.io.FilenameFilter[] filters;

        private OrFileNameFilter(java.io.FilenameFilter... filters) {
            this.filters = filters;
        }

        @Override
        public boolean accept(java.io.File dir, String name) {
            for (java.io.FilenameFilter filter : filters) {
                if (!filter.accept(dir, name)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static final FileLock NO_LOCK = new FileLock("no_lock", 0);

    private class FileLockCache extends LoadingValueSLRUCache<Path, FileLock> {
        FileLockCache() {
            super(PARTITION_PROTECTED_SIZE, PARTITION_PROBATIONARY_SIZE);
        }

        @Override
        protected FileLock loadValue(Path key) {
            DataInputStream dis = null;

            try {
                final Path lockFilePath = getLockFilePath(key);
                final java.io.File lockIoFile = new java.io.File(ioRoot, toIoPath(lockFilePath));
                if (lockIoFile.exists()) {
                    final PathLockFactory.PathLock lockFilePathLock =
                            pathLockFactory.getLock(lockFilePath, false).acquire(LOCK_FILE_TIMEOUT);
                    try {
                        dis = new DataInputStream(new BufferedInputStream(new FileInputStream(lockIoFile)));
                        return locksSerializer.read(dis);
                    } finally {
                        lockFilePathLock.release();
                    }
                }
                return NO_LOCK;
            } catch (IOException e) {
                String msg = String.format("Unable read lock for '%s'. ", key);
                LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
                throw new RuntimeException(msg);
            } finally {
                closeQuietly(dis);
            }
        }
    }


    private class FileMetadataCache extends LoadingValueSLRUCache<Path, Map<String, String[]>> {
        FileMetadataCache() {
            super(PARTITION_PROTECTED_SIZE, PARTITION_PROBATIONARY_SIZE);
        }

        @Override
        protected Map<String, String[]> loadValue(Path key) {
            DataInputStream dis = null;
            try {
                final Path metadataFilePath = getMetadataFilePath(key);
                java.io.File metadataIoFile = new java.io.File(ioRoot, toIoPath(metadataFilePath));
                if (metadataIoFile.exists()) {
                    final PathLockFactory.PathLock metadataFilePathLock =
                            pathLockFactory.getLock(metadataFilePath, false).acquire(LOCK_FILE_TIMEOUT);
                    try {
                        dis = new DataInputStream(new BufferedInputStream(new FileInputStream(metadataIoFile)));
                        return metadataSerializer.read(dis);
                    } finally {
                        metadataFilePathLock.release();
                    }
                }
                return Collections.emptyMap();
            } catch (IOException e) {
                String msg = String.format("Unable read properties for '%s'. ", key);
                LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
                throw new RuntimeException(msg);
            } finally {
                closeQuietly(dis);
            }
        }
    }


    private final String           workspaceId;
    private final java.io.File     ioRoot;
    private final EventService     eventService;
    private final SearcherProvider searcherProvider;
    private final SystemPathsFilter systemFilter;

    /* NOTE -- This does not related to virtual file system locking in any kind. -- */
    private final PathLockFactory pathLockFactory;

    private final VirtualFileImpl root;

    /* ----- Virtual file system lock feature. ----- */
    private final FileLockSerializer      locksSerializer;
    private final Cache<Path, FileLock>[] lockTokensCache;

    /* ----- File metadata. ----- */
    private final FileMetadataSerializer               metadataSerializer;
    private final Cache<Path, Map<String, String[]>>[] metadataCache;

    private final VirtualFileSystemUserContext userContext;

    /**
     * ACL that returned for any fs entry.
     */
    private final AccessControlList defaultAcl = new AccessControlList();

    /**
     * @param workspaceId
     *         id of workspace to which this MountPoint belongs to
     * @param ioRoot
     *         root directory for virtual file system. Any file in higher level than root are not accessible through
     *         virtual file system API.
     */
    @SuppressWarnings("unchecked")
    FSMountPoint(String workspaceId, java.io.File ioRoot, EventService eventService, SearcherProvider searcherProvider, SystemPathsFilter systemFilter) {
        this.workspaceId = workspaceId;
        this.ioRoot = ioRoot;
        this.eventService = eventService;
        this.searcherProvider = searcherProvider;
        this.systemFilter = systemFilter;

        root = new VirtualFileImpl(ioRoot, Path.ROOT, pathToId(Path.ROOT), this);
        pathLockFactory = new PathLockFactory(FILE_LOCK_MAX_THREADS);

        locksSerializer = new FileLockSerializer();
        lockTokensCache = new Cache[CACHE_PARTITIONS_NUM];

        metadataSerializer = new FileMetadataSerializer();
        metadataCache = new Cache[CACHE_PARTITIONS_NUM];

        for (int i = 0; i < CACHE_PARTITIONS_NUM; i++) {
            lockTokensCache[i] = new SynchronizedCache(new FileLockCache());
            metadataCache[i] = new SynchronizedCache(new FileMetadataCache());
        }
        userContext = VirtualFileSystemUserContext.newInstance();

        List<AccessControlEntry> acl = new ArrayList<>(2);
        acl.add(newDto(AccessControlEntry.class)
                        .withPrincipal(newDto(Principal.class)
                                               .withName("user")
                                               .withType(Principal.Type.GROUP))
                        .withPermissions(singletonList("all")));

        acl.add(newDto(AccessControlEntry.class)
                        .withPrincipal(newDto(Principal.class)
                                               .withName("temp_user")
                                               .withType(Principal.Type.GROUP))
                        .withPermissions(singletonList("all")));
        defaultAcl.update(acl, true);
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId;
    }

    @Override
    public VirtualFileImpl getRoot() {
        return root;
    }

    @Override
    public VirtualFileImpl getVirtualFileById(String id) throws NotFoundException, ForbiddenException, ServerException {
        if (root.getId().equals(id)) {
            return root;
        }
        return doGetVirtualFile(idToPath(id));
    }

    @Override
    public SearcherProvider getSearcherProvider() {
        return searcherProvider;
    }

    @Override
    public EventService getEventService() {
        return eventService;
    }

    @Override
    public VirtualFileImpl getVirtualFile(String path) throws NotFoundException, ForbiddenException, ServerException {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return getRoot();
        }
        return doGetVirtualFile(Path.fromString(path));
    }

    private VirtualFileImpl doGetVirtualFile(Path vfsPath) throws NotFoundException, ForbiddenException, ServerException {
        final VirtualFileImpl virtualFile =
                new VirtualFileImpl(new java.io.File(ioRoot, toIoPath(vfsPath)), vfsPath, pathToId(vfsPath), this);
        if (!virtualFile.exists()) {
            throw new NotFoundException(String.format("Object '%s' does not exists. ", vfsPath));
        }
        if (!hasPermission(virtualFile, BasicPermissions.READ.value(), true)) {
            throw new ForbiddenException(String.format("Unable get item '%s'. Operation not permitted. ", virtualFile.getPath()));
        }
        return virtualFile;
    }

    /** Call after unmount this MountPoint. Clear all caches. */
    public void reset() {
        clearMetadataCache();
        clearLockTokensCache();
    }

    // Used in tests. Need this to check state of PathLockFactory.
    // All locks MUST be released at the end of request lifecycle.
    PathLockFactory getPathLockFactory() {
        return pathLockFactory;
    }

   /* =================================== INTERNAL =================================== */

    // All methods below designed to be used from VirtualFileImpl ONLY.

    Path idToPath(String id) throws NotFoundException {
        if (id.equals(root.getId())) {
            return Path.ROOT;
        }
        final String raw;
        try {
            raw = new String(Base64.decodeBase64(id), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new IllegalStateException(e.getMessage(), e);
        }
        final int split = raw.indexOf(':') + 1;
        if (split > 0) {
            return Path.fromString(raw.substring(split));
        }
        // Invalid format of ID
        throw new NotFoundException(String.format("Object '%s' does not exists. ", id));
    }


    String pathToId(Path path) {
        try {
            return Base64.encodeBase64URLSafeString((workspaceId + ':' + (path.isRoot() ? "root" : path.toString())).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // Should never happen.
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    VirtualFileImpl getParent(VirtualFileImpl virtualFile) {
        if (virtualFile.isRoot()) {
            return null;
        }
        final Path parentPath = virtualFile.getVirtualFilePath().getParent();
        return new VirtualFileImpl(new java.io.File(ioRoot, toIoPath(parentPath)), parentPath, pathToId(parentPath), this);
    }


    VirtualFileImpl getChild(VirtualFileImpl parent, String name) throws ForbiddenException {
        if (parent.isFile()) {
            return null;
        }
        final Path childPath = parent.getVirtualFilePath().newPath(name);
        final VirtualFileImpl child = new VirtualFileImpl(new java.io.File(parent.getIoFile(), name), childPath, pathToId(childPath), this);
        if (child.exists()) {
            if (systemFilter.accept(workspaceId, child.getVirtualFilePath())) {
                // Don't check permissions for file "misc.xml" in folder ".codenvy". Dirty huck :( but seems simplest solution for now.
                // Need to work with 'misc.xml' independently to user.
                if (!hasPermission(child, BasicPermissions.READ.value(), true)) {
                    throw new ForbiddenException(String.format("Unable get item '%s'. Operation not permitted. ", child.getPath()));
                }
            }
            return child;
        }

        return null;
    }


    LazyIterator<VirtualFile> getChildren(VirtualFileImpl parent, VirtualFileFilter filter) throws ServerException {
        if (!parent.isFolder()) {
            return LazyIterator.emptyIterator();
        }

        if (parent.isRoot()) {
            // NOTE: We do not check read permissions when access to ROOT folder.
            if (!hasPermission(parent, BasicPermissions.READ.value(), false)) {
                // User has not access to ROOT folder.
                return LazyIterator.emptyIterator();
            }
        }
        final List<VirtualFile> children = doGetChildren(parent, SERVICE_DIR_FILTER);
        for (Iterator<VirtualFile> iterator = children.iterator(); iterator.hasNext(); ) {
            VirtualFile child = iterator.next();
            // Check permission directly for current file only.
            // We know the parent is accessible for current user otherwise we should not be here.
            if (!hasPermission((VirtualFileImpl)child, BasicPermissions.READ.value(), false) || !filter.accept(child)) {
                iterator.remove(); // Do not show item in list if current user has not permission to see it
            }
        }
        // Always sort to get the exact same order of files for each listing.
        Collections.sort(children);
        return LazyIterator.fromList(children);
    }


    private List<VirtualFile> doGetChildren(VirtualFileImpl virtualFile, java.io.FilenameFilter filter) throws ServerException {
        final String[] names = virtualFile.getIoFile().list(filter);
        if (names == null) {
            // Something wrong. According to java docs may be null only if i/o error occurs.
            throw new ServerException(String.format("Unable get children '%s'. ", virtualFile.getPath()));
        }
        final List<VirtualFile> children = new ArrayList<>(names.length);
        for (String name : names) {
            final Path childPath = virtualFile.getVirtualFilePath().newPath(name);
            children.add(new VirtualFileImpl(new java.io.File(ioRoot, toIoPath(childPath)), childPath, pathToId(childPath), this));
        }
        return children;
    }


    VirtualFileImpl createFile(VirtualFileImpl parent, String name, InputStream content)
            throws ForbiddenException, ConflictException, ServerException {
        checkName(name);

        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable create new file. Item specified as parent is not a folder. ");
        }

        final Path newPath = parent.getVirtualFilePath().newPath(name);
        if (systemFilter.accept(workspaceId, newPath)) {
            // Don't check permissions when create file "misc.xml" in folder ".codenvy". Dirty huck :( but seems simplest solution for now.
            // Need to work with 'misc.xml' independently to user.
            if (!hasPermission(parent, BasicPermissions.WRITE.value(), true)) {
                throw new ForbiddenException(String.format("Unable create new file in '%s'. Operation not permitted. ", parent.getPath()));
            }
        }
        final java.io.File newIoFile = new java.io.File(ioRoot, toIoPath(newPath));
        try {
            if (!newIoFile.createNewFile()) { // atomic
                throw new ConflictException(String.format("Item '%s' already exists. ", newPath));
            }
        } catch (IOException e) {
            String msg = String.format("Unable create new file '%s'. ", newPath);
            LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
            throw new ServerException(msg);
        }

        final VirtualFileImpl newVirtualFile = new VirtualFileImpl(newIoFile, newPath, pathToId(newPath), this);
        // Update content if any.
        if (content != null) {
            doUpdateContent(newVirtualFile, content);
        }

        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(this, true).add(newVirtualFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        eventService.publish(new CreateEvent(workspaceId, newVirtualFile.getPath(), false));
        return newVirtualFile;
    }


    VirtualFileImpl createFolder(VirtualFileImpl parent, String name) throws ForbiddenException, ConflictException, ServerException {
        checkName(name);

        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable create folder. Item specified as parent is not a folder. ");
        }

        if (!hasPermission(parent, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(
                    String.format("Unable create new folder in '%s'. Operation not permitted. ", parent.getPath()));
        }
        // Name may be hierarchical, e.g. folder1/folder2/folder3.
        // Some folder in hierarchy may already exists but at least one folder must be created.
        // If no one folder created then ItemAlreadyExistException is thrown.
        Path currentPath = parent.getVirtualFilePath();
        Path newPath = null;
        java.io.File newIoFile = null;
        for (String element : Path.fromString(name).elements()) {
            currentPath = currentPath.newPath(element);
            java.io.File currentIoFile = new java.io.File(ioRoot, toIoPath(currentPath));
            if (currentIoFile.mkdir()) {
                newPath = currentPath;
                newIoFile = currentIoFile;
            }
        }

        if (newPath == null) {
            // Folder or folder hierarchy already exists.
            throw new ConflictException(String.format("Item '%s' already exists. ", parent.getVirtualFilePath().newPath(name)));
        }

        // Return first created folder, e.g. assume we need create: folder1/folder2/folder3 in specified folder.
        // If folder1 already exists then return folder2 as first created in hierarchy.
        final VirtualFileImpl newVirtualFile = new VirtualFileImpl(newIoFile, newPath, pathToId(newPath), this);
        eventService.publish(new CreateEvent(workspaceId, newVirtualFile.getPath(), true));
        return newVirtualFile;
    }

    VirtualFileImpl copy(VirtualFileImpl source, VirtualFileImpl parent) throws ForbiddenException, ConflictException, ServerException {
        return copy(source, parent, null, false);
    }

    /**
     * Copy a VirtualFileImpl to a given location
     *
     * @param source the VirtualFileImpl instance to copy
     * @param parent the VirtualFileImpl (must be a folder) which will become
     * the parent of the source
     * @param name the name of the copy, can be left {@code null} or empty
     * {@code String} for current source name
     * @param overWrite should the destination be overwritten, set to true to
     * overwrite, false otherwise
     * @return an instance of VirtualFileImpl, which is the actual copy of
     * source under parent
     * @throws ForbiddenException
     * @throws ConflictException
     * @throws ServerException
     */
    @Beta
    public VirtualFileImpl copy(VirtualFileImpl source, VirtualFileImpl parent, String name, boolean overWrite) throws ForbiddenException, ConflictException, ServerException {
        if (source.getVirtualFilePath().equals(parent.getVirtualFilePath())) {
            throw new ForbiddenException("Item cannot be copied to itself. ");
        }
        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable copy item. Item specified as parent is not a folder. ");
        }
        if (!hasPermission(parent, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable copy item '%s' to %s. Operation not permitted. ",
                                                       source.getPath(), parent.getPath()));
        }
        String newName = nullToEmpty(name).trim().isEmpty() ? source.getName() : name;
        final Path newPath = parent.getVirtualFilePath().newPath(newName); // TODO: change name here
        final File theFile = new File(ioRoot, toIoPath(newPath));
        final VirtualFileImpl destination
                = new VirtualFileImpl(theFile, newPath, pathToId(newPath), this);

        // checking override
        if (destination.exists()) {
            doOverWrite(overWrite, destination, newPath);
        }

        doCopy(source, destination);
        eventService.publish(new CreateEvent(workspaceId, destination.getPath(), source.isFolder()));
        return destination;
    }


    private void doCopy(VirtualFileImpl source, VirtualFileImpl destination) throws ServerException {
        try {
            // First copy metadata (properties) for source.
            // If we do in this way and fail cause to any i/o or
            // other error client will see error and may try to copy again.
            // But if we successfully copy tree (or single file) and then
            // fail to copy metadata client may not try to copy again
            // because copy destination already exists.

            // NOTE: Don't copy lock and permissions, just files itself and metadata files.

            // Check recursively permissions of sources in case of folder
            // and add all item current user cannot read in skip list.
            java.io.FilenameFilter filter = null;
            if (source.isFolder()) {
                final LinkedList<VirtualFileImpl> skipList = new LinkedList<>();
                final LinkedList<VirtualFile> q = new LinkedList<>();
                q.add(source);
                while (!q.isEmpty()) {
                    for (VirtualFile current : doGetChildren((VirtualFileImpl)q.pop(), SERVICE_GIT_DIR_FILTER)) {
                        // Check permission directly for current file only.
                        // We already know parent accessible for current user otherwise we should not be here.
                        // Ignore item if don't have permission to read it.
                        if (!hasPermission((VirtualFileImpl)current, BasicPermissions.READ.value(), false)) {
                            skipList.add((VirtualFileImpl)current);
                        } else {
                            if (current.isFolder()) {
                                q.add(current);
                            }
                        }
                    }
                }
                if (!skipList.isEmpty()) {
                    filter = new java.io.FilenameFilter() {
                        @Override
                        public boolean accept(java.io.File dir, String name) {
                            final String testPath = dir.getAbsolutePath() + java.io.File.separatorChar + name;
                            for (VirtualFileImpl skipFile : skipList) {
                                if (testPath.startsWith(skipFile.getIoFile().getAbsolutePath())) {
                                    return false;
                                }
                                final java.io.File metadataFile =
                                        new java.io.File(ioRoot, toIoPath(getMetadataFilePath(skipFile.getVirtualFilePath())));
                                if (metadataFile.exists() && testPath.startsWith(metadataFile.getAbsolutePath())) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    };
                }
            }

            final java.io.File sourceMetadataFile = new java.io.File(ioRoot, toIoPath(getMetadataFilePath(source.getVirtualFilePath())));
            final java.io.File destinationMetadataFile =
                    new java.io.File(ioRoot, toIoPath(getMetadataFilePath(destination.getVirtualFilePath())));
            if (sourceMetadataFile.exists()) {
                nioCopy(sourceMetadataFile, destinationMetadataFile, filter);
            }
            nioCopy(source.getIoFile(), destination.getIoFile(), filter);

            if (searcherProvider != null) {
                try {
                    searcherProvider.getSearcher(this, true).add(destination);
                } catch (ServerException e) {
                    LOG.error(e.getMessage(), e); // just log about i/o error in index
                }
            }
        } catch (IOException e) {
            // Do nothing for file tree. Let client side decide what to do.
            // User may delete copied files (if any) and try copy again.
            String msg = String.format("Unable copy '%s' to '%s'. ", source, destination);
            LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
            throw new ServerException(msg);
        }
    }


    VirtualFileImpl rename(VirtualFileImpl virtualFile, String newName, String newMediaType, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        if (virtualFile.isRoot()) {
            throw new ForbiddenException("Unable rename root folder. ");
        }
        final String sourcePath = virtualFile.getPath();
        if (!hasPermission(virtualFile, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable rename item '%s'. Operation not permitted. ", sourcePath));
        }
        if (virtualFile.isFile() && !validateLockTokenIfLocked(virtualFile, lockToken)) {
            throw new ForbiddenException(String.format("Unable rename file '%s'. File is locked. ", sourcePath));
        }
        final String name = virtualFile.getName();
        final VirtualFileImpl renamed;
        if (!(newName == null || name.equals(newName))) {
            final Path newPath = virtualFile.getVirtualFilePath().getParent().newPath(newName);
            renamed = new VirtualFileImpl(new java.io.File(ioRoot, toIoPath(newPath)), newPath, pathToId(newPath), this);
            if (renamed.exists()) {
                throw new ConflictException(String.format("Item '%s' already exists. ", renamed.getName()));
            }
            // use copy and delete
            doCopy(virtualFile, renamed);
            // permissions is not copied with 'doCopy' method, copy them now if any
            final AccessControlList sourceAcl = getACL(virtualFile);
            if (!sourceAcl.isEmpty()) {
                final java.io.File renamedAclFile = new java.io.File(ioRoot, toIoPath(getAclFilePath(renamed.getVirtualFilePath())));
                DataOutputStream dos = null;
                try {
                    // Ignore result of 'mkdirs' here. If we are failed to create directory
                    // We will get FileNotFoundException at the next line when try to create FileOutputStream.
                    renamedAclFile.getParentFile().mkdirs();
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(renamedAclFile)));
                } catch (IOException e) {
                    String msg = String.format("Unable save ACL for '%s'. ", virtualFile.getPath());
                    LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
                    throw new ServerException(msg);
                } finally {
                    closeQuietly(dos);
                }
            }
            doDelete(virtualFile, lockToken);
        } else {
            renamed = virtualFile;
        }

        if (newMediaType != null) {
            setProperty(renamed, "vfs:mimeType", newMediaType);
            if (!virtualFile.getIoFile().setLastModified(System.currentTimeMillis())) {
                LOG.warn("Unable to set timestamp to '{}'. ", virtualFile.getIoFile());
            }
        }
        eventService.publish(new RenameEvent(workspaceId, renamed.getPath(), sourcePath, renamed.isFolder()));
        return renamed;
    }


    VirtualFileImpl move(VirtualFileImpl source, VirtualFileImpl parent, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        return move(source, parent, null, false, lockToken);
    }

    /**
     * Move a VirtualFileImpl to a given location
     *
     * @param source the VirtualFileImpl instance to move
     * @param parent the VirtualFileImpl (must be a folder) which will become
     * the parent of the source
     * @param name a new name for the moved source, can be left {@code null} or
     * empty {@code String} for current source name
     * @param overWrite should the destination be overwritten, set to true to
     * overwrite, false otherwise
     * @return an instance of VirtualFileImpl, source under parent
     * @throws ForbiddenException
     * @throws ConflictException
     * @throws ServerException
     */
    @Beta
    VirtualFileImpl move(VirtualFileImpl source, VirtualFileImpl parent, String name, boolean overWrite, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        final String sourcePath = source.getPath();
        final String parentPath = parent.getPath();
        if (source.isRoot()) {
            throw new ForbiddenException("Unable move root folder. ");
        }
        if (source.getVirtualFilePath().equals(parent.getVirtualFilePath())) {
            throw new ForbiddenException("Item cannot be moved to itself. ");
        }
        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable move. Item specified as parent is not a folder. ");
        }
        if (source.isFolder() && parent.getVirtualFilePath().isChild(source.getVirtualFilePath())) {
            throw new ForbiddenException(String.format("Unable move item '%s' to '%s'. Item may not have itself as parent. ",
                                                       sourcePath, parentPath));
        }

        if (!(hasPermission(source, BasicPermissions.WRITE.value(), true)
              && hasPermission(parent, BasicPermissions.WRITE.value(), true))) {
            throw new ForbiddenException(
                    String.format("Unable move item '%s' to %s. Operation not permitted. ", sourcePath, parentPath));
        }
        // Even we check lock before delete original file check it here also to have better behaviour.
        // Prevent even copy original file if we already know it is locked.
        if (source.isFile() && !validateLockTokenIfLocked(source, lockToken)) {
            throw new ForbiddenException(String.format("Unable move file '%s'. File is locked. ", sourcePath));
        }

        String newName = nullToEmpty(name).trim().isEmpty() ? source.getName() : name;
        final Path newPath = parent.getVirtualFilePath().newPath(newName);
        VirtualFileImpl destination
                = new VirtualFileImpl(new java.io.File(ioRoot, toIoPath(newPath)), newPath, pathToId(newPath), this);

        // checking override
        if (destination.exists()) {
            doOverWrite(overWrite, destination, newPath);
        }

        // use copy and delete
        doCopy(source, destination);
        doDelete(source, lockToken);
        eventService.publish(new MoveEvent(workspaceId, destination.getPath(), sourcePath, destination.isFolder()));
        return destination;
    }

    private void doOverWrite(boolean overWrite, VirtualFileImpl destination, final Path newPath) throws ForbiddenException, ConflictException, ServerException {
        // if we override, then dest needs to be erased before proceeding with copy
        if (overWrite) {
            String token = null;
            if (destination.isFile()) {
                token = destination.lock(0);
            }
            destination.delete(token);
        } else {
            throw new ConflictException(String.format("Item '%s' already exists. ", newPath));
        }
    }

    ContentStream getContent(VirtualFileImpl virtualFile) throws ForbiddenException, ServerException {
        if (!virtualFile.isFile()) {
            throw new ForbiddenException(String.format("Unable get content. Item '%s' is not a file. ", virtualFile.getPath()));
        }

        final PathLockFactory.PathLock lock = pathLockFactory.getLock(virtualFile.getVirtualFilePath(), false).acquire(LOCK_FILE_TIMEOUT);
        try {
            final java.io.File ioFile = virtualFile.getIoFile();
            FileInputStream fIn = null;
            try {
                final long fLength = ioFile.length();
                if (fLength <= MAX_BUFFER_SIZE) {
                    // If file small enough save its content in memory.
                    fIn = new FileInputStream(ioFile);
                    final byte[] buff = new byte[(int)fLength];
                    int offset = 0;
                    int len = buff.length;
                    int r;
                    while ((r = fIn.read(buff, offset, len)) > 0) {
                        offset += r;
                        len -= r;
                    }
                    return new ContentStream(virtualFile.getName(), new ByteArrayInputStream(buff),
                                             virtualFile.getMediaType(), buff.length, new Date(ioFile.lastModified()));
                }

                // Otherwise copy this file to be able release the file lock before leave this method.
                final java.io.File f = java.io.File.createTempFile("spool_file", null);
                nioCopy(ioFile, f, null);
                return new ContentStream(virtualFile.getName(), new DeleteOnCloseFileInputStream(f),
                                         virtualFile.getMediaType(), fLength, new Date(ioFile.lastModified()));
            } catch (IOException e) {
                String msg = String.format("Unable get content of '%s'. ", virtualFile.getPath());
                LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
                throw new ServerException(msg);
            } finally {
                closeQuietly(fIn);
            }
        } finally {
            lock.release();
        }
    }

    void updateContent(VirtualFileImpl virtualFile, InputStream content, String lockToken) throws ForbiddenException, ServerException {
        if (!virtualFile.isFile()) {
            throw new ForbiddenException(String.format("Unable update content. Item '%s' is not file. ", virtualFile.getPath()));
        }

        if (systemFilter.accept(workspaceId, virtualFile.getVirtualFilePath())) {
            // Don't check permissions when update file ".codenvy/misc.xml". Dirty huck :( but seems simplest solution for now.
            // Need to work with 'misc.xml' independently to user.
            if (!hasPermission(virtualFile, BasicPermissions.WRITE.value(), true)) {
                throw new ForbiddenException(
                        String.format("Unable update content of file '%s'. Operation not permitted. ", virtualFile.getPath()));
            }
        }
        if (!validateLockTokenIfLocked(virtualFile, lockToken)) {
            throw new ForbiddenException(String.format("Unable update content of file '%s'. File is locked. ", virtualFile.getPath()));
        }

        doUpdateContent(virtualFile, content);

        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(this, true).update(virtualFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        eventService.publish(new UpdateContentEvent(workspaceId, virtualFile.getPath()));
    }

    private void doUpdateContent(VirtualFileImpl virtualFile, InputStream content) throws ServerException {
        final PathLockFactory.PathLock lock = pathLockFactory.getLock(virtualFile.getVirtualFilePath(), true).acquire(LOCK_FILE_TIMEOUT);
        try {
            _doUpdateContent(virtualFile, content);
        } finally {
            lock.release();
        }
    }

    // UNDER LOCK
    private void _doUpdateContent(VirtualFileImpl virtualFile, InputStream content) throws ServerException {
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(virtualFile.getIoFile());
            final byte[] buff = new byte[COPY_BUFFER_SIZE];
            int r;
            while ((r = content.read(buff)) != -1) {
                fOut.write(buff, 0, r);
            }
        } catch (IOException e) {
            String msg = String.format("Unable set content of '%s'. ", virtualFile.getPath());
            LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
            throw new ServerException(msg);
        } finally {
            closeQuietly(fOut);
        }
    }


    void delete(VirtualFileImpl virtualFile, String lockToken) throws ForbiddenException, ServerException {
        if (virtualFile.isRoot()) {
            throw new ForbiddenException("Unable delete root folder. ");
        }
        final String myPath = virtualFile.getPath();
        final boolean folder = virtualFile.isFolder();
        if (!hasPermission(virtualFile, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable delete item '%s'. Operation not permitted. ", myPath));
        }
        if (virtualFile.isFile() && !validateLockTokenIfLocked(virtualFile, lockToken)) {
            throw new ForbiddenException(String.format("Unable delete item '%s'. Item is locked. ", myPath));
        }

        doDelete(virtualFile, lockToken);
        eventService.publish(new DeleteEvent(workspaceId, myPath, folder));
    }

    private void doDelete(VirtualFileImpl virtualFile, String lockToken) throws ForbiddenException, ServerException {
        if (virtualFile.isFolder()) {
            final LinkedList<VirtualFile> q = new LinkedList<>();
            q.add(virtualFile);
            while (!q.isEmpty()) {
                for (VirtualFile child : doGetChildren((VirtualFileImpl)q.pop(), SERVICE_GIT_DIR_FILTER)) {
                    // Check permission directly for current file only.
                    // We already know parent may be deleted by current user otherwise we should not be here.
                    if (!hasPermission((VirtualFileImpl)child, BasicPermissions.WRITE.value(), false)) {
                        throw new ForbiddenException(String.format("Unable delete item '%s'. Operation not permitted. ", child.getPath()));
                    }
                    if (child.isFolder()) {
                        q.push(child);
                    } else if (isLocked((VirtualFileImpl)child)) {
                        // Do not check lock token here. It checked only when remove file directly.
                        // If folder contains locked children it may not be deleted.
                        throw new ForbiddenException(String.format("Unable delete item '%s'. Child item '%s' is locked. ",
                                                                   virtualFile.getPath(), child.getPath()));
                    }
                }
            }
        }

        // unlock file
        if (virtualFile.isFile()) {
            final FileLock fileLock = checkIsLockValidAndGet(virtualFile);
            if (NO_LOCK != fileLock) {
                doUnlock(virtualFile, fileLock, lockToken);
            }
        }

        // clear caches
        clearLockTokensCache();
        clearMetadataCache();

        final String path = virtualFile.getPath();
        boolean isFile = virtualFile.isFile();
        if (!deleteRecursive(virtualFile.getIoFile())) {
            LOG.error("Unable delete file {}", virtualFile.getIoFile());
            throw new ServerException(String.format("Unable delete item '%s'. ", path));
        }

        // delete ACL file
        final java.io.File aclFile = new java.io.File(ioRoot, toIoPath(getAclFilePath(virtualFile.getVirtualFilePath())));
        if (aclFile.delete()) {
            if (aclFile.exists()) {
                LOG.error("Unable delete ACL file {}", aclFile);
                throw new ServerException(String.format("Unable delete item '%s'. ", path));
            }
        }

        // delete metadata file
        final java.io.File metadataFile = new java.io.File(ioRoot, toIoPath(getMetadataFilePath(virtualFile.getVirtualFilePath())));
        if (metadataFile.delete()) {
            if (metadataFile.exists()) {
                LOG.error("Unable delete file metadata {}", metadataFile);
                throw new ServerException(String.format("Unable delete item '%s'. ", path));
            }
        }

        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(this, true).delete(path, isFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }


    private void clearLockTokensCache() {
        for (Cache<Path, FileLock> cache : lockTokensCache) {
            cache.clear();
        }
    }


    private void clearMetadataCache() {
        for (Cache<Path, Map<String, String[]>> cache : metadataCache) {
            cache.clear();
        }
    }


    ContentStream zip(VirtualFileImpl virtualFile, VirtualFileFilter filter) throws ForbiddenException, ServerException {
        if (!virtualFile.isFolder()) {
            throw new ForbiddenException(String.format("Unable export to zip. Item '%s' is not a folder. ", virtualFile.getPath()));
        }
        java.io.File zipFile = null;
        FileOutputStream out = null;
        try {
            zipFile = java.io.File.createTempFile("export", ".zip");
            out = new FileOutputStream(zipFile);
            final ZipOutputStream zipOut = new ZipOutputStream(out);
            final LinkedList<VirtualFile> q = new LinkedList<>();
            q.add(virtualFile);
            final int zipEntryNameTrim = virtualFile.getVirtualFilePath().length();
            final byte[] buff = new byte[COPY_BUFFER_SIZE];
            while (!q.isEmpty()) {
                for (VirtualFile current : doGetChildren((VirtualFileImpl)q.pop(), SERVICE_GIT_DIR_FILTER)) {
                    // (1) Check filter.
                    // (2) Check permission directly for current file only.
                    // We already know parent accessible for current user otherwise we should not be here.
                    // Ignore item if don't have permission to read it.
                    if (filter.accept(current) && hasPermission((VirtualFileImpl)current, BasicPermissions.READ.value(), false)) {
                        final String zipEntryName = current.getVirtualFilePath().subPath(zipEntryNameTrim).toString().substring(1);
                        if (current.isFile()) {
                            final ZipEntry zipEntry = new ZipEntry(zipEntryName);
                            zipOut.putNextEntry(zipEntry);
                            InputStream in = null;
                            final PathLockFactory.PathLock lock =
                                    pathLockFactory.getLock(current.getVirtualFilePath(), false).acquire(LOCK_FILE_TIMEOUT);
                            try {
                                zipEntry.setTime(virtualFile.getLastModificationDate());
                                in = new FileInputStream(((VirtualFileImpl)current).getIoFile());
                                int r;
                                while ((r = in.read(buff)) != -1) {
                                    zipOut.write(buff, 0, r);
                                }
                            } finally {
                                closeQuietly(in);
                                lock.release();
                            }
                            zipOut.closeEntry();
                        } else if (current.isFolder()) {
                            final ZipEntry zipEntry = new ZipEntry(zipEntryName + '/');
                            zipEntry.setTime(0);
                            zipOut.putNextEntry(zipEntry);
                            q.add(current);
                            zipOut.closeEntry();
                        }
                    }
                }
            }
            closeQuietly(zipOut);
            final String name = virtualFile.getName() + ".zip";
            return new ContentStream(name, new DeleteOnCloseFileInputStream(zipFile), ExtMediaType.APPLICATION_ZIP, zipFile.length(), new Date());
        } catch (IOException | RuntimeException ioe) {
            if (zipFile != null) {
                zipFile.delete();
            }
            throw new ServerException(ioe.getMessage(), ioe);
        } finally {
            closeQuietly(out);
        }
    }


    void unzip(VirtualFileImpl parent, InputStream zipped, boolean overwrite, int stripNumber)
            throws ForbiddenException, ConflictException, ServerException {
        if (!parent.isFolder()) {
            throw new ForbiddenException(String.format("Unable import zip content. Item '%s' is not a folder. ", parent.getPath()));
        }
        final ZipContent zipContent;
        try {
            zipContent = ZipContent.newInstance(zipped);
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
        if (!hasPermission(parent, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable import from zip to '%s'. Operation not permitted. ", parent.getPath()));
        }

        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(zipContent.zippedData);
            // Wrap zip stream to prevent close it. We can pass stream to other method and it can read content of current
            // ZipEntry but not able to close original stream of ZIPed data.
            InputStream noCloseZip = new NotClosableInputStream(zip);
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                VirtualFileImpl current = parent;
                Path relPath = Path.fromString(zipEntry.getName());

                if (stripNumber > 0) {
                    int currentLevel = relPath.elements().length;
                    if (currentLevel <= stripNumber) {
                        continue;
                    }
                    relPath = relPath.subPath(stripNumber);
                }

                final String name = relPath.getName();
                if (relPath.length() > 1) {
                    // create all required parent directories
                    final Path parentPath = parent.getVirtualFilePath().newPath(relPath.subPath(0, relPath.length() - 1));
                    current = new VirtualFileImpl(new java.io.File(ioRoot, toIoPath(parentPath)), parentPath, pathToId(parentPath), this);
                    if (!(current.exists() || current.getIoFile().mkdirs())) {
                        throw new ServerException(String.format("Unable create directory '%s' ", parentPath));
                    }
                }
                final Path newPath = current.getVirtualFilePath().newPath(name);
                if (zipEntry.isDirectory()) {
                    final java.io.File dir = new java.io.File(current.getIoFile(), name);
                    if (!dir.exists()) {
                        if (dir.mkdir()) {
                            eventService.publish(new CreateEvent(workspaceId, newPath.toString(), true));
                        } else {
                            throw new ServerException(String.format("Unable create directory '%s' ", newPath));
                        }
                    }
                } else {
                    final VirtualFileImpl file =
                            new VirtualFileImpl(new java.io.File(current.getIoFile(), name), newPath, pathToId(newPath), this);
                    if (file.exists()) {
                        if (isLocked(file)) {
                            throw new ForbiddenException(String.format("File '%s' already exists and locked. ", file.getPath()));
                        }
                        if (!hasPermission(file, BasicPermissions.WRITE.value(), true)) {
                            throw new ForbiddenException(
                                    String.format("Unable update file '%s'. Operation not permitted. ", file.getPath()));
                        }
                    }

                    boolean newFile;
                    try {
                        if (!(newFile = file.getIoFile().createNewFile())) { // atomic
                            if (!overwrite) {
                                throw new ConflictException(String.format("File '%s' already exists. ", file.getPath()));
                            }
                        }
                    } catch (IOException e) {
                        String msg = String.format("Unable create new file '%s'. ", newPath);
                        LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
                        throw new ServerException(msg);
                    }

                    doUpdateContent(file, noCloseZip);
                    if (newFile) {
                        eventService.publish(new CreateEvent(workspaceId, newPath.toString(), false));
                    } else {
                        eventService.publish(new UpdateContentEvent(workspaceId, newPath.toString()));
                    }
                }
                zip.closeEntry();
            }
            if (searcherProvider != null) {
                try {
                    searcherProvider.getSearcher(this, true).add(parent);
                } catch (ServerException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        } finally {
            closeQuietly(zip);
        }
    }

   /* ============ LOCKING ============ */

    String lock(VirtualFileImpl virtualFile, long timeout) throws ForbiddenException, ConflictException, ServerException {
        if (!virtualFile.isFile()) {
            throw new ForbiddenException(String.format("Unable lock '%s'. Locking allowed for files only. ", virtualFile.getPath()));
        }

        if (!hasPermission(virtualFile, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable lock '%s'. Operation not permitted. ", virtualFile.getPath()));
        }
        return doLock(virtualFile, timeout);
    }


    private String doLock(VirtualFileImpl virtualFile, long timeout) throws ConflictException, ServerException {
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        if (NO_LOCK == lockTokensCache[index].get(virtualFile.getVirtualFilePath())) // causes read from file if need.
        {
            final String lockToken = NameGenerator.generate(null, 16);
            final long expired = timeout > 0 ? (System.currentTimeMillis() + timeout) : Long.MAX_VALUE;
            final FileLock fileLock = new FileLock(lockToken, expired);
            DataOutputStream dos = null;
            try {
                final Path lockFilePath = getLockFilePath(virtualFile.getVirtualFilePath());
                final java.io.File lockIoFile = new java.io.File(ioRoot, toIoPath(lockFilePath));
                lockIoFile.getParentFile().mkdirs(); // Ignore result of 'mkdirs' here. If we are failed to create
                // directory we will get FileNotFoundException at the next line when try to create FileOutputStream.
                final PathLockFactory.PathLock lockFilePathLock = pathLockFactory.getLock(lockFilePath, true).acquire(LOCK_FILE_TIMEOUT);
                try {
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(lockIoFile)));
                    locksSerializer.write(dos, fileLock);
                } finally {
                    lockFilePathLock.release();
                }
            } catch (IOException e) {
                String msg = String.format("Unable lock file '%s'. ", virtualFile.getPath());
                LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
                throw new ServerException(msg);
            } finally {
                closeQuietly(dos);
            }

            // Save lock token in cache if lock successful.
            lockTokensCache[index].put(virtualFile.getVirtualFilePath(), fileLock);
            return lockToken;
        }

        throw new ConflictException(String.format("Unable lock file '%s'. File already locked. ", virtualFile.getPath()));
    }


    void unlock(VirtualFileImpl virtualFile, String lockToken) throws ForbiddenException, ConflictException, ServerException {
        if (lockToken == null) {
            throw new ForbiddenException("Null lock token. ");
        }
        if (!virtualFile.isFile()) {
            // Locks available for files only.
            throw new ConflictException(String.format("Item '%s' is not locked. ", virtualFile.getPath()));
        }
        final FileLock fileLock = checkIsLockValidAndGet(virtualFile);
        if (NO_LOCK == fileLock) {
            throw new ConflictException(String.format("File '%s' is not locked. ", virtualFile.getPath()));
        }
        doUnlock(virtualFile, fileLock, lockToken);
    }

    private void doUnlock(VirtualFileImpl virtualFile, FileLock lock, String lockToken) throws ForbiddenException, ServerException {
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        try {
            if (!lock.getLockToken().equals(lockToken)) {
                throw new ForbiddenException(String.format("Unable unlock file '%s'. Lock token does not match. ", virtualFile.getPath()));
            }
            final java.io.File lockIoFile = new java.io.File(ioRoot, toIoPath(getLockFilePath(virtualFile.getVirtualFilePath())));
            if (!lockIoFile.delete()) {
                throw new IOException(String.format("Unable delete lock file %s. ", lockIoFile));
            }
            // Mark as unlocked in cache.
            lockTokensCache[index].put(virtualFile.getVirtualFilePath(), NO_LOCK);
        } catch (IOException e) {
            String msg = String.format("Unable unlock file '%s'. ", virtualFile.getPath());
            LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
            throw new ServerException(msg);
        }
    }


    boolean isLocked(VirtualFileImpl virtualFile) {
        return virtualFile.isFile() && NO_LOCK != checkIsLockValidAndGet(virtualFile);
    }

    private FileLock checkIsLockValidAndGet(VirtualFileImpl virtualFile) {
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        // causes read from file if need
        final FileLock lock = lockTokensCache[index].get(virtualFile.getVirtualFilePath());
        if (NO_LOCK == lock) {
            return NO_LOCK;
        }
        if (lock.getExpired() < System.currentTimeMillis()) {
            final java.io.File lockIoFile = new java.io.File(ioRoot, toIoPath(getLockFilePath(virtualFile.getVirtualFilePath())));
            if (!lockIoFile.delete()) {
                if (lockIoFile.exists()) {
                    // just warn here
                    LOG.warn("Unable delete lock file %s. ", lockIoFile);
                }
            }
            lockTokensCache[index].put(virtualFile.getVirtualFilePath(), NO_LOCK);
            return NO_LOCK;
        }
        return lock;
    }


    private boolean validateLockTokenIfLocked(VirtualFileImpl virtualFile, String checkLockToken) {
        final FileLock lock = checkIsLockValidAndGet(virtualFile);
        return NO_LOCK == lock || lock.getLockToken().equals(checkLockToken);
    }


    private Path getLockFilePath(Path virtualFilePath) {
        return virtualFilePath.isRoot()
               ? virtualFilePath.newPath(LOCKS_DIR, virtualFilePath.getName() + LOCK_FILE_SUFFIX)
               : virtualFilePath.getParent().newPath(LOCKS_DIR, virtualFilePath.getName() + LOCK_FILE_SUFFIX);
    }

   /* ============ ACCESS CONTROL  ============ */

    AccessControlList getACL(VirtualFileImpl virtualFile) {
        return defaultAcl;
    }


    void updateACL(VirtualFileImpl virtualFile, List<AccessControlEntry> acl, boolean override, String lockToken)
            throws ForbiddenException, ServerException {
        eventService.publish(new UpdateACLEvent(workspaceId, virtualFile.getPath(), virtualFile.isFolder()));
    }


    private boolean hasPermission(VirtualFileImpl virtualFile, String p, boolean checkParent) {
        return true;
    }


    private Path getAclFilePath(Path virtualFilePath) {
        return virtualFilePath.isRoot()
               ? virtualFilePath.newPath(ACL_DIR, virtualFilePath.getName() + ACL_FILE_SUFFIX)
               : virtualFilePath.getParent().newPath(ACL_DIR, virtualFilePath.getName() + ACL_FILE_SUFFIX);
    }

   /* ============ METADATA  ============ */

    List<Property> getProperties(VirtualFileImpl virtualFile, PropertyFilter filter) {
        // Do not check permission here. We already check 'read' permission when get VirtualFile.
        final Map<String, String[]> metadata = getFileMetadata(virtualFile);
        final List<Property> result = new ArrayList<>(metadata.size());
        for (Map.Entry<String, String[]> e : metadata.entrySet()) {
            final String name = e.getKey();
            if (filter.accept(name)) {
                final Property property = DtoFactory.getInstance().createDto(Property.class).withName(name);
                if (e.getValue() != null) {
                    List<String> list = new ArrayList<>(e.getValue().length);
                    Collections.addAll(list, e.getValue());
                    property.setValue(list);
                }
                result.add(property);
            }
        }
        return result;
    }


    void updateProperties(VirtualFileImpl virtualFile, List<Property> properties, String lockToken)
            throws ForbiddenException, ServerException {
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        if (!hasPermission(virtualFile, BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(
                    String.format("Unable update properties for '%s'. Operation not permitted. ", virtualFile.getPath()));
        }

        if (virtualFile.isFile() && !validateLockTokenIfLocked(virtualFile, lockToken)) {
            throw new ForbiddenException(
                    String.format("Unable update properties of item '%s'. Item is locked. ", virtualFile.getPath()));
        }

        // 1. make copy of properties
        final Map<String, String[]> metadata = copyMetadataMap(metadataCache[index].get(virtualFile.getVirtualFilePath()));
        // 2. update
        for (Property property : properties) {
            final String name = property.getName();
            final List<String> value = property.getValue();
            if (value != null) {
                metadata.put(name, value.toArray(new String[value.size()]));
            } else {
                metadata.remove(name);
            }
        }

        // 3. save in file
        saveFileMetadata(virtualFile, metadata);
        // 4. update cache
        metadataCache[index].put(virtualFile.getVirtualFilePath(), metadata);
        // 5. update last modification time
        if (!virtualFile.getIoFile().setLastModified(System.currentTimeMillis())) {
            LOG.warn("Unable to set timestamp to '{}'. ", virtualFile.getIoFile());
        }
        eventService.publish(new UpdatePropertiesEvent(workspaceId, virtualFile.getPath(), virtualFile.isFolder()));
    }


    private Map<String, String[]> getFileMetadata(VirtualFileImpl virtualFile) {
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        return copyMetadataMap(metadataCache[index].get(virtualFile.getVirtualFilePath()));
    }


    String getPropertyValue(VirtualFileImpl virtualFile, String name) {
        // Do not check permission here. We already check 'read' permission when get VirtualFile.
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        final String[] value = metadataCache[index].get(virtualFile.getVirtualFilePath()).get(name);
        return value == null || value.length == 0 ? null : value[0];
    }


    String[] getPropertyValues(VirtualFileImpl virtualFile, String name) {
        // Do not check permission here. We already check 'read' permission when get VirtualFile.
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        final String[] value = metadataCache[index].get(virtualFile.getVirtualFilePath()).get(name);
        final String[] copyValue = new String[value.length];
        System.arraycopy(value, 0, copyValue, 0, value.length);
        return copyValue;
    }


    void setProperty(VirtualFileImpl virtualFile, String name, String value) throws ServerException {
        setProperty(virtualFile, name, value == null ? null : new String[]{value});
    }


    void setProperty(VirtualFileImpl virtualFile, String name, String... value) throws ServerException {
        final int index = virtualFile.getVirtualFilePath().hashCode() & MASK;
        // 1. make copy of properties
        final Map<String, String[]> metadata = copyMetadataMap(metadataCache[index].get(virtualFile.getVirtualFilePath()));
        // 2. update
        if (value != null) {
            String[] copyValue = new String[value.length];
            System.arraycopy(value, 0, copyValue, 0, value.length);
            metadata.put(name, copyValue);
        } else {
            metadata.remove(name);
        }
        // 3. save in file
        saveFileMetadata(virtualFile, metadata);
        // 4. update cache
        metadataCache[index].put(virtualFile.getVirtualFilePath(), metadata);
    }


    private void saveFileMetadata(VirtualFileImpl virtualFile, Map<String, String[]> properties) throws ServerException {
        DataOutputStream dos = null;

        try {
            final Path metadataFilePath = getMetadataFilePath(virtualFile.getVirtualFilePath());
            final java.io.File metadataFile = new java.io.File(ioRoot, toIoPath(metadataFilePath));
            if (properties.isEmpty()) {
                if (!metadataFile.delete()) {
                    if (metadataFile.exists()) {
                        throw new IOException(String.format("Unable delete file '%s'. ", metadataFile));
                    }
                }
            } else {
                metadataFile.getParentFile().mkdirs(); // Ignore result of 'mkdirs' here. If we are failed to create
                // directory we will get FileNotFoundException at the next line when try to create FileOutputStream.
                final PathLockFactory.PathLock lock = pathLockFactory.getLock(metadataFilePath, true).acquire(LOCK_FILE_TIMEOUT);
                try {
                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metadataFile)));
                    metadataSerializer.write(dos, properties);
                } finally {
                    lock.release();
                }
            }
        } catch (IOException e) {
            String msg = String.format("Unable save properties for '%s'. ", virtualFile.getPath());
            LOG.error(msg + e.getMessage(), e); // More details in log but do not show internal error to caller.
            throw new ServerException(msg);
        } finally {
            closeQuietly(dos);
        }
    }


    private Path getMetadataFilePath(Path virtualFilePath) {
        return virtualFilePath.isRoot()
               ? virtualFilePath.newPath(PROPS_DIR, virtualFilePath.getName() + PROPERTIES_FILE_SUFFIX)
               : virtualFilePath.getParent().newPath(PROPS_DIR, virtualFilePath.getName() + PROPERTIES_FILE_SUFFIX);
    }

   /* ============ VERSIONING ============ */
   /* versions is not supported in fact. Here implements simple contract for single version. */

    String getVersionId(VirtualFileImpl virtualFile) {
        return virtualFile.isFile() ? "0" : null;
    }

    LazyIterator<VirtualFile> getVersions(VirtualFileImpl virtualFile, VirtualFileFilter filter) throws ForbiddenException {
        if (!virtualFile.isFile()) {
            throw new ForbiddenException("Versioning allowed for files only. ");
        }
        if (filter.accept(virtualFile)) {
            return LazyIterator.<VirtualFile>singletonIterator(virtualFile);
        }
        return LazyIterator.emptyIterator();
    }

    VirtualFileImpl getVersion(VirtualFileImpl virtualFile, String versionId) throws ForbiddenException, NotFoundException {
        if (!virtualFile.isFile()) {
            throw new ForbiddenException("Versioning allowed for files only. ");
        }
        if ("0".equals(versionId)) {
            return virtualFile;
        }
        throw new NotFoundException("Version " + versionId + " for file " + virtualFile.getPath() + " doesn't exist. ");
    }


   /* ==================================== */

    LazyIterator<Pair<String, String>> countMd5Sums(VirtualFileImpl virtualFile) throws ServerException {
        if (!virtualFile.isFolder()) {
            return LazyIterator.emptyIterator();
        }
        final List<Pair<String, String>> hashes = new ArrayList<>();
        final int trimPathLength = virtualFile.getPath().length() + 1;
        final HashFunction hashFunction = Hashing.md5();
        final ValueHolder<ServerException> errorHolder = new ValueHolder<>();
        virtualFile.accept(new VirtualFileVisitor() {
            @Override
            public void visit(final VirtualFile virtualFile) {
                try {
                    if (virtualFile.isFile()) {
                        hashes.add(Pair.of(countHashSum(virtualFile, hashFunction), virtualFile.getPath().substring(trimPathLength)));
                    } else {
                        final LazyIterator<VirtualFile> children = virtualFile.getChildren(VirtualFileFilter.ALL);
                        while (children.hasNext()) {
                            children.next().accept(this);
                        }
                    }
                } catch (ServerException e) {
                    errorHolder.set(e);
                }
            }
        });
        return LazyIterator.fromList(hashes);
    }


    private String countHashSum(VirtualFile virtualFile, HashFunction hashFunction) throws ServerException {
        final PathLockFactory.PathLock lock = pathLockFactory.getLock(virtualFile.getVirtualFilePath(), false).acquire(LOCK_FILE_TIMEOUT);
        try (InputStream contentStream = virtualFile.getContent().getStream()) {
            return ByteSource.wrap(ByteStreams.toByteArray(contentStream)).hash(hashFunction).toString();
        } catch (ForbiddenException e) {
            throw new ServerException(e.getServiceError());
        } catch (IOException e) {
            throw new ServerException(e);
        } finally {
            lock.release();
        }
    }

   /* ============ HELPERS  ============ */

    /* Relative system path */
    private String toIoPath(Path vfsPath) {
        if (vfsPath.isRoot()) {
            return "";
        }
        if ('/' == java.io.File.separatorChar) {
            // Unix like system. Use vfs path as relative i/o path.
            return vfsPath.toString();
        }
        return vfsPath.join(java.io.File.separatorChar);
    }

    private Map<String, String[]> copyMetadataMap(Map<String, String[]> source) {
        final Map<String, String[]> copyMap = new HashMap<>(source.size());
        for (Map.Entry<String, String[]> e : source.entrySet()) {
            String[] value = e.getValue();
            String[] copyValue = new String[value.length];
            System.arraycopy(value, 0, copyValue, 0, value.length);
            copyMap.put(e.getKey(), copyValue);
        }
        return copyMap;
    }


    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }


    private void checkName(String name) throws ServerException {
        if (name == null || name.trim().isEmpty()) {
            throw new ServerException("Item's name is not set. ");
        }
    }
}
