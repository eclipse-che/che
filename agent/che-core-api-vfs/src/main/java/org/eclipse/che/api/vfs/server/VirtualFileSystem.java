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
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.File;
import org.eclipse.che.api.vfs.shared.dto.Folder;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.api.vfs.shared.dto.ItemNode;
import org.eclipse.che.api.vfs.shared.dto.Lock;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.api.vfs.shared.dto.ReplacementSet;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.apache.commons.fileupload.FileItem;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Virtual file system abstraction.
 *
 * @author andrew00x
 */
public interface VirtualFileSystem {
    /**
     * Create copy of item {@code id} in {@code parentId} folder.
     *
     * @param id
     *         id of source item
     * @param parentId
     *         id of parent for new copy
     * @param newName
     *         new name for new copy
     * @return newly created copy of item
     * @throws NotFoundException
     *         if {@code id} or {@code parentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parentId} already contains item with the same name
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("copy")
    @Produces({MediaType.APPLICATION_JSON})
    Item copy(String id, String parentId, String newName) throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Clone item to destination Virtual File System.
     *
     * @param srcPath
     *         path of source item
     * @param srcVfsId
     *         id of sources Virtual File System
     * @param parentPath
     *         path of parent for new copy
     * @param name
     *         new name for copied item
     * @return newly created copy of item
     * @throws NotFoundException
     *         if {@code srcPath} or {@code parentPath} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentPath} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parentPath} already contains item with the same name
     * @throws ServerException
     *         if any other errors occur
     * @deprecated
     */
    @POST
    @Path("clone")
    @Deprecated
    Item clone(String srcPath, String srcVfsId, String parentPath, String name)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Create new File in specified folder.
     *
     * @param parentId
     *         id of parent for new File
     * @param name
     *         name of File
     * @param content
     *         content of File
     * @return newly created file
     * @throws NotFoundException
     *         if {@code parentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parentId} already contains item with specified {@code name}
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("file")
    @Produces({MediaType.APPLICATION_JSON})
    File createFile(String parentId, String name, InputStream content)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException;

