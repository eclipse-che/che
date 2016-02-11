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
package org.eclipse.che.api.vfs.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.search.QueryExpression;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.eclipse.che.api.vfs.server.util.LinksHelper;
import org.eclipse.che.api.vfs.shared.ItemType;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.File;
import org.eclipse.che.api.vfs.shared.dto.Folder;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.api.vfs.shared.dto.ItemNode;
import org.eclipse.che.api.vfs.shared.dto.Lock;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.api.vfs.shared.dto.ReplacementSet;
import org.eclipse.che.api.vfs.shared.dto.Variable;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.ACLCapability;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;
import org.eclipse.che.commons.lang.Deserializer;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.apache.commons.fileupload.FileItem;
import org.everrest.core.impl.provider.multipart.OutputItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Base implementation of VirtualFileSystem.
 *
 * @author andrew00x
 */
public abstract class VirtualFileSystemImpl implements VirtualFileSystem {
    private static final Logger LOG = LoggerFactory.getLogger(VirtualFileSystemImpl.class);

    protected final String                       vfsId;
    protected final URI                          baseUri;
    protected final VirtualFileSystemUserContext userContext;
    protected final MountPoint                   mountPoint;
    protected final SearcherProvider             searcherProvider;
    protected final VirtualFileSystemRegistry    vfsRegistry;

    public VirtualFileSystemImpl(String vfsId,
                                 URI baseUri,
                                 VirtualFileSystemUserContext userContext,
                                 MountPoint mountPoint,
                                 SearcherProvider searcherProvider,
                                 VirtualFileSystemRegistry vfsRegistry) {
        this.vfsId = vfsId;
        this.baseUri = baseUri;
        this.userContext = userContext;
        this.mountPoint = mountPoint;
        this.searcherProvider = searcherProvider;
        this.vfsRegistry = vfsRegistry;
    }

    @Override
    public MountPoint getMountPoint() {
        return mountPoint;
    }

