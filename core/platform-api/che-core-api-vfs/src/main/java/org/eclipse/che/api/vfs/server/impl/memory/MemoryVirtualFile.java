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
package org.eclipse.che.api.vfs.server.impl.memory;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.ContentTypeGuesser;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.LazyIterator;
import org.eclipse.che.api.vfs.server.MountPoint;
import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.VirtualFileFilter;
import org.eclipse.che.api.vfs.server.VirtualFileVisitor;
import org.eclipse.che.api.vfs.server.observation.CreateEvent;
import org.eclipse.che.api.vfs.server.observation.DeleteEvent;
import org.eclipse.che.api.vfs.server.observation.MoveEvent;
import org.eclipse.che.api.vfs.server.observation.RenameEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateACLEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateContentEvent;
import org.eclipse.che.api.vfs.server.observation.UpdatePropertiesEvent;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.server.util.NotClosableInputStream;
import org.eclipse.che.api.vfs.server.util.ZipContent;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Folder;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

    private static MemoryVirtualFile newFile(MemoryVirtualFile parent, String name, InputStream content)
            throws IOException {
        return new MemoryVirtualFile(parent, ObjectIdGenerator.generateId(), name, content);
    }

    private static MemoryVirtualFile newFile(MemoryVirtualFile parent, String name, byte[] content) {
        return new MemoryVirtualFile(parent, ObjectIdGenerator.generateId(), name, content);
    }

    private static MemoryVirtualFile newFolder(MemoryVirtualFile parent, String name) {
        return new MemoryVirtualFile(parent, ObjectIdGenerator.generateId(), name);
    }

    //

    private final boolean                   type;
    private final String                    id;
    private final Map<String, List<String>> properties;
    private final long                      creationDate;
    private final Map<String, VirtualFile>  children;
    private final MemoryMountPoint          mountPoint;

    private String                      name;
    private MemoryVirtualFile           parent;
    private Path                        path;
    private byte[]                      content;
    private long                        lastModificationDate;
    private LockHolder                  lock;
    private Map<Principal, Set<String>> permissionsMap;
    private boolean exists = true;

    // --- File ---
    private MemoryVirtualFile(MemoryVirtualFile parent, String id, String name, InputStream content)
            throws IOException {
        this(parent, id, name, content == null ? null : ByteStreams.toByteArray(content));
    }

    private MemoryVirtualFile(MemoryVirtualFile parent, String id, String name, byte[] content) {
        this.mountPoint = (MemoryMountPoint)parent.getMountPoint();
        this.parent = parent;
        this.type = FILE;
        this.id = id;
        this.name = name;
        this.permissionsMap = new HashMap<>();
        this.properties = new HashMap<>();
        this.creationDate = this.lastModificationDate = System.currentTimeMillis();
        this.content = content == null ? new byte[0] : content;
        children = Collections.emptyMap();
    }

    // -- Folder ---
    private MemoryVirtualFile(MemoryVirtualFile parent, String id, String name) {
        this.mountPoint = (MemoryMountPoint)parent.getMountPoint();
        this.parent = parent;
        this.type = FOLDER;
        this.id = id;
        this.name = name;
        this.permissionsMap = new HashMap<>();
        this.properties = new HashMap<>();
        this.creationDate = this.lastModificationDate = System.currentTimeMillis();
        children = new HashMap<>();
    }

    /* root folder */ MemoryVirtualFile(MountPoint mountPoint) {
        this.mountPoint = (MemoryMountPoint)mountPoint;
        this.type = FOLDER;
        this.id = ObjectIdGenerator.generateId();
        this.name = "";
        this.permissionsMap = new HashMap<>();
        final Principal groupPrincipal = DtoFactory.getInstance().createDto(Principal.class);
        groupPrincipal.setName("workspace/developer");
        groupPrincipal.setType(Principal.Type.GROUP);
        final Principal anyPrincipal = DtoFactory.getInstance().createDto(Principal.class)
                                                 .withName(VirtualFileSystemInfo.ANY_PRINCIPAL).withType(Principal.Type.USER);

        final Set<String> groupPermissions = new HashSet<>(4);
        groupPermissions.add(BasicPermissions.ALL.value());
        this.permissionsMap.put(groupPrincipal, groupPermissions);
        final Set<String> anyPermissions = new HashSet<>(4);
        anyPermissions.add(BasicPermissions.READ.value());
        this.permissionsMap.put(anyPrincipal, anyPermissions);
        this.properties = new HashMap<>();
        this.creationDate = this.lastModificationDate = System.currentTimeMillis();
        children = new HashMap<>();
    }

    @Override
    public String getId() {
        checkExist();
        return id;
    }

    @Override
    public String getVersionId() {
        checkExist();
        return isFile() ? "0" : null;
    }

    @Override
    public String getName() {
        checkExist();
        return name;
    }

    public String getPath() {
        return getVirtualFilePath().toString();
    }

    @Override
    public Path getVirtualFilePath() {
        checkExist();
        MemoryVirtualFile parent = this.parent;
        if (parent == null) {
            return Path.ROOT; // for root folder
        }
        // Reuse if already cached
        if (path != null) {
            return path;
        }
        // Calculate based on parent path and cache the result
        Path parentPath = parent.getVirtualFilePath();
        path = parentPath.newPath(getName());
        return path;
    }

    @Override
    public boolean isFile() {
        checkExist();
        return type == FILE;
    }

    @Override
    public boolean isFolder() {
        checkExist();
        return type == FOLDER;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public boolean isRoot() {
        checkExist();
        return parent == null;
    }

    public long getCreationDate() {
        checkExist();
        return creationDate;
    }

    public long getLastModificationDate() {
        checkExist();
        return lastModificationDate;
    }

    public VirtualFile getParent() {
        checkExist();
        return parent;
    }

    public String getMediaType() {
        checkExist();
        String mediaType = getPropertyValue("vfs:mimeType");
        if (mediaType == null) {
            mediaType = isFile() ? ContentTypeGuesser.guessContentType(getName()) : Folder.FOLDER_MIME_TYPE;
        }
        return mediaType;
    }

    @Override
    public VirtualFile setMediaType(String mediaType) {
        checkExist();
        if (mediaType == null) {
            properties.remove("vfs:mimeType");
        } else {
            properties.put("vfs:mimeType", Arrays.asList(mediaType));
        }
        return this;
    }

    public VirtualFile updateACL(List<AccessControlEntry> acl, boolean override, String lockToken) throws ForbiddenException {
        checkExist();
        if (!hasPermission(BasicPermissions.UPDATE_ACL.value(), true)) {
            throw new ForbiddenException(String.format("Unable update ACL for '%s'. Operation not permitted. ", getPath()));
        }
        if (isFile() && !validateLockTokenIfLocked(lockToken)) {
            throw new ForbiddenException(String.format("Unable update ACL of item '%s'. Item is locked. ", getPath()));
        }
        if (acl.isEmpty() && !override) {
            return this;
        }
        final Map<Principal, Set<String>> update = override ? new HashMap<Principal, Set<String>>(acl.size()) : getPermissions();
        for (AccessControlEntry ace : acl) {
            final Principal principal = ace.getPrincipal();
            // Do not use 'transport' object directly, copy it instead.
            final Principal copyPrincipal = DtoFactory.getInstance().clone(principal);
            final List<String> acePermissions = ace.getPermissions();
            if (acePermissions == null || acePermissions.isEmpty()) {
                update.remove(copyPrincipal);
            } else {
                Set<String> permissions = update.get(copyPrincipal);
                if (permissions == null) {
                    update.put(copyPrincipal, permissions = new HashSet<>(4));
                } else {
                    permissions.clear();
                }
                permissions.addAll(acePermissions);
            }
        }

        permissionsMap = update;
        lastModificationDate = System.currentTimeMillis();
        mountPoint.getEventService().publish(new UpdateACLEvent(mountPoint.getWorkspaceId(), getPath(), isFolder()));
        return this;
    }

    @Override
    public LazyIterator<VirtualFile> getVersions(VirtualFileFilter filter) throws ForbiddenException {
        checkExist();
        if (!isFile()) {
            throw new ForbiddenException("Versioning allowed for files only. ");
        }
        if (filter.accept(this)) {
            return LazyIterator.<VirtualFile>singletonIterator(this);
        }
        return LazyIterator.emptyIterator();
    }

    @Override
    public VirtualFile getVersion(String versionId) throws ForbiddenException, ServerException {
        checkExist();
        if (!isFile()) {
            throw new ForbiddenException("Versioning allowed for files only. ");
        }
        if ("0".equals(versionId)) {
            return this;
        }
        throw new ServerException("Versioning is not supported. ");
    }

    @Override
    public Map<Principal, Set<String>> getPermissions() {
        checkExist();
        final Map<Principal, Set<String>> copy = new HashMap<>(permissionsMap.size());
        for (Map.Entry<Principal, Set<String>> e : permissionsMap.entrySet()) {
            final Principal copyPrincipal = DtoFactory.getInstance().clone(e.getKey());
            copy.put(copyPrincipal, new LinkedHashSet<>(e.getValue()));
        }
        return copy;
    }

    @Override
    public List<AccessControlEntry> getACL() {
        checkExist();
        final Map<Principal, Set<String>> permissions = getPermissions();
        final List<AccessControlEntry> acl = new ArrayList<>(permissions.size());
        for (Map.Entry<Principal, Set<String>> e : permissions.entrySet()) {
            final Set<String> basicPermissions = e.getValue();
            final Principal principal = e.getKey();
            final List<String> plainPermissions = new ArrayList<>(basicPermissions);
            // principal is already copied in method getPermissions
            acl.add(DtoFactory.getInstance().createDto(AccessControlEntry.class)
                              .withPrincipal(principal).withPermissions(plainPermissions));
        }
        return acl;
    }

    public List<Property> getProperties(PropertyFilter filter) {
        checkExist();
        final List<Property> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : properties.entrySet()) {
            final String name = e.getKey();
            if (filter.accept(name)) {
                final List<String> value = e.getValue();
                final Property property = DtoFactory.getInstance().createDto(Property.class).withName(name);
                if (value != null) {
                    property.setValue(new ArrayList<>(value));
                }
                result.add(property);
            }
        }
        return result;
    }

    public VirtualFile updateProperties(List<Property> update, String lockToken) throws ForbiddenException {
        checkExist();
        if (!hasPermission(BasicPermissions.UPDATE_ACL.value(), true)) {
            throw new ForbiddenException(String.format("Unable update properties for '%s'. Operation not permitted. ", getPath()));
        }
        if (isFile() && !validateLockTokenIfLocked(lockToken)) {
            throw new ForbiddenException(String.format("Unable update properties of item '%s'. Item is locked. ", getPath()));
        }
        for (Property p : update) {
            String name = p.getName();
            List<String> value = p.getValue();
            if (value != null) {
                List<String> copy = new ArrayList<>(value);
                properties.put(name, copy);
            } else {
                properties.remove(name);
            }
        }
        lastModificationDate = System.currentTimeMillis();
        mountPoint.getEventService().publish(new UpdatePropertiesEvent(mountPoint.getWorkspaceId(), getPath(), isFolder()));
        return this;
    }

    public void accept(VirtualFileVisitor visitor) throws ServerException {
        checkExist();
        visitor.visit(this);
    }

    @Override
    public LazyIterator<Pair<String, String>> countMd5Sums() throws ServerException {
        checkExist();
        if (isFile()) {
            return LazyIterator.emptyIterator();
        }

        final List<Pair<String, String>> hashes = new ArrayList<>();
        final int trimPathLength = getPath().length() + 1;
        final HashFunction hashFunction = Hashing.md5();
        final ValueHolder<ServerException> errorHolder = new ValueHolder<>();
        accept(new VirtualFileVisitor() {
            @Override
            public void visit(final VirtualFile virtualFile) {
                try {
                    if (virtualFile.isFile()) {
                        try (InputStream stream = virtualFile.getContent().getStream()) {
                            final String hexHash = ByteSource.wrap(ByteStreams.toByteArray(stream)).hash(hashFunction).toString();
                            hashes.add(Pair.of(hexHash, virtualFile.getPath().substring(trimPathLength)));
                        } catch (ForbiddenException e) {
                            throw new ServerException(e.getServiceError());
                        } catch (IOException e) {
                            throw new ServerException(e);
                        }
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
        final ServerException error = errorHolder.get();
        if (error != null) {
            throw error;
        }
        return LazyIterator.fromList(hashes);
    }

    @Override
    public LazyIterator<VirtualFile> getChildren(VirtualFileFilter filter) {
        checkExist();
        if (isFile()) {
            return LazyIterator.emptyIterator();
        }

        if (isRoot()) {
            // NOTE: We do not check read permissions when access to ROOT folder.
            if (!hasPermission(BasicPermissions.READ.value(), false)) {
                // User has not access to ROOT folder.
                return LazyIterator.emptyIterator();
            }
        }

        List<VirtualFile> children = doGetChildren(this);
        for (Iterator<VirtualFile> i = children.iterator(); i.hasNext(); ) {
            VirtualFile virtualFile = i.next();
            if (!((MemoryVirtualFile)virtualFile).hasPermission(BasicPermissions.READ.value(), false) || !filter.accept(virtualFile)) {
                i.remove();
            }
        }
        Collections.sort(children);
        return LazyIterator.fromList(children);
    }

    private List<VirtualFile> doGetChildren(VirtualFile folder) {
        return new ArrayList<>(((MemoryVirtualFile)folder).children.values());
    }

    @Override
    public VirtualFile getChild(String path) throws ForbiddenException {
        checkExist();
        String[] elements = Path.fromString(path).elements();
        MemoryVirtualFile child = (MemoryVirtualFile)children.get(elements[0]);
        if (child != null && elements.length > 1) {
            for (int i = 1, l = elements.length; i < l && child != null; i++) {
                if (child.isFolder()) {
                    child = (MemoryVirtualFile)child.getChild(elements[i]);
                }
            }
        }
        if (child != null) {
            if (mountPoint.acceptPath(child.getVirtualFilePath())) {
                // Don't check permissions for file "misc.xml" in folder ".codenvy". Dirty huck :( but seems simplest solution for now.
                // Need to work with 'misc.xml' independently to user.
                if (!child.hasPermission(BasicPermissions.READ.value(), false)) {
                    throw new ForbiddenException(String.format("We were unable to get an item '%s'.  " +
                                                               "You do not have the correct permissions to complete this operation. ",
                                                               getPath()));
                }
            }
            return child;
        }
        return null;
    }

    private boolean addChild(VirtualFile child) {
        checkExist();
        final String childName = child.getName();
        if (children.get(childName) == null) {
            children.put(childName, child);
            return true;
        }
        return false;
    }

    @Override
    public ContentStream getContent() throws ForbiddenException {
        checkExist();
        if (!isFile()) {
            throw new ForbiddenException(String.format("We were unable to retrieve the content. Item '%s' is not a file. ", getPath()));
        }
        if (content == null) {
            content = new byte[0];
        }
        return new ContentStream(getName(), new ByteArrayInputStream(content), getMediaType(), content.length,
                                 new Date(lastModificationDate));
    }

    @Override
    public VirtualFile updateContent(InputStream content, String lockToken) throws ForbiddenException, ServerException {
        return updateContent(null, content, lockToken, false);
    }

    private VirtualFile updateContent(String mediaType, InputStream content, String lockToken, boolean updateMediaType)
            throws ForbiddenException, ServerException {
        checkExist();

        if (!isFile()) {
            throw new ForbiddenException(String.format("We were unable to update the content. Item '%s' is not a file. ", getPath()));
        }
        if (mountPoint.acceptPath(getVirtualFilePath())) {
            // Don't check permissions when update file ".codenvy/misc.xml". Dirty huck :( but seems simplest solution for now.
            // Need to work with 'misc.xml' independently to user.
            if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
                throw new ForbiddenException(String.format("We were unable to update item '%s'." +
                                                           " You do not have the correct permissions to complete this operation.",
                                                           getPath()));
            }
        }
        if (isFile() && !validateLockTokenIfLocked(lockToken)) {
            throw new ForbiddenException(
                    String.format("We were unable to update the content of file '%s'. The file is locked. ", getPath()));
        }

        try {
            this.content = ByteStreams.toByteArray(content);
        } catch (IOException e) {
            throw new ServerException(String.format("We were unable to set the content of '%s'. ", getPath()));
        }

        if (updateMediaType) {
            setMediaType(mediaType);
        }

        SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(mountPoint, true).update(this);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        lastModificationDate = System.currentTimeMillis();
        mountPoint.getEventService().publish(new UpdateContentEvent(mountPoint.getWorkspaceId(), getPath()));
        return this;
    }

    @Override
    public long getLength() {
        checkExist();
        if (!isFile()) {
            return 0;
        }
        return content.length;
    }

    @Override
    public String getPropertyValue(String name) {
        checkExist();
        List<Property> properties = getProperties(PropertyFilter.valueOf(name));
        if (properties.size() > 0) {
            List<String> values = properties.get(0).getValue();
            if (!(values == null || values.isEmpty())) {
                return values.get(0);
            }
        }
        return null;
    }

    @Override
    public String[] getPropertyValues(String name) {
        checkExist();
        List<Property> properties = getProperties(PropertyFilter.valueOf(name));
        if (properties.size() > 0) {
            List<String> values = properties.get(0).getValue();
            if (!(values == null || values.isEmpty())) {
                return values.toArray(new String[values.size()]);
            }
        }
        return new String[0];
    }

    @Override
    public VirtualFile copyTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException {
        return copyTo(parent, null, false);
    }

    @Override
    public VirtualFile copyTo(VirtualFile parent, String name, boolean overWrite) throws ForbiddenException, ConflictException, ServerException {
        checkExist();
        MemoryVirtualFile theParent = ((MemoryVirtualFile) parent);
        theParent.checkExist();
        // setting copy name accordingly
        String nameToCopy = ("".equals(String.valueOf(name).trim()) || null == name) ? this.getName() : name;

        if (isRoot()) {
            throw new ServerException("Unable copy root folder. ");
        }
        if (!parent.isFolder()) {
            throw new ForbiddenException(String.format("Unable create copy of '%s'. Item '%s' specified as parent is not a folder.",
                                                       getPath(), parent.getPath()));
        }
        if (!theParent.hasPermission(BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable copy item '%s' to '%s'. Operation not permitted. ",
                                                       getPath(), parent.getPath()));
        }

        VirtualFile copy = doCopy(parent, nameToCopy, overWrite);
        mountPoint.putItem((MemoryVirtualFile) copy);
        SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(mountPoint, true).add(parent);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        mountPoint.getEventService().publish(new CreateEvent(mountPoint.getWorkspaceId(), copy.getPath(), copy.isFolder()));
        return copy;
    }

    private VirtualFile doCopy(VirtualFile parent) throws ConflictException {
        return doCopy(parent, null, false);
    }

    private VirtualFile doCopy(VirtualFile parent, String targetName, boolean overWrite) throws ConflictException {

        String nameToCopy = ("".equals(String.valueOf(targetName).trim()) || null == targetName) ? this.getName() : targetName;

        if (overWrite) {
            doOverWrite(parent, targetName);
        }

        VirtualFile virtualFile;
        if (isFile()) {
            virtualFile = newFile((MemoryVirtualFile) parent, nameToCopy, Arrays.copyOf(content, content.length));
        } else {
            virtualFile = newFolder((MemoryVirtualFile) parent, nameToCopy);
            LazyIterator<VirtualFile> children = getChildren(VirtualFileFilter.ALL);
            while (children.hasNext()) {
                ((MemoryVirtualFile)children.next()).doCopy(virtualFile);
            }
        }
        for (Map.Entry<String, List<String>> e : properties.entrySet()) {
            String name = e.getKey();
            List<String> value = e.getValue();
            if (value != null) {
                List<String> copy = new ArrayList<>(value);
                ((MemoryVirtualFile)virtualFile).properties.put(name, copy);
            }
        }
        if (!((MemoryVirtualFile)parent).addChild(virtualFile)) {
            throw new ConflictException(String.format("Item '%s' already exists. ", (parent.getPath() + '/' + name)));
        }
        return virtualFile;
    }

    private void doOverWrite(VirtualFile theParent, String targetName) {
        try {
            VirtualFile overWritenVirtualFile = theParent.getChild(targetName);
            boolean targetExists = (null != overWritenVirtualFile);
            /**
             * if a VirtualFile with same target name already exists under new
             * parent we need to determent if we should overwrite it or not.
             */
            if (targetExists) { // name collision
                String deleteToken = null;
                if (isFile()) {
                    deleteToken = overWritenVirtualFile.lock(0);
                }
                overWritenVirtualFile.delete(deleteToken);
            }
        } catch (ForbiddenException | ServerException | ConflictException ex) {
            LOG.error(ex.getMessage());
        }
    }

    @Override
    public VirtualFile moveTo(VirtualFile parent, final String lockToken) throws ConflictException, ForbiddenException, ServerException {
        return moveTo(parent, null, false, lockToken);
    }

    @Override
    public VirtualFile moveTo(VirtualFile parent, String newName, boolean overWrite, String lockToken) throws ForbiddenException, ConflictException, ServerException {
        checkExist();
        ((MemoryVirtualFile)parent).checkExist();
        boolean isFile = isFile();

        // the name set to destination after moving
        String destinationName = ("".equals(String.valueOf(newName).trim()) || null == newName) ? this.getName() : newName;

        if (isRoot()) {
            throw new ForbiddenException("Unable move root folder. ");
        }
        final String myPath = getPath();
        final String newParentPath = parent.getPath();
        if (!parent.isFolder()) {
            throw new ForbiddenException("Unable move item. Item specified as parent is not a folder. ");
        }
        if (!(((MemoryVirtualFile)parent).hasPermission(BasicPermissions.WRITE.value(), true)
              && hasPermission(BasicPermissions.WRITE.value(), true))) {
            throw new ForbiddenException(String.format("Unable move item '%s' to %s. Operation not permitted. ", myPath, newParentPath));
        }

        final boolean folder = isFolder();
        if (folder) {
            // Be sure destination folder is not child (direct or not) of moved item.
            if (newParentPath.startsWith(myPath)) {
                throw new ForbiddenException(
                        String.format("Unable move item %s to %s. Item may not have itself as parent. ", myPath, newParentPath));
            }
            final ValueHolder<Exception> errorHolder = new ValueHolder<>();
            accept(new VirtualFileVisitor() {
                @Override
                public void visit(VirtualFile virtualFile) {
                    try {
                        if (virtualFile.isFolder()) {
                            for (VirtualFile childVirtualFile : doGetChildren(virtualFile)) {
                                childVirtualFile.accept(this);
                            }
                        }
                        if (!((MemoryVirtualFile)virtualFile).hasPermission(BasicPermissions.WRITE.value(), false)) {
                            throw new ForbiddenException(
                                    String.format("Unable move item '%s'. Operation not permitted. ", virtualFile.getPath()));
                        }
                        if (virtualFile.isFile() && virtualFile.isLocked()) {
                            throw new ForbiddenException(
                                    String.format("Unable move item '%s'. Child item '%s' is locked. ", name, virtualFile.getPath()));
                        }
                    } catch (ServerException | ForbiddenException e) {
                        errorHolder.set(e);
                    }
                }
            });
            final Exception error = errorHolder.get();
            if (error != null) {
                if (error instanceof ForbiddenException) {
                    throw (ForbiddenException)error;
                } else if (error instanceof ServerException) {
                    throw (ServerException)error;
                } else {
                    throw new ServerException(error.getMessage(), error);
                }
            }
        } else {
            if (!validateLockTokenIfLocked(lockToken)) {
                throw new ForbiddenException(String.format("Unable move item %s. Item is locked. ", myPath));
            }
        }

        //====-overwriting-====
        if (overWrite) {
            doOverWrite(parent, destinationName);
        }
        //=====================

        /**
         * if newName was sent NOT null NOR empty String, then request was
         * intended to change the VirtualFile name after moving
         */
        if (!("".equals(String.valueOf(newName).trim()) || null == newName)) {
            if (((MemoryVirtualFile) parent).children.containsKey(destinationName)) {
                throw new ConflictException(String.format("Item '%s' already exists. ", (parent.getPath() + '/' + destinationName)));
            }
            this.parent.children.remove(getName());
            this.parent = (MemoryVirtualFile) parent;
            this.parent.children.put(destinationName, this);
            this.name = destinationName;
        } else { // default behavior is to move with current name
            if (!((MemoryVirtualFile) parent).addChild(this)) {
            throw new ConflictException(String.format("Item '%s' already exists. ", (parent.getPath() + '/' + name)));
        }
        this.parent.children.remove(getName());
            this.parent = (MemoryVirtualFile) parent;
        }
        this.path = null;
        // =======================

        SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(mountPoint, true).delete(myPath, isFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
            try {
                searcherProvider.getSearcher(mountPoint, true).add(parent);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        mountPoint.getEventService().publish(new MoveEvent(mountPoint.getWorkspaceId(), getPath(), myPath, folder));
        return this;
    }

    @Override
    public VirtualFile rename(String newName, String newMediaType, String lockToken)
            throws ForbiddenException, ConflictException, ServerException {
        checkExist();
        checkName(newName);
        boolean isFile = isFile();
        if (isRoot()) {
            throw new ForbiddenException("We were unable to rename a root folder.");
        }
        if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("We were unable to delete an item '%s'." +
                                                       " You do not have the correct permissions to complete this operation.", getPath()));
        }
        final String myPath = getPath();
        final boolean folder = isFolder();
        if (folder) {
            final ValueHolder<Exception> errorHolder = new ValueHolder<>();
            accept(new VirtualFileVisitor() {
                @Override
                public void visit(VirtualFile virtualFile) {
                    try {
                        if (virtualFile.isFolder()) {
                            for (VirtualFile childVirtualFile : doGetChildren(virtualFile)) {
                                childVirtualFile.accept(this);
                            }
                        }
                        if (!((MemoryVirtualFile)virtualFile).hasPermission(BasicPermissions.WRITE.value(), false)) {
                            throw new ForbiddenException(
                                    String.format("We were unable to rename an item '%s'." +
                                                  " You do not have the correct permissions to complete this operation.",
                                                  virtualFile.getPath()));
                        }
                        if (virtualFile.isFile() && virtualFile.isLocked()) {
                            throw new ForbiddenException(
                                    String.format("We were unable to rename an item '%s'." +
                                                  " The child item '%s' is currently locked by the system.", getPath(),
                                                  virtualFile.getPath()));
                        }
                    } catch (ServerException | ForbiddenException e) {
                        errorHolder.set(e);
                    }
                }
            });
            final Exception error = errorHolder.get();
            if (error != null) {
                if (error instanceof ForbiddenException) {
                    throw (ForbiddenException)error;
                } else if (error instanceof ServerException) {
                    throw (ServerException)error;
                } else {
                    throw new ServerException(error.getMessage(), error);
                }
            }
        } else {
            if (!validateLockTokenIfLocked(lockToken)) {
                throw new ForbiddenException(String.format("We were unable to rename an item '%s'." +
                                                           " The item is currently locked by the system.", getPath()));
            }
        }

        if (parent.getChild(newName) != null) {
            throw new ConflictException(String.format("Item '%s' already exists. ", newName));
        }
        parent.children.remove(name);
        parent.children.put(newName, this);
        name = newName;
        path = null;

        if (newMediaType != null) {
            setMediaType(newMediaType);
        }
        lastModificationDate = System.currentTimeMillis();
        SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(mountPoint, true).delete(myPath, isFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
            try {
                searcherProvider.getSearcher(mountPoint, true).add(parent);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        mountPoint.getEventService().publish(new RenameEvent(mountPoint.getWorkspaceId(), getPath(), myPath, folder));
        return this;
    }

    @Override
    public void delete(final String lockToken) throws ForbiddenException, ServerException {
        checkExist();
        boolean isFile = isFile();
        if (isRoot()) {
            throw new ForbiddenException("Unable delete root folder. ");
        }
        if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("We were unable to delete an item '%s'." +
                                                       " You do not have the correct permissions to complete this operation.", getPath()));
        }
        final String myPath = getPath();
        final boolean folder = isFolder();
        if (folder) {
            final ValueHolder<Exception> errorHolder = new ValueHolder<>();
            final List<VirtualFile> toDelete = new ArrayList<>();
            accept(new VirtualFileVisitor() {
                @Override
                public void visit(VirtualFile virtualFile) {
                    try {
                        if (virtualFile.isFolder()) {
                            for (VirtualFile childVirtualFile : doGetChildren(virtualFile)) {
                                childVirtualFile.accept(this);
                            }
                        }
                        if (!((MemoryVirtualFile)virtualFile).hasPermission(BasicPermissions.WRITE.value(), false)) {
                            throw new ForbiddenException(
                                    String.format("We were unable to delete an item '%s'." +
                                                  " You do not have the correct permissions to complete this operation.",
                                                  virtualFile.getPath()));
                        }

                        if (virtualFile.isFile() && virtualFile.isLocked()) {
                            throw new ForbiddenException(String.format("Unable delete item '%s'. Child item '%s' is locked. ",
                                                                       getPath(), virtualFile.getPath()));
                        }
                        toDelete.add(virtualFile);
                    } catch (ServerException | ForbiddenException e) {
                        errorHolder.set(e);
                    }
                }
            });
            final Exception error = errorHolder.get();
            if (error != null) {
                if (error instanceof ForbiddenException) {
                    throw (ForbiddenException)error;
                } else if (error instanceof ServerException) {
                    throw (ServerException)error;
                } else {
                    throw new ServerException(error.getMessage(), error);
                }
            }
            for (VirtualFile virtualFile : toDelete) {
                mountPoint.deleteItem(virtualFile.getId());
                ((MemoryVirtualFile)virtualFile).exists = false;
            }
        } else {
            if (!validateLockTokenIfLocked(lockToken)) {
                throw new ForbiddenException(String.format("Unable delete item '%s'. Item is locked. ", getPath()));
            }
            mountPoint.deleteItem(getId());
        }
        parent.children.remove(name);
        exists = false;
        parent = null;
        path = null;
        SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(mountPoint, true).delete(myPath, isFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        mountPoint.getEventService().publish(new DeleteEvent(mountPoint.getWorkspaceId(), myPath, folder));
    }

    @Override
    public ContentStream zip(VirtualFileFilter filter) throws ForbiddenException, ServerException {
        checkExist();
        if (!isFolder()) {
            throw new ForbiddenException(String.format("Unable export to zip. Item '%s' is not a folder. ", getPath()));
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            final ZipOutputStream zipOut = new ZipOutputStream(out);
            final LinkedList<VirtualFile> q = new LinkedList<>();
            q.add(this);
            final int rootZipPathLength = isRoot() ? 1 : (getPath().length() + 1);
            while (!q.isEmpty()) {
                final LazyIterator<VirtualFile> children = q.pop().getChildren(filter);
                while (children.hasNext()) {
                    VirtualFile current = children.next();
                    final String zipEntryName = current.getPath().substring(rootZipPathLength);
                    if (current.isFile()) {
                        final ZipEntry zipEntry = new ZipEntry(zipEntryName);
                        zipEntry.setTime(current.getLastModificationDate());
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(((MemoryVirtualFile)current).content);
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
            zipOut.close();
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
        final byte[] zipContent = out.toByteArray();
        return new ContentStream(getName() + ".zip", new ByteArrayInputStream(zipContent), ExtMediaType.APPLICATION_ZIP, zipContent.length,
                                 new Date());
    }

    @Override
    public void unzip(InputStream zipped, boolean overwrite, int stripNumber) throws ForbiddenException, ServerException {
        checkExist();
        if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("We were unable to import a ZIP file to '%s' as part of the import." +
                                                       " You do not have the correct permissions to complete this operation.", getPath()));
        }

        ZipInputStream zip = null;
        try {
            final ZipContent zipContent = ZipContent.newInstance(zipped);
            zip = new ZipInputStream(zipContent.zippedData);
            // Wrap zip stream to prevent close it. We can pass stream to other method and it can read content of current
            // ZipEntry but not able to close original stream of ZIPed data.
            InputStream noCloseZip = new NotClosableInputStream(zip);
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                VirtualFile current = this;
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
                    for (int i = 0, stop = relPath.length() - 1; i < stop; i++) {
                        MemoryVirtualFile folder = newFolder((MemoryVirtualFile)current, relPath.element(i));
                        if (((MemoryVirtualFile)current).addChild(folder)) {
                            current = folder;
                            mountPoint.putItem(folder);
                        } else {
                            current = current.getChild(relPath.element(i));
                        }
                    }
                }
                if (zipEntry.isDirectory()) {
                    if (current.getChild(name) == null) {
                        MemoryVirtualFile folder = newFolder((MemoryVirtualFile)current, name);
                        ((MemoryVirtualFile)current).addChild(folder);
                        mountPoint.putItem(folder);
                        mountPoint.getEventService().publish(new CreateEvent(mountPoint.getWorkspaceId(), folder.getPath(), true));
                    }
                } else {
                    current.getChild(name);
                    VirtualFile file = current.getChild(name);
                    if (file != null) {
                        if (file.isLocked()) {
                            throw new ForbiddenException(String.format("File '%s' already exists and locked. ", file.getPath()));
                        }
                        if (!((MemoryVirtualFile)file).hasPermission(BasicPermissions.WRITE.value(), true)) {
                            throw new ForbiddenException(
                                    String.format("We were unable to update file '%s' as part of the import." +
                                                  " You do not have the correct permissions to complete this operation.", file.getPath()));
                        }
                        if (!overwrite) {
                            throw new ForbiddenException(String.format("File '%s' already exists. ", file.getPath()));
                        }
                        file.updateContent(noCloseZip, null);
                        mountPoint.getEventService().publish(new UpdateContentEvent(mountPoint.getWorkspaceId(), file.getPath()));
                    } else {
                        file = newFile((MemoryVirtualFile)current, name, noCloseZip);
                        ((MemoryVirtualFile)current).addChild(file);
                        mountPoint.putItem((MemoryVirtualFile)file);
                        mountPoint.getEventService().publish(new CreateEvent(mountPoint.getWorkspaceId(), file.getPath(), false));
                    }
                }
                zip.closeEntry();
            }
            SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
            if (searcherProvider != null) {
                try {
                    searcherProvider.getSearcher(mountPoint, true).add(this);
                } catch (ServerException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public String lock(long timeout) throws ForbiddenException, ConflictException {
        checkExist();
        if (!isFile()) {
            throw new ForbiddenException(String.format("Unable lock '%s'. Locking allowed for files only. ", getPath()));
        }

        if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("Unable lock '%s'. Operation not permitted. ", getPath()));
        }
        final String lockToken = NameGenerator.generate(null, 32);
        final LockHolder lock = new LockHolder(lockToken, timeout);
        if (this.lock != null) {
            throw new ConflictException("File already locked. ");
        }
        this.lock = lock;
        lastModificationDate = System.currentTimeMillis();
        return lockToken;
    }

    @Override
    public VirtualFile unlock(String lockToken) throws ForbiddenException, ConflictException {
        checkExist();
        if (!isFile()) {
            throw new ForbiddenException(String.format("Unable unlock '%s'. Locking allowed for files only. ", getPath()));
        }
        final LockHolder myLock = lock;
        if (myLock == null) {
            throw new ConflictException("File is not locked. ");
        } else if (myLock.expired < System.currentTimeMillis()) {
            lock = null;
            throw new ConflictException("File is not locked. ");
        }
        if (myLock.lockToken.equals(lockToken)) {
            lock = null;
            lastModificationDate = System.currentTimeMillis();
        } else {
            throw new ForbiddenException("Unable remove lock from file. Lock token does not match. ");
        }
        lastModificationDate = System.currentTimeMillis();
        return this;
    }

    @Override
    public boolean isLocked() {
        checkExist();
        final LockHolder myLock = lock;
        if (lock != null) {
            if (myLock.expired < System.currentTimeMillis()) {
                // replace lock
                lock = null;
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public VirtualFile createFile(String name, InputStream content)
            throws ForbiddenException, ConflictException, ServerException {
        checkExist();
        checkName(name);
        if (!isFolder()) {
            throw new ForbiddenException("Unable create new file. Item specified as parent is not a folder. ");
        }
        if (mountPoint.acceptPath(getVirtualFilePath().newPath(name))) {
            // Don't check permissions when create file "misc.xml" in folder ".codenvy". Dirty huck :( but seems simplest solution for now.
            // Need to work with 'misc.xml' independently to user.
            if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
                throw new ForbiddenException(String.format("Unable create new file in '%s'. Operation not permitted. ", getPath()));
            }
        }
        final MemoryVirtualFile newFile;
        try {
            newFile = newFile(this, name, content);
        } catch (IOException e) {
            throw new ServerException(String.format("Unable set content of '%s'. ", getPath() + e.getMessage()));
        }
        if (!addChild(newFile)) {
            throw new ConflictException(String.format("Item with the name '%s' already exists. ", name));
        }
        mountPoint.putItem(newFile);
        SearcherProvider searcherProvider = mountPoint.getSearcherProvider();
        if (searcherProvider != null) {
            try {
                searcherProvider.getSearcher(mountPoint, true).add(newFile);
            } catch (ServerException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        mountPoint.getEventService().publish(new CreateEvent(mountPoint.getWorkspaceId(), newFile.getPath(), false));
        return newFile;
    }

    @Override
    public VirtualFile createFolder(String name) throws ForbiddenException, ConflictException, ServerException {
        checkExist();
        checkName(name);
        if (!isFolder()) {
            throw new ForbiddenException("Unable create new folder. Item specified as parent is not a folder. ");
        }
        if (!hasPermission(BasicPermissions.WRITE.value(), true)) {
            throw new ForbiddenException(String.format("We were unable to create a new folder in '%s' as part of the import. " +
                                                       "You do not have the correct permissions to complete this operation. ", getPath()));
        }
        MemoryVirtualFile newFolder = null;
        MemoryVirtualFile current = this;
        if (name.indexOf('/') > 0) {
            final Path internPath = Path.fromString(name);
            for (String element : internPath.elements()) {
                MemoryVirtualFile folder = newFolder(current, element);
                if (current.addChild(folder)) {
                    newFolder = folder;
                    current = folder;
                } else {
                    current = (MemoryVirtualFile)current.getChild(element);
                }
            }
            if (newFolder == null) {
                // Folder or folder hierarchy already exists.
                throw new ConflictException(String.format("Item with the name '%s' already exists. ", name));
            }
        } else {
            newFolder = newFolder(this, name);
            if (!addChild(newFolder)) {
                throw new ConflictException(String.format("Item with the name '%s' already exists. ", name));
            }
        }
        mountPoint.putItem(newFolder);
        mountPoint.getEventService().publish(new CreateEvent(mountPoint.getWorkspaceId(), newFolder.getPath(), true));
        return newFolder;
    }

    @Override
    public MountPoint getMountPoint() {
        return mountPoint;
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
    public File getIoFile() {
        throw new UnsupportedOperationException("Not supported in Memory implementation");
    }

    boolean hasPermission(String permission, boolean checkParent) {
        checkExist();
        return true;
    }

    private void checkExist() {
        if (!exists) {
            throw new RuntimeException(String.format("Item '%s' already removed. ", name));
        }
    }

    private void checkName(String name) throws ServerException {
        if (name == null || name.trim().isEmpty()) {
            throw new ServerException("Item's name is not set. ");
        }
    }

    private boolean validateLockTokenIfLocked(String lockToken) {
        if (!isLocked()) {
            return true;
        }
        final LockHolder myLock = lock;
        return myLock == null || myLock.lockToken.equals(lockToken);
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