    /**
     * Create new folder in specified folder.
     *
     * @param parentId
     *         id of parent for new folder
     * @param name
     *         name of new folder. If name is string separated by '/' all nonexistent parent folders must be created.
     * @return newly created folder
     * @throws NotFoundException
     *         if {@code parentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parentId} already contains item with specified {@code name}
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("folder")
    @Produces({MediaType.APPLICATION_JSON})
    Folder createFolder(String parentId, String name) throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Delete item {@code id}. If item is folder then all children of this folder should be removed.
     *
     * @param id
     *         id of item to be removed
     * @param lockToken
     *         lock token. This lock token will be used if {@code id} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} is root folder</li>
     *         <li>item {@code id} is locked file and {@code lockToken} is {code null} or doesn't match or if iem is folder that contains
     *         at least one locked file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("delete")
    void delete(String id, String lockToken) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get ACL applied to {@code id}. If there is no any ACL applied to item this method must return empty list. Example of JSON response:
     * <p/>
     * <pre>
     * [{"principal":"john","permissions":["all"]},{"principal":"marry","permissions":["read"]}]
     * </pre>
     * <p/>
     * Such JSON message means:
     * <ul>
     * <li>principal "john" has "all" permissions</li>
     * <li>principal "marry" has "read" permission only</li>
     * </ul>
     *
     * @param id
     *         id of item
     * @return ACL applied to item
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if user which perform operation has no permissions
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#getAclCapability()
     */
    @GET
    @Path("acl")
    @Produces({MediaType.APPLICATION_JSON})
    List<AccessControlEntry> getACL(String id) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get children of specified folder. Example of JSON response:
     * <p/>
     * <pre>
     * {
     *   "hasMoreItems":false,
     *   "items":[
     *       {
     *          "id":"/folder01/DOCUMENT01.txt",
     *          "type":"FILE",
     *          "path":"/folder01/DOCUMENT01.txt",
     *          "versionId":"current",
     *          "creationDate":1292574268440,
     *          "contentType":MediaType.TEXT_PLAIN,
     *          "length":100,
     *          "lastModificationDate":1292574268440
     *          "locked":false,
     *          "properties":[],
     *       }
     *   ],
     *   "numItems":1
     * }
     * </pre>
     *
     * @param folderId
     *         folder's id
     * @param maxItems
     *         max number of items in response. If {@code -1} then no limit of max items in result set
     * @param skipCount
     *         skip items. Must be equals or greater then {@code 0}
     * @param itemType
     *         item type filter. If not null then only item of specified type returned in result list. Expected one of type (case
     *         insensitive):
     *         <ul>
     *         <li>file</li>
     *         <li>folder</li>
     *         </ul>
     * @param includePermissions
     *         if {@code true} add permissions for current user for each item. See {@link org.eclipse.che.api.vfs.shared.dto.Item#getPermissions()}.
     *         If parameter isn't set then result is implementation specific.
     * @param propertyFilter
     *         only properties which are accepted by filter should be included in response. See {@link PropertyFilter#accept(String)}
     * @return list of children of specified folder
     * @throws NotFoundException
     *         if {@code folderId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code folderId} isn't a folder</li>
     *         <li>{@code itemType} is set but not one of the known item types (neither 'file' nor 'folder')</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         {@code skipCount} is negative or greater then total number of items
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.ItemType
     */
    @GET
    @Path("children")
    @Produces({MediaType.APPLICATION_JSON})
    ItemList getChildren(String folderId, int maxItems, int skipCount, String itemType, Boolean includePermissions,
                         PropertyFilter propertyFilter) throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    // For local usage. This method isn't accessible over REST interface.
    ItemList getChildren(String folderId, int maxItems, int skipCount, String itemType, boolean includePermissions)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Get tree of items starts from specified folder.
     *
     * @param folderId
     *         folder's id
     * @param depth
     *         depth for discover children if {@code -1} then get children at all levels
     * @param includePermissions
     *         if {@code true} add permissions for current user for each item. See {@link org.eclipse.che.api.vfs.shared.dto.Item#getPermissions()}.
     *         If parameter isn't set then result is implementation specific.
     * @param propertyFilter
     *         only properties which are accepted by filter should be included in response. See {@link PropertyFilter#accept(String)}
     * @return items tree started from specified folder
     * @throws NotFoundException
     *         if {@code folderId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code folderId} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("tree")
    @Produces({MediaType.APPLICATION_JSON})
    ItemNode getTree(String folderId, int depth, Boolean includePermissions, PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ServerException;

    // For local usage. This method isn't accessible over REST interface.
    ItemNode getTree(String folderId, int depth, boolean includePermissions)
            throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get content of File.
     *
     * @param id
     *         id of File
     * @return content
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("content")
    ContentStream getContent(String id) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get content of File by path.
     *
     * @param path
     *         path of File
     * @param versionId
     *         version id for File item. If {@code null} content of latest version returned. If versioning isn't supported this parameter
     *         must be {@code null}
     * @return content
     * @throws NotFoundException
     *         if {@code path} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code path} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#isVersioningSupported()
     */
    @GET
    @Path("contentbypath")
    ContentStream getContent(String path, String versionId) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get information about virtual file system and its capabilities.
     *
     * @return info about this virtual file system
     * @throws ServerException
     *         if any errors occur in VFS
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    VirtualFileSystemInfo getInfo() throws ServerException;

    /**
     * Get item by id. Example of JSON response:
     * <p/>
     * <pre>
     * {
     *   "id":"/folder01/DOCUMENT01.txt",
     *   "type":"FILE",
     *   "path":"/folder01/DOCUMENT01.txt",
     *   "versionId":"current",
     *   "creationDate":1292574268440,
     *   "contentType":MediaType.TEXT_PLAIN,
     *   "length":100,
     *   "lastModificationDate":1292574268440
     *   "locked":false,
     *   "properties":[],
     * }
     * </pre>
     *
     * @param id
     *         id of item
     * @param includePermissions
     *         if {@code true} add permissions for current user in item description. If parameter isn't set then result is implementation
     *         specific
     * @param propertyFilter
     *         only properties which are accepted by filter should be included in response. See {@link PropertyFilter#accept(String)}
     * @return item
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if user which perform operation has no permissions
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.Item#getPermissions()
     */
    @GET
    @Path("item")
    @Produces({MediaType.APPLICATION_JSON})
    Item getItem(String id, Boolean includePermissions, PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ServerException;

    // For local usage. This method isn't accessible over REST interface.
    Item getItem(String id, boolean includePermissions) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get item by path.
     *
     * @param path
     *         item path
     * @param versionId
     *         version id for File item. Pass {@code null} to get last version. Must be {@code null} for Folders. If versioning isn't
     *         supported this parameter must be {@code null}
     * @param includePermissions
     *         if {@code true} add permissions for current user in item description. If parameter isn't set then result is implementation
     *         specific
     * @param propertyFilter
     *         only properties which are accepted by filter should be included in response. See {@link PropertyFilter#accept(String)}
     * @return item
     * @throws NotFoundException
     *         if {@code path} or its version doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code versionId} is specified by item {@code path} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#isVersioningSupported()
     * @see org.eclipse.che.api.vfs.shared.dto.Item#getPermissions()
     */
    @GET
    @Path("itembypath")
    @Produces({MediaType.APPLICATION_JSON})
    Item getItemByPath(String path, String versionId, Boolean includePermissions, PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ServerException;

    // For local usage. This method isn't accessible over REST interface.
    Item getItemByPath(String path, String versionId, boolean includePermissions)
            throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get content of version of File item.
     *
     * @param id
     *         id of item
     * @param versionId
     *         version id
     * @return content response
     * @throws NotFoundException
     *         if {@code id} or its version doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("version")
    ContentStream getVersion(String id, String versionId) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Get list of versions of File. Even if File isn't versionable result must contain at least one item (current version of File).
     * Example of JSON response:
     * <p/>
     * <pre>
     * {
     *   "hasMoreItems":false,
     *   "items":[
     *       {
     *          "id":"/folder01/DOCUMENT01.txt",
     *          "type":"FILE",
     *          "path":"/folder01/DOCUMENT01.txt",
     *          "versionId":"1",
     *          "creationDate":1292574263440,
     *          "contentType":MediaType.TEXT_PLAIN,
     *          "length":56,
     *          "lastModificationDate":1292574263440
     *          "locked":false,
     *          "properties":[],
     *       }
     *       {
     *          "id":"/folder01/DOCUMENT01.txt",
     *          "type":"FILE",
     *          "path":"/folder01/DOCUMENT01.txt",
     *          "versionId":"2",
     *          "creationDate":1292574265640,
     *          "contentType":MediaType.TEXT_PLAIN,
     *          "length":83,
     *          "lastModificationDate":1292574265640
     *          "locked":false,
     *          "properties":[],
     *       }
     *       {
     *          "id":"/folder01/DOCUMENT01.txt",
     *          "type":"FILE",
     *          "path":"/folder01/DOCUMENT01.txt",
     *          "versionId":"current",
     *          "creationDate":1292574267340,
     *          "contentType":MediaType.TEXT_PLAIN,
     *          "length":100,
     *          "lastModificationDate":1292574268440
     *          "locked":false,
     *          "properties":[],
     *       }
     *   ],
     *   "numItems":1
     * }
     * </pre>
     *
     * @param id
     *         id of File
     * @param maxItems
     *         max number of items in response. If {@code -1} then no limit of max items in result set
     * @param skipCount
     *         the skip items. Must be equals or greater then {@code 0}
     * @param propertyFilter
     *         only properties which are accepted by filter should be included in response. See {@link PropertyFilter#accept(String)}
     * @return versions of file
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         {@code skipCount} is negative or greater then total number of items
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("version-history")
    @Produces({MediaType.APPLICATION_JSON})
    ItemList getVersions(String id, int maxItems, int skipCount, PropertyFilter propertyFilter)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    // For local usage. This method isn't accessible over REST interface.
    ItemList getVersions(String id, int maxItems, int skipCount)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException;

    /**
     * Place lock on File item. Example of JSON response if locking is successful:
     * <p/>
     * <pre>
     * {"lockToken":"f37ed0b2c0a8006600afbefda74c2dac"}
     * </pre>
     *
     * @param id
     *         item to be locked
     * @param timeout
     *         optional lock timeout in milliseconds, default value is {@code 0} which means no timeout. After specified timeout lock will
     *         be removed.
     * @return lock token
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if item already locked
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#isLockSupported()
     */
    @POST
    @Path("lock")
    @Produces({MediaType.APPLICATION_JSON})
    Lock lock(String id, long timeout) throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Move item {@code id} in {@code newparentId} folder. Example of JSON response:
     * <p/>
     * <pre>
     * {"id":"/TESTROOT/NEW_PARENT/DOCUMENT01.txt"}
     * </pre>
     *
     * @param id
     *         id of item to be moved
     * @param parentId
     *         id of new parent
     * @param newName
     *         new name for destination
     * @param lockToken
     *         lock token. This lock token will be used if {@code id} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @return moved item
     * @throws NotFoundException
     *         if {@code id} or {@code newparentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} isn't a folder</li>
     *         <li>item {@code id} is locked and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parentId} already contains item with the same name
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("move")
    @Produces({MediaType.APPLICATION_JSON})
    Item move(String id, String parentId, String newName, String lockToken)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Rename and(or) set content type for Item.
     *
     * @param id
     *         id of Item to be updated
     * @param mediaType
     *         new media type. May be not specified if not need to change media type, e.g. need rename only
     * @param newname
     *         new name of Item. May be not specified if not need to change name, e.g. need update media type only
     * @param lockToken
     *         lock token. This lock token will be used if {@code id} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @return renamed item
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ConflictException
     *         if parent folder already contains item with specified name
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>item {@code id} is locked and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("rename")
    @Produces({MediaType.APPLICATION_JSON})
    Item rename(String id, MediaType mediaType, String newname, String lockToken)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException;

    /**
     * Performs in-depth replacing of variables or text entries in the Items.
     *
     * @param path
     *         folder root to perform replace in depth
     * @param replacements
     *         list of replacements. each replacement contains filename (or regex pattern) and list of changes
     * @param lockToken
     *         lock token. This lock token will be used if {@code path} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @throws NotFoundException
     *         if {@code path} doesn't exist
     * @throws ConflictException
     *         if {@code path} is not a folder
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>item {@code path} is locked and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */

    @POST
    @Path("replace/{path:.*}")
    public void replace(String path, List<ReplacementSet> replacements, String lockToken)
            throws NotFoundException, ConflictException, ForbiddenException, ServerException;

    /**
     * Executes a SQL query statement against the contents of virtual file system.
     *
     * @param query
     *         set of opaque parameters of query statement. Set of parameters that can be passed by client and how SQL statement (in case
     *         of SQL storage) created from this parameters is implementation specific
     * @param maxItems
     *         max number of items in response. If {@code -1} then no limit of max items in result set
     * @param skipCount
     *         the skip items. Must be equals or greater then {@code 0}
     * @param propertyFilter
     *         only properties which are accepted by filter should be included in response. See {@link PropertyFilter#accept(String)}
     * @return query result
     * @throws ConflictException
     *         {@code skipCount} is negative or greater then total number of items
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#getQueryCapability()
     */
    @POST
    @Path("search")
    @Produces({MediaType.APPLICATION_JSON})
    ItemList search(MultivaluedMap<String, String> query, int maxItems, int skipCount, PropertyFilter propertyFilter)
            throws ConflictException, ServerException;

    // For local usage. This method isn't accessible over REST interface.
    ItemList search(MultivaluedMap<String, String> query, int maxItems, int skipCount) throws ConflictException, ServerException;

    /**
     * Execute a SQL query statement against the contents of virtual file system.
     *
     * @param statement
     *         query statement
     * @param maxItems
     *         max number of items in response. If {@code -1} then no limit of max items in result set
     * @param skipCount
     *         the skip items. Must be equals or greater then {@code 0}
     * @return query result
     * @throws ForbiddenException
     *         if query statement syntax is invalid
     * @throws ConflictException
     *         {@code skipCount} is negative or greater then total number of items
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#getQueryCapability()
     */
    @GET
    @Path("search")
    @Produces({MediaType.APPLICATION_JSON})
    ItemList search(String statement, int maxItems, int skipCount) throws ConflictException, ForbiddenException, ServerException;

    /**
     * Remove lock from file.
     *
     * @param id
     *         id of item to be unlocked
     * @param lockToken
     *         lock token
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if item isn't locked
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#isLockSupported()
     */
    @POST
    @Path("unlock")
    void unlock(String id, String lockToken) throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Update ACL of item. Example of JSON message:
     * <p/>
     * <pre>
     * [{"principal":"john","type":"USER","permissions":["all"]},{"principal":"marry","type":"USER","permissions":["read"]}]
     * </pre>
     * <p/>
     * JSON message as above will set "all" permissions for principal "john" and "read" permission only for principal "marry".
     *
     * @param id
     *         id of item for ACL updates
     * @param acl
     *         ACL to be applied to item. If method {@link AccessControlEntry#getPermissions()} for any principal return empty set of
     *         permissions then all permissions for this principal will be removed.
     * @param override
     *         if {@code true} then previous ACL will be overridden, if {@code false} then specified ACL will be merged with previous if
     *         any. If such parameters isn't specified then behavior is implementation specific
     * @param lockToken
     *         lock token. This lock token will be used if {@code id} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     * @see org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo#getAclCapability()
     */
    @POST
    @Path("acl")
    @Consumes({MediaType.APPLICATION_JSON})
    void updateACL(String id, List<AccessControlEntry> acl, Boolean override, String lockToken)
            throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Update content of File.
     *
     * @param id
     *         id of File
     * @param newContent
     *         new content of File
     * @param lockToken
     *         lock token. This lock token will be used if {@code id} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} isn't a file</li>
     *         <li>item {@code id} is locked and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("content")
    void updateContent(String id, InputStream newContent, String lockToken)
            throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Update properties of item.
     *
     * @param id
     *         id of item to be updated
     * @param properties
     *         new properties
     * @param lockToken
     *         lock token. This lock token will be used if {@code id} is locked. Pass {@code null} if there is no lock token, e.g. item is
     *         not locked
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>property can't be updated cause to any constraint, e.g. property is read only</li>
     *         <li>item {@code id} is locked and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("item")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    Item updateItem(String id, List<Property> properties, String lockToken) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Export content of {@code folderId} to ZIP archive.
     *
     * @param folderId
     *         folder for ZIP
     * @return ZIP as stream
     * @throws NotFoundException
     *         if {@code folderId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code folderId} item isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("export")
    @Produces({ExtMediaType.APPLICATION_ZIP})
    ContentStream exportZip(String folderId) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Export content of {@code folderId} to ZIP archive. Unlike to the method {@link #exportZip(String)} this method includes in the zip
     * response only updated files. Caller must send list of files with their md5sums in next format:
     * <pre>
     * &lt;md5sum&gt;&lt;space&gt;&lt;file path relative to requested folder&gt;
     * ...
     * </pre>
     * For example:
     * <pre>
     * ae3ddf74ea668c7fcee0e3865173e10b  my_project/pom.xml
     * 3ad8580e46189873b48c27983d965df8  my_project/src/main/java/org/test/Main.java
     * ...
     * </pre>
     * Example of typical usage of such method.
     * <ol>
     * <li>Imagine caller has content of this folder stored remotely</li>
     * <li>In some point of time caller likes to get updates</li>
     * <li>Caller traverses local tree and count md5sum for each file, folders must be omitted</li>
     * <li>Caller sends request. See about format of request body above</li>
     * <li>Response contains only files for which the md5sum doesn't match. Comma-separated list of names of removed files is added in
     * response header: <i>x-removed-paths</i></li>
     * <li>If there is no any updates this method return response with status: 204 No Content</li>
     * <li>Depending to the response caller updates his local copy of this folder</li>
     * </ol>
     *
     * @param folderId
     *         folder for ZIP
     * @param in
     *         stream, see above about its format
     * @return ZIP as stream
     * @throws NotFoundException
     *         if {@code folderId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code folderId} item isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("export")
    @Produces({ExtMediaType.APPLICATION_ZIP})
    @Consumes({MediaType.TEXT_PLAIN})
    Response exportZip(String folderId, InputStream in) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Export content of {@code folderId} to ZIP archive. Unlike to the method {@link #exportZip(String)} this method includes in the
     * zip response only updated files. Caller must send list of files with their md5sums in next format:
     * <pre>
     * &lt;md5sum&gt;&lt;space&gt;&lt;file path relative to requested folder&gt;
     * ...
     * </pre>
     * For example:
     * <pre>
     * ae3ddf74ea668c7fcee0e3865173e10b  my_project/pom.xml
     * 3ad8580e46189873b48c27983d965df8  my_project/src/main/java/org/test/Main.java
     * ...
     * </pre>
     * Example of typical usage of such method.
     * <ol>
     * <li>Imagine caller has content of this folder stored remotely</li>
     * <li>In some point of time caller likes to get updates</li>
     * <li>Caller traverses local tree and count md5sum for each file, folders must be omitted</li>
     * <li>Caller sends request. See about format of request body above</li>
     * <li>Multipart/form-data response contains archive with files for which the md5sum doesn't match (field 'updates') and list of names
     * of removed files (field 'removed-paths')</li>
     * <li>If there is no any updates this method return response with status: 204 No Content</li>
     * <li>Depending to the response caller updates his local copy of this folder</li>
     * </ol>
     *
     * @param folderId
     *         folder for ZIP
     * @param in
     *         stream, see above about its format
     * @return ZIP as stream
     * @throws NotFoundException
     *         if {@code folderId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code folderId} item isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("export")
    @Produces({MediaType.MULTIPART_FORM_DATA})
    @Consumes({MediaType.TEXT_PLAIN})
    Response exportZipMultipart(String folderId, InputStream in) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Import ZIP content.
     *
     * @param parentId
     *         id of folder to unzip
     * @param in
     *         ZIP content
     * @param overwrite
     *         overwrite or not existing files. If such parameters isn't specified then behavior is implementation specific
     * @param skipFirstLevel
     *         skip or not the first level of the archive content
     * @throws NotFoundException
     *         if {@code parentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} item isn't a folder</li>
     *         <li>{@code parentId} contains at least one locked child</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code overwrite} parameter is set to {@code false} and any item in zipped content causes name conflict
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("import")
    @Consumes({ExtMediaType.APPLICATION_ZIP})
    void importZip(String parentId, InputStream in, Boolean overwrite, Boolean skipFirstLevel)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Download content of File. Response must contains 'Content-Disposition' header to force web browser saves file.
     *
     * @param id
     *         id of File
     * @return Response with file content for download.
     * @throws NotFoundException
     *         if {@code id} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code id} isn't a file</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("downloadfile")
    Response downloadFile(String id) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Upload content of file. Content of file is part of multipart/form-data request, e.g. content sent from HTML form.
     *
     * @param parentId
     *         id of parent for new File
     * @param formData
     *         content of file and optional additional form fields. Set of additional field is implementation specific.
     * @return Response that represents response in HTML format.
     * @throws NotFoundException
     *         if {@code parentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} isn't a folder</li>
     *         <li>form doesn't contain all required fields. Set of fields is implementation specific</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parentId} already contains item with the same name. It is possible to prevent such type of exception by sending
     *         some form parameters that allow to overwrite file content
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("uploadfile")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.TEXT_HTML})
    Response uploadFile(String parentId, Iterator<FileItem> formData)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    /**
     * Download content of {@code folderId} as ZIP archive. Response must contains 'Content-Disposition' header to force web browser saves
     * file.
     *
     * @param folderId
     *         folder for ZIP
     * @return Response with ZIPed content of folder
     * @throws NotFoundException
     *         if {@code folderId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code folderId} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ServerException
     *         if any other errors occur
     */
    @GET
    @Path("downloadzip")
    @Produces({ExtMediaType.APPLICATION_ZIP})
    Response downloadZip(String folderId) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Import ZIP content. ZIP content is part of multipart/form-data request, e.g. content sent from HTML form.
     *
     * @param parentId
     *         id of folder to unzip
     * @param formData
     *         contains ZIPed folder and add optional additional form fields. Set of additional field is implementation specific.
     * @return Response that represents response in HTML format.
     * @throws NotFoundException
     *         if {@code parentId} doesn't exist
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code parentId} isn't a folder</li>
     *         <li>user which perform operation has no permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if item in zipped content causes name conflict
     * @throws ServerException
     *         if any other errors occur
     */
    @POST
    @Path("uploadzip")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces({MediaType.TEXT_HTML})
    Response uploadZip(String parentId, Iterator<FileItem> formData)
            throws NotFoundException, ForbiddenException, ConflictException, ServerException;

    MountPoint getMountPoint();
}