    @Path("copy/{id}")
    @Override
    public Item copy(@PathParam("id") String id,
                     @QueryParam("parentId") String parentId,
                     @QueryParam("name") String newName)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFile virtualFileCopy =
                mountPoint.getVirtualFileById(id).copyTo(mountPoint.getVirtualFileById(parentId), newName, false);
        return fromVirtualFile(virtualFileCopy, false, PropertyFilter.ALL_FILTER);
    }

    @Path("clone")
    @Override
    @Deprecated
    public Item clone(@QueryParam("srcPath") String srcPath,
                      @QueryParam("srcVfsId") String srcVfsId,
                      @QueryParam("parentPath") String parentPath,
                      @QueryParam("name") String name) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFile item = vfsRegistry.getProvider(srcVfsId).getMountPoint(true).getVirtualFile(srcPath);
        final VirtualFile destination = mountPoint.getVirtualFile(parentPath);
        if (!destination.isFolder()) {
            throw new ForbiddenException("Unable to perform cloning. Item specified as parent is not a folder.");
        }
        return fromVirtualFile(clone(item, destination, name), false, PropertyFilter.ALL_FILTER);
    }

    public static VirtualFile clone(VirtualFile item, VirtualFile destination, String name)
            throws ForbiddenException, ConflictException, ServerException {
        if (item.isFile()) {
            InputStream input = null;
            try {
                input = item.getContent().getStream();
                return destination.createFile(name != null ? name : item.getName(), input);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        } else {
            final VirtualFile newFolder = destination.createFolder(name != null ? name : item.getName());
            final LazyIterator<VirtualFile> children = item.getChildren(VirtualFileFilter.ALL);
            while (children.hasNext()) {
                clone(children.next(), newFolder, null);
            }
            return newFolder;
        }
    }

    @Path("file/{parentId}")
    @Override
    public File createFile(@PathParam("parentId") String parentId,
                           @QueryParam("name") String name,
                           InputStream content) throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFile parent = mountPoint.getVirtualFileById(parentId);
        final VirtualFile newVirtualFile = parent.createFile(name, content);
        return (File)fromVirtualFile(newVirtualFile, false, PropertyFilter.ALL_FILTER);
    }

    @Path("folder/{parentId}")
    @Override
    public Folder createFolder(@PathParam("parentId") String parentId, @QueryParam("name") String name)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFile parent = mountPoint.getVirtualFileById(parentId);
        final VirtualFile newVirtualFile = parent.createFolder(name);
        return (Folder)fromVirtualFile(newVirtualFile, false, PropertyFilter.ALL_FILTER);
    }

    @Path("delete/{id}")
    @Override
    public void delete(@PathParam("id") String id, @QueryParam("lockToken") String lockToken)
            throws NotFoundException, ForbiddenException, ServerException {
        final VirtualFile virtualFile = mountPoint.getVirtualFileById(id);
        if (virtualFile.isRoot()) {
            throw new ForbiddenException("Unable delete root folder. ");
        }
        virtualFile.delete(lockToken);
    }

    @Path("acl/{id}")
    @Override
    public List<AccessControlEntry> getACL(@PathParam("id") String id) throws NotFoundException, ForbiddenException, ServerException {
        if (getInfo().getAclCapability() == ACLCapability.NONE) {
            throw new ServerException("ACL feature is not supported. ");
        }
        return mountPoint.getVirtualFileById(id).getACL();
    }

    @Path("children/{id}")
    @Override
    public ItemList getChildren(@PathParam("id") String folderId,
                                @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                                @QueryParam("skipCount") int skipCount,
                                @QueryParam("itemType") String itemType,
                                @DefaultValue("false") @QueryParam("includePermissions") Boolean includePermissions,
                                @DefaultValue(PropertyFilter.NONE) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if (skipCount < 0) {
            throw new ConflictException("'skipCount' parameter is negative. ");
        }

        final ItemType itemTypeType;
        if (itemType != null) {
            try {
                itemTypeType = ItemType.fromValue(itemType);
            } catch (IllegalArgumentException e) {
                throw new ForbiddenException(String.format("Unknown type: %s", itemType));
            }
        } else {
            itemTypeType = null;
        }

        final VirtualFile virtualFile = mountPoint.getVirtualFileById(folderId);

        if (!virtualFile.isFolder()) {
            throw new ForbiddenException(String.format("Unable get children. Item '%s' is not a folder. ", virtualFile.getPath()));
        }

        final VirtualFileFilter filter;
        if (itemTypeType == null) {
            filter = VirtualFileFilter.ALL;
        } else {
            filter = new VirtualFileFilter() {
                @Override
                public boolean accept(VirtualFile file) {
                    try {
                        return (itemTypeType == ItemType.FILE && file.isFile()) || (itemTypeType == ItemType.FOLDER && file.isFolder());
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            };
        }
        final LazyIterator<VirtualFile> children = virtualFile.getChildren(filter);
        try {
            if (skipCount > 0) {
                children.skip(skipCount);
            }
        } catch (NoSuchElementException nse) {
            throw new ConflictException("'skipCount' parameter is greater then total number of items. ");
        }

        final List<Item> items = new ArrayList<>();
        for (int count = 0; children.hasNext() && (maxItems < 0 || count < maxItems); count++) {
            items.add(fromVirtualFile(children.next(), includePermissions, propertyFilter));
        }
        return DtoFactory.getInstance().createDto(ItemList.class).withItems(items).withNumItems(children.size())
                         .withHasMoreItems(children.hasNext());
    }

    @Override
    public ItemList getChildren(String folderId, int maxItems, int skipCount, String itemType, boolean includePermissions)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        return getChildren(folderId, maxItems, skipCount, itemType, includePermissions, PropertyFilter.ALL_FILTER);
    }

    @Path("tree/{id}")
    @Override
    public ItemNode getTree(@PathParam("id") String folderId,
                            @DefaultValue("-1") @QueryParam("depth") int depth,
                            @DefaultValue("false") @QueryParam("includePermissions") Boolean includePermissions,
                            @DefaultValue(PropertyFilter.NONE) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ServerException {
        final VirtualFile virtualFile = mountPoint.getVirtualFileById(folderId);
        if (!virtualFile.isFolder()) {
            throw new ForbiddenException(String.format("Unable get tree. Item '%s' is not a folder. ", virtualFile.getPath()));
        }
        return DtoFactory.getInstance().createDto(ItemNode.class)
                         .withItem(fromVirtualFile(virtualFile, includePermissions, propertyFilter))
                         .withChildren(getTreeLevel(virtualFile, depth, includePermissions, propertyFilter));
    }

    @Override
    public ItemNode getTree(String folderId, int depth, boolean includePermissions)
            throws NotFoundException, ForbiddenException, ServerException {
        return getTree(folderId, depth, includePermissions, PropertyFilter.ALL_FILTER);
    }

    private List<ItemNode> getTreeLevel(VirtualFile virtualFile, int depth, boolean includePermissions, PropertyFilter propertyFilter)
            throws ServerException {
        if (depth == 0 || !virtualFile.isFolder()) {
            return null;
        }
        final LazyIterator<VirtualFile> children = virtualFile.getChildren(VirtualFileFilter.ALL);
        final List<ItemNode> level = new ArrayList<>(children.size());
        while (children.hasNext()) {
            final VirtualFile next = children.next();
            level.add(DtoFactory.getInstance().createDto(ItemNode.class)
                                .withItem(fromVirtualFile(next, includePermissions, propertyFilter))
                                .withChildren(getTreeLevel(next, depth - 1, includePermissions, propertyFilter)));
        }
        return level;
    }

    @Path("content/{id}")
    @Override
    public ContentStream getContent(@PathParam("id") String id) throws NotFoundException, ForbiddenException, ServerException {
        return mountPoint.getVirtualFileById(id).getContent();
    }

    @Path("contentbypath/{path:.*}")
    @Override
    public ContentStream getContent(@PathParam("path") String path, @QueryParam("versionId") String versionId)
            throws NotFoundException, ForbiddenException, ServerException {
        return mountPoint.getVirtualFile(path).getContent();
    }

    @Override
    public abstract VirtualFileSystemInfo getInfo() throws ServerException;

    @Path("item/{id}")
    @Override
    public Item getItem(@PathParam("id") String id,
                        @DefaultValue("false") @QueryParam("includePermissions") Boolean includePermissions,
                        @DefaultValue(PropertyFilter.ALL) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ServerException {
        return fromVirtualFile(mountPoint.getVirtualFileById(id), includePermissions, propertyFilter);
    }

    @Override
    public Item getItem(String id, boolean includePermissions) throws NotFoundException, ForbiddenException, ServerException {
        return getItem(id, includePermissions, PropertyFilter.ALL_FILTER);
    }

    @Path("itembypath/{path:.*}")
    @Override
    public Item getItemByPath(@PathParam("path") String path,
                              @QueryParam("versionId") String versionId,
                              @DefaultValue("false") @QueryParam("includePermissions") Boolean includePermissions,
                              @DefaultValue(PropertyFilter.ALL) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ServerException {
        VirtualFile virtualFile = mountPoint.getVirtualFile(path);
        if (virtualFile.isFile()) {
            if (versionId != null) {
                virtualFile = virtualFile.getVersion(versionId);
            }
        } else if (versionId != null) {
            throw new ForbiddenException(String.format("Object '%s' is not a file. Version ID must not be set. ", path));
        }

        return fromVirtualFile(virtualFile, includePermissions, propertyFilter);
    }

    @Override
    public Item getItemByPath(String path, String versionId, boolean includePermissions)
            throws NotFoundException, ForbiddenException, ServerException {
        return getItemByPath(path, versionId, includePermissions, PropertyFilter.ALL_FILTER);
    }

    @Path("version/{id}/{versionId}")
    @Override
    public ContentStream getVersion(@PathParam("id") String id, @PathParam("versionId") String versionId)
            throws NotFoundException, ForbiddenException, ServerException {
        return mountPoint.getVirtualFileById(id).getVersion(versionId).getContent();
    }

    @Path("version-history/{id}")
    @Override
    public ItemList getVersions(@PathParam("id") String id,
                                @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                                @QueryParam("skipCount") int skipCount,
                                @DefaultValue(PropertyFilter.ALL) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if (skipCount < 0) {
            throw new ConflictException("'skipCount' parameter is negative. ");
        }
        final VirtualFile virtualFile = mountPoint.getVirtualFileById(id);
        if (!virtualFile.isFile()) {
            throw new ForbiddenException(
                    String.format("Unable get versions of '%s'. Versioning allowed for files only. ", virtualFile.getPath()));
        }
        final LazyIterator<VirtualFile> versions = virtualFile.getVersions(VirtualFileFilter.ALL);
        try {
            if (skipCount > 0) {
                versions.skip(skipCount);
            }
        } catch (NoSuchElementException nse) {
            throw new ConflictException("'skipCount' parameter is greater then total number of items. ");
        }

        final List<Item> items = new ArrayList<>();
        for (int count = 0; versions.hasNext() && (maxItems < 0 || count < maxItems); count++) {
            items.add(fromVirtualFile(versions.next(), false, propertyFilter));
        }
        return DtoFactory.getInstance().createDto(ItemList.class).withItems(items).withNumItems(versions.size())
                         .withHasMoreItems(versions.hasNext());
    }

    @Override
    public ItemList getVersions(String id, int maxItems, int skipCount)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        return getVersions(id, maxItems, skipCount, PropertyFilter.ALL_FILTER);
    }

    @Path("lock/{id}")
    @Override
    public Lock lock(@PathParam("id") String id,
                     @DefaultValue("0") @QueryParam("timeout") long timeout)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if (!getInfo().isLockSupported()) {
            throw new ServerException("Locking is not supported. ");
        }
        final VirtualFileSystemUser user = userContext.getVirtualFileSystemUser();
        final VirtualFile virtualFile = mountPoint.getVirtualFileById(id);
        if (!virtualFile.isFile()) {
            throw new ForbiddenException(String.format("Unable lock '%s'. Locking allowed for files only. ", virtualFile.getPath()));
        }
        final String lockToken = mountPoint.getVirtualFileById(id).lock(timeout);
        return DtoFactory.getInstance().createDto(Lock.class).withLockToken(lockToken).withOwner(user.getUserId()).withTimeout(timeout);
    }

    @Path("move/{id}")
    @Override
    public Item move(@PathParam("id") String id,
                     @QueryParam("parentId") String parentId,
                     @QueryParam("name") String newName,
                     @QueryParam("lockToken") String lockToken)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        return fromVirtualFile(mountPoint.getVirtualFileById(id).moveTo(mountPoint.getVirtualFileById(parentId), newName, false, lockToken),
                false, PropertyFilter.ALL_FILTER);
    }

    @Path("rename/{id}")
    @Override
    public Item rename(@PathParam("id") String id,
                       @QueryParam("mediaType") MediaType newMediaType,
                       @QueryParam("newname") String newName,
                       @QueryParam("lockToken") String lockToken)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if ((newName == null || newName.isEmpty()) && newMediaType == null) {
            // Nothing to do. Return unchanged object.
            return getItem(id, false, PropertyFilter.ALL_FILTER);
        }
        final VirtualFile origin = mountPoint.getVirtualFileById(id);
        final VirtualFile renamedVirtualFile = origin.rename(newName, newMediaType == null ? null : newMediaType.toString(), lockToken);
        return fromVirtualFile(renamedVirtualFile, false, PropertyFilter.ALL_FILTER);
    }

    @Path("replace/{path:.*}")
    @Override
    public void replace(@PathParam("path") String path,
                       List<ReplacementSet> replacements,
                       @QueryParam("lockToken") String lockToken)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        VirtualFile projectRoot = mountPoint.getVirtualFile(path);
        if (!projectRoot.isFolder()) {
            throw new ConflictException("Given path must be an project root folder. ");
        }
        final Map<String, ReplacementContainer> changesPerFile = new HashMap<>();
        // fill changes matrix first
        for (final ReplacementSet replacement : replacements) {
            for (final String regex : replacement.getFiles()) {
                Pattern pattern  = Pattern.compile(regex);
                ItemNode rootNode = getTree(projectRoot.getId(), -1, false, PropertyFilter.ALL_FILTER);
                LinkedList<ItemNode> q = new LinkedList<>();
                q.add(rootNode);
                while (!q.isEmpty()) {
                    ItemNode node = q.pop();
                    Item item = node.getItem();
                    if (item.getItemType().equals(ItemType.FOLDER)) {
                        q.addAll(node.getChildren());
                    } else if (item.getItemType().equals(ItemType.FILE)) {
                        // for cases like:  src/main/java/(.*)
                        String itemInternalPath = item.getPath().substring(projectRoot.getPath().length() + 1);
                        if (pattern.matcher(item.getName()).matches() || pattern.matcher(itemInternalPath).matches()) {
                            ReplacementContainer container =
                                    (changesPerFile.get(item.getPath()) != null) ? changesPerFile.get(item.getPath())
                                                                                 : new ReplacementContainer();
                            for (Variable variable : replacement.getEntries()) {
                                String replaceMode  = variable.getReplacemode();
                                if (replaceMode == null || "variable_singlepass".equals(replaceMode)) {
                                    container.getVariableProps().put(variable.getFind(), variable.getReplace());
                                } else if ("text_multipass".equals(replaceMode)) {
                                    container.getTextProps().put(variable.getFind(), variable.getReplace());
                                }
                            }
                            changesPerFile.put(item.getPath(), container);
                        }
                    }
                }
            }
        }
        //now apply changes matrix
        for (Map.Entry<String, ReplacementContainer> entry : changesPerFile.entrySet()) {
            try {
                if (entry.getValue().hasReplacements()) {
                    ContentStream cs = mountPoint.getVirtualFile(entry.getKey()).getContent();
                    String content = IoUtil.readAndCloseQuietly(cs.getStream());
                    String modified =
                            Deserializer.resolveVariables(content, entry.getValue().getVariableProps(), false);
                    for (Map.Entry<String, String> replacement : entry.getValue().getTextProps().entrySet()) {
                        if (modified.contains(replacement.getKey())) {
                            modified = modified.replace(replacement.getKey(), replacement.getValue());
                        }
                    }
                    //better to compare big strings by hash codes first
                    if (!(content.hashCode() == modified.hashCode()) || !content.equals(modified)) {
                        mountPoint.getVirtualFile(entry.getKey())
                                  .updateContent(new ByteArrayInputStream(modified.getBytes(
                                          StandardCharsets.UTF_8)), lockToken);
                    }
                }
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }

    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Override
    public ItemList search(MultivaluedMap<String, String> query,
                           @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                           @QueryParam("skipCount") int skipCount,
                           @DefaultValue(PropertyFilter.ALL) @QueryParam("propertyFilter") PropertyFilter propertyFilter)
            throws ConflictException, ServerException {
        if (searcherProvider != null) {
            if (skipCount < 0) {
                throw new ConflictException("'skipCount' parameter is negative. ");
            }
            final QueryExpression expr = new QueryExpression()
                    .setPath(query.getFirst("path"))
                    .setName(query.getFirst("name"))
                    .setMediaType(query.getFirst("mediaType"))
                    .setText(query.getFirst("text"))
                    .setSkipCount(skipCount)
                    .setMaxItems(maxItems);

            final String[] result = searcherProvider.getSearcher(mountPoint, true).search(expr);
            if (skipCount > 0) {
                if (skipCount > result.length) {
                    throw new ConflictException("'skipCount' parameter is greater then total number of items. ");
                }
            }
            final int length = maxItems > 0 ? Math.min(result.length, maxItems) : result.length;
            final List<Item> items = new ArrayList<>(length);
            for (int i = skipCount; i < length; i++) {
                String path = result[i];
                try {
                    items.add(fromVirtualFile(mountPoint.getVirtualFile(path), false, propertyFilter));
                } catch (NotFoundException | ForbiddenException ignored) {
                }
            }

            return DtoFactory.getInstance().createDto(ItemList.class).withItems(items).withNumItems(result.length)
                             .withHasMoreItems(length < result.length);
        }
        throw new ServerException("Not supported. ");
    }

    @Override
    public ItemList search(MultivaluedMap<String, String> query, int maxItems, int skipCount) throws ConflictException, ServerException {
        return search(query, maxItems, skipCount, PropertyFilter.ALL_FILTER);
    }

    @Override
    public ItemList search(@QueryParam("statement") String statement,
                           @DefaultValue("-1") @QueryParam("maxItems") int maxItems,
                           @QueryParam("skipCount") int skipCount) throws ServerException {
        // No plan to support SQL at the moment.
        throw new ServerException("Not supported. ");
    }

    @Path("unlock/{id}")
    @Override
    public void unlock(@PathParam("id") String id, @QueryParam("lockToken") String lockToken)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        if (!getInfo().isLockSupported()) {
            throw new ServerException("Locking is not supported. ");
        }
        final VirtualFile virtualFile = mountPoint.getVirtualFileById(id);
        if (!virtualFile.isFile()) {
            throw new ConflictException(
                    String.format("Unable unlock '%s'. Item isn't locked. Locking allowed for files only. ", virtualFile.getPath()));
        }
        virtualFile.unlock(lockToken);
    }

    @Path("acl/{id}")
    @Override
    public void updateACL(@PathParam("id") String id,
                          List<AccessControlEntry> acl,
                          @DefaultValue("false") @QueryParam("override") Boolean override,
                          @QueryParam("lockToken") String lockToken) throws NotFoundException, ForbiddenException, ServerException {
        if (getInfo().getAclCapability() != ACLCapability.MANAGE) {
            throw new ServerException("Managing of ACL is not supported. ");
        }
        mountPoint.getVirtualFileById(id).updateACL(acl, override, lockToken);
    }

    @Path("content/{id}")
    @Override
    public void updateContent(
            @PathParam("id") String id,
            InputStream newContent,
            @QueryParam("lockToken") String lockToken) throws NotFoundException, ForbiddenException, ServerException {
        mountPoint.getVirtualFileById(id).updateContent(newContent, lockToken);
    }

    @Path("item/{id}")
    @Override
    public Item updateItem(@PathParam("id") String id,
                           List<Property> properties,
                           @QueryParam("lockToken") String lockToken) throws NotFoundException, ForbiddenException, ServerException {
        final VirtualFile virtualFile = mountPoint.getVirtualFileById(id);
        virtualFile.updateProperties(properties, lockToken);
        return fromVirtualFile(virtualFile, false, PropertyFilter.ALL_FILTER);
    }

    @Path("export/{folderId}")
    @Override
    public ContentStream exportZip(@PathParam("folderId") String folderId) throws NotFoundException, ForbiddenException, ServerException {
        return exportZip(mountPoint.getVirtualFileById(folderId));
    }

    // For usage from Project API.
    public static ContentStream exportZip(VirtualFile folder) throws ForbiddenException, ServerException {
        return folder.zip(VirtualFileFilter.ALL);
    }

    @Path("export/{folderId}")
    @Override
    public Response exportZip(@PathParam("folderId") String folderId, InputStream in)
            throws NotFoundException, ForbiddenException, ServerException {
        return exportZip(mountPoint.getVirtualFileById(folderId), in);
    }

    @Path("export/{folderId}")
    @Override
    public Response exportZipMultipart(@PathParam("folderId") String folderId, InputStream in)
            throws NotFoundException, ForbiddenException, ServerException {
        return exportZipMultipart(mountPoint.getVirtualFileById(folderId), in);
    }

    // For usage from Project API.
    public static Response exportZipMultipart(VirtualFile folder, InputStream in) throws ForbiddenException, ServerException {
        final List<String> deleted = new LinkedList<>();
        final ContentStream zip = exportZip(folder, in, deleted);
        if (zip == null) {
            return Response.status(204).build();
        }
        final List<OutputItem> multipart = new LinkedList<>();
        // String name, Object entity, MediaType mediaType, String fileName
        final OutputItem updates = OutputItem.create("updates", zip.getStream(), ExtMediaType.APPLICATION_ZIP_TYPE, zip.getFileName());
        updates.getHeaders().putSingle(HttpHeaders.CONTENT_LENGTH, Long.toString(zip.getLength()));
        multipart.add(updates);

        if (!deleted.isEmpty()) {
            multipart.add(OutputItem.create("removed-paths", deleted, MediaType.APPLICATION_JSON_TYPE));
        }

        final String boundary = NameGenerator.generate(null, 8);
        return Response
                .ok(new GenericEntity<List<OutputItem>>(multipart) {
                }, "multipart/form-data; boundary=" + boundary)
                .lastModified(zip.getLastModificationDate())
                .build();
    }


    // For usage from Project API.
    public static Response exportZip(VirtualFile folder, InputStream in) throws ForbiddenException, ServerException {
        final List<String> deleted = new LinkedList<>();
        final ContentStream zip = exportZip(folder, in, deleted);
        if (zip == null) {
            return Response.status(204).build();
        }
        final Response.ResponseBuilder responseBuilder = Response
                .ok(zip.getStream(), zip.getMimeType())
                .lastModified(zip.getLastModificationDate())
                .header(HttpHeaders.CONTENT_LENGTH, Long.toString(zip.getLength()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zip.getFileName() + '"');
        if (!deleted.isEmpty()) {
            final StringBuilder buff = new StringBuilder();
            for (String str : deleted) {
                if (buff.length() > 0) {
                    buff.append(',');
                }
                buff.append(str);
            }
            responseBuilder.header("x-removed-paths", deleted.toString());
        }
        return responseBuilder.build();
    }

    // For usage from Project API.
    protected static ContentStream exportZip(VirtualFile folder, InputStream in, List<String> deleted)
            throws ForbiddenException, ServerException {
        final List<Pair<String, String>> remote = new LinkedList<>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String hash = line.substring(0, 32); // 32 is length of MD-5 hash sum
                int startPath = 33;
                int l = line.length();
                while (startPath < l && Character.isWhitespace(line.charAt(startPath))) {
                    startPath++;
                }
                String relPath = line.substring(startPath);
                remote.add(Pair.of(hash, relPath));
            }
        } catch (IOException e) {
            throw new ServerException(e.getMessage(), e);
        }
        if (remote.isEmpty()) {
            return folder.zip(VirtualFileFilter.ALL);
        }
        final LazyIterator<Pair<String, String>> md5Sums = folder.countMd5Sums();
        final int size = md5Sums.size();
        final List<Pair<String, String>> local =
                size > 0 ? new ArrayList<Pair<String, String>>(size) : new ArrayList<Pair<String, String>>();
        while (md5Sums.hasNext()) {
            local.add(md5Sums.next());
        }
        final Comparator<Pair<String, String>> comp = new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                return o1.second.compareTo(o2.second);
            }
        };
        Collections.sort(remote, comp);
        Collections.sort(local, comp);
        int remoteIndex = 0;
        int localIndex = 0;
        final List<Pair<String, org.eclipse.che.api.vfs.server.Path>> diff = new LinkedList<>();
        while (remoteIndex < remote.size() && localIndex < local.size()) {
            final Pair<String, String> remoteItem = remote.get(remoteIndex);
            final Pair<String, String> localItem = local.get(localIndex);
            // compare path
            int r = remoteItem.second.compareTo(localItem.second);
            if (r == 0) {
                // remote and local file exist, compare md5sum
                if (!remoteItem.first.equals(localItem.first)) {
                    diff.add(Pair.of(remoteItem.second, folder.getVirtualFilePath().newPath(localItem.second)));
                }
                remoteIndex++;
                localIndex++;
            } else if (r > 0) {
                // new file
                diff.add(Pair.of((String)null, folder.getVirtualFilePath().newPath(localItem.second)));
                localIndex++;
            } else {
                // deleted file
                diff.add(Pair.of(remoteItem.second, (org.eclipse.che.api.vfs.server.Path)null));
                remoteIndex++;
            }
        }
        while (remoteIndex < remote.size()) {
            diff.add(Pair.of(remote.get(remoteIndex++).second, (org.eclipse.che.api.vfs.server.Path)null));
        }
        while (localIndex < local.size()) {
            diff.add(Pair.of((String)null, folder.getVirtualFilePath().newPath(local.get(localIndex++).second)));
        }

        if (diff.isEmpty()) {
            return null;
        }

        final ContentStream zip = folder.zip(new VirtualFileFilter() {
            @Override
            public boolean accept(VirtualFile file) {
                for (Pair<String, org.eclipse.che.api.vfs.server.Path> pair : diff) {
                    if (pair.second != null
                        && (pair.second.equals(file.getVirtualFilePath()) || pair.second.isChild(file.getVirtualFilePath()))) {
                        return true;
                    }
                }
                return false;
            }
        });

        deleted.clear();
        for (Pair<String, org.eclipse.che.api.vfs.server.Path> pair : diff) {
            if (pair.first != null && pair.second == null) {
                deleted.add(pair.first);
            }
        }
        return zip;
    }

    @Path("import/{parentId}")
    @Override
    public void importZip(@PathParam("parentId") String parentId,
                          InputStream in,
                          @DefaultValue("false") @QueryParam("overwrite") Boolean overwrite,
                          @DefaultValue("false") @QueryParam("skipFirstLevel") Boolean skipFirstLevel)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        final VirtualFile parent = mountPoint.getVirtualFileById(parentId);
        importZip(parent, in, overwrite, skipFirstLevel);
    }

    // For usage from Project API.
    public static void importZip(VirtualFile parent, InputStream in, boolean overwrite, boolean skipFirstLevel)
            throws ForbiddenException, ConflictException, ServerException {
        int stripNum = skipFirstLevel ? 1 : 0;
        parent.unzip(in, overwrite, stripNum);
    }

    @Path("downloadfile/{id}")
    @Override
    public Response downloadFile(@PathParam("id") String id) throws NotFoundException, ForbiddenException, ServerException {
        return downloadFile(getContent(id));
    }

    public static Response downloadFile(ContentStream content) {
        return Response
                .ok(content.getStream(), content.getMimeType())
                .lastModified(content.getLastModificationDate())
                .header(HttpHeaders.CONTENT_LENGTH, Long.toString(content.getLength()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + content.getFileName() + '"')
                .build();
    }

    @Path("uploadfile/{parentId}")
    @Override
    public Response uploadFile(@PathParam("parentId") String parentId, Iterator<FileItem> formData)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        return uploadFile(mountPoint.getVirtualFileById(parentId), formData);
    }

    public static Response uploadFile(VirtualFile parent, Iterator<FileItem> formData)
            throws ForbiddenException, ConflictException, ServerException {
        try {
            FileItem contentItem = null;
            String name = null;
            boolean overwrite = false;

            while (formData.hasNext()) {
                FileItem item = formData.next();
                if (!item.isFormField()) {
                    if (contentItem == null) {
                        contentItem = item;
                    } else {
                        throw new ServerException("More then one upload file is found but only one should be. ");
                    }
                } else if ("name".equals(item.getFieldName())) {
                    name = item.getString().trim();
                } else if ("overwrite".equals(item.getFieldName())) {
                    overwrite = Boolean.parseBoolean(item.getString().trim());
                }
            }

            if (contentItem == null) {
                throw new ServerException("Cannot find file for upload. ");
            }
            if (name == null || name.isEmpty()) {
                name = contentItem.getName();
            }

            try {
                try {
                    parent.createFile(name, contentItem.getInputStream());
                } catch (ConflictException e) {
                    if (!overwrite) {
                        throw new ConflictException("Unable upload file. Item with the same name exists. ");
                    }
                    parent.getChild(name).updateContent(contentItem.getInputStream(), null);
                }
            } catch (IOException ioe) {
                throw new ServerException(ioe.getMessage(), ioe);
            }

            return Response.ok("", MediaType.TEXT_HTML).build();
        } catch (ForbiddenException | ConflictException | ServerException e) {
            HtmlErrorFormatter.sendErrorAsHTML(e);
            // never thrown
            throw e;
        }
    }

    @Path("downloadzip/{folderId}")
    @Override
    public Response downloadZip(@PathParam("folderId") String folderId) throws NotFoundException, ForbiddenException, ServerException {
        final ContentStream zip = exportZip(folderId);
        return Response //
                .ok(zip.getStream(), zip.getMimeType()) //
                .lastModified(zip.getLastModificationDate()) //
                .header(HttpHeaders.CONTENT_LENGTH, Long.toString(zip.getLength())) //
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zip.getFileName() + '"') //
                .build();
    }

    @Path("uploadzip/{parentId}")
    @Override
    public Response uploadZip(@PathParam("parentId") String parentId, Iterator<FileItem> formData)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException {
        return uploadZip(mountPoint.getVirtualFileById(parentId), formData);
    }

    public static Response uploadZip(VirtualFile parent, Iterator<FileItem> formData)
            throws ForbiddenException, ConflictException, ServerException {
        try {
            FileItem contentItem = null;
            boolean overwrite = false;
            boolean skipFirstLevel = false;
            while (formData.hasNext()) {
                FileItem item = formData.next();
                if (!item.isFormField()) {
                    if (contentItem == null) {
                        contentItem = item;
                    } else {
                        throw new ServerException("More then one upload file is found but only one should be. ");
                    }
                } else if ("overwrite".equals(item.getFieldName())) {
                    overwrite = Boolean.parseBoolean(item.getString().trim());
                } else if ("skipFirstLevel".equals(item.getFieldName())) {
                    skipFirstLevel = Boolean.parseBoolean(item.getString().trim());
                }
            }
            if (contentItem == null) {
                throw new ServerException("Cannot find file for upload. ");
            }
            try {
                importZip(parent, contentItem.getInputStream(), overwrite, skipFirstLevel);
            } catch (IOException ioe) {
                throw new ServerException(ioe.getMessage(), ioe);
            }
            return Response.ok("", MediaType.TEXT_HTML).build();
        } catch (ForbiddenException | ConflictException | ServerException e) {
            HtmlErrorFormatter.sendErrorAsHTML(e);
            // never thrown
            throw e;
        }
    }


   /* ==================================================================== */

    protected VirtualFile getVirtualFileByPath(String path) throws NotFoundException, ForbiddenException, ServerException {
        return mountPoint.getVirtualFile(path);
    }

    protected Item fromVirtualFile(VirtualFile virtualFile, boolean includePermissions, PropertyFilter propertyFilter)
            throws ServerException {
        return fromVirtualFile(virtualFile, includePermissions, propertyFilter, true);
    }

    protected Item fromVirtualFile(VirtualFile virtualFile, boolean includePermissions, PropertyFilter propertyFilter, boolean addLinks)
            throws ServerException {
        final String id = virtualFile.getId();
        final String name = virtualFile.getName();
        final String path = virtualFile.getPath();
        final boolean isRoot = virtualFile.isFolder() && virtualFile.isRoot();
        final String parentId = isRoot ? null : virtualFile.getParent().getId();
        final String mediaType = virtualFile.getMediaType();
        final long created = virtualFile.getCreationDate();

        Item item;
        if (virtualFile.isFile()) {
            final boolean locked = virtualFile.isLocked();
            File dtoFile = (File)DtoFactory.getInstance().createDto(File.class)
                                           .withVersionId(virtualFile.getVersionId())
                                           .withLength(virtualFile.getLength())
                                           .withLastModificationDate(virtualFile.getLastModificationDate())
                                           .withLocked(locked)
                                           .withItemType(ItemType.FILE)
                                           .withParentId(parentId)
                                           .withId(id)
                                           .withName(name)
                                           .withPath(path)
                                           .withMimeType(mediaType)
                                           .withCreationDate(created)
                                           .withVfsId(vfsId)
                                           .withProperties(virtualFile.getProperties(propertyFilter));
            if (addLinks) {
                dtoFile.setLinks(LinksHelper.createFileLinks(baseUri, vfsId, id, id, path, mediaType, locked, parentId));
            }
            item = dtoFile;
        } else {
            Folder dtoFolder = (Folder)DtoFactory.getInstance().createDto(Folder.class)
                                                 .withItemType(ItemType.FOLDER)
                                                 .withParentId(parentId)
                                                 .withId(id)
                                                 .withName(name)
                                                 .withPath(path)
                                                 .withMimeType(mediaType)
                                                 .withCreationDate(created)
                                                 .withVfsId(vfsId)
                                                 .withProperties(virtualFile.getProperties(propertyFilter));
            if (addLinks) {
                dtoFolder.setLinks(LinksHelper.createFolderLinks(baseUri, vfsId, id, isRoot, parentId));
            }
            item = dtoFolder;
        }

        if (includePermissions) {
            VirtualFileSystemUser user = userContext.getVirtualFileSystemUser();
            VirtualFile current = virtualFile;
            while (current != null) {
                final Map<Principal, Set<String>> objectPermissions = current.getPermissions();
                if (!objectPermissions.isEmpty()) {
                    Set<String> userPermissions = new HashSet<>(4);
                    final Principal userPrincipal =
                            DtoFactory.getInstance().createDto(Principal.class).withName(user.getUserId()).withType(Principal.Type.USER);
                    Set<String> permissionsSet = objectPermissions.get(userPrincipal);
                    if (!(permissionsSet == null || permissionsSet.isEmpty())) {
                        userPermissions.addAll(permissionsSet);
                    }
                    final Principal anyPrincipal = DtoFactory.getInstance().createDto(Principal.class)
                                                             .withName(VirtualFileSystemInfo.ANY_PRINCIPAL).withType(Principal.Type.USER);
                    permissionsSet = objectPermissions.get(anyPrincipal);
                    if (!(permissionsSet == null || permissionsSet.isEmpty())) {
                        userPermissions.addAll(permissionsSet);
                    }
                    for (String group : user.getGroups()) {
                        final Principal groupPrincipal =
                                DtoFactory.getInstance().createDto(Principal.class).withName(group).withType(Principal.Type.GROUP);
                        permissionsSet = objectPermissions.get(groupPrincipal);
                        if (!(permissionsSet == null || permissionsSet.isEmpty())) {
                            userPermissions.addAll(permissionsSet);
                        }
                    }
                    item.setPermissions(new ArrayList<>(userPermissions));
                    break;
                } else {
                    current = current.getParent();
                }
            }
            if (item.getPermissions() == null) {
                item.setPermissions(Arrays.asList(BasicPermissions.ALL.value()));
            }
        }

        return item;
    }
}
