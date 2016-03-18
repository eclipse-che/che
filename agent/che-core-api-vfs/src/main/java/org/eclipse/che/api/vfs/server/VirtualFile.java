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

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.shared.PropertyFilter;
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.Property;
import org.eclipse.che.commons.lang.Pair;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Item of Virtual Filesystem.
 *
 * @author andrew00x
 */
public interface VirtualFile extends Comparable<VirtualFile> {
    /**
     * Gets unique id.
     */
    String getId();

    /**
     * Gets unique id of version of this VirtualFile.
     */
    String getVersionId();

    /**
     * Gets name.
     */
    String getName();

    /**
     * Gets path. Path of root folder is "/".
     */
    String getPath();

    /**
     * Gets internal representation of path of item.
     */
    Path getVirtualFilePath();

    /**
     * Tests whether this VirtualFile exists.
     */
    boolean exists();

    /**
     * Tests whether this VirtualFile is a root folder.
     */
    boolean isRoot();

    /**
     * Tests whether this VirtualFile is a regular file.
     */
    boolean isFile();

    /**
     * Tests whether this VirtualFile is a folder. Folder may contain other files.
     */
    boolean isFolder();

    /**
     * Gets creation time in long format or {@code -1} if creation time is unknown.
     */
    long getCreationDate();

    /**
     * Gets time of last modification in long format or {@code -1} if time is unknown.
     */
    long getLastModificationDate();

    /**
     * Gets parent folder. If this item is root folder this method always returns {@code null}.
     *
     * @see #isRoot()
     */
    VirtualFile getParent();

    /**
     * Gets media type of the VirtualFile. This method should not return {@code null}.
     *
     * @throws ServerException
     *         if an error occurs
     */
    String getMediaType() throws ServerException;

    /**
     * Sets media type of the VirtualFile.
     *
     * @param mediaType
     *         new media type
     * @throws ServerException
     *         if an error occurs
     */
    VirtualFile setMediaType(String mediaType) throws ServerException;

    /**
     * Gets iterator over files in this folder. If this VirtualFile isn't a folder this method returns empty iterator. If current user
     * doesn't have read access to some child they should not be included in returned result.
     *
     * @param filter
     *         virtual files filter
     * @throws ServerException
     *         if an error occurs
     */
    LazyIterator<VirtualFile> getChildren(VirtualFileFilter filter) throws ServerException;

    /**
     * Gets child by relative path. If this VirtualFile isn't folder this method returns {@code null}.
     *
     * @param path
     *         child item path
     * @return child
     * @throws ForbiddenException
     *         if current user doesn't have read permission to the child
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile getChild(String path) throws ForbiddenException, ServerException;

    /**
     * Gets content of the file.
     *
     * @return content ot he file
     * @throws ForbiddenException
     *         if this item isn't a file
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    ContentStream getContent() throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @param lockToken
     *         lock token. This parameter is required if the file is locked
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item isn't a file</li>
     *         <li>this file is locked and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(InputStream content, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Get length of content of the file. Always returns {@code 0} for folders.
     *
     * @throws ServerException
     *         if an error occurs
     */
    long getLength() throws ServerException;

    /**
     * Gets properties of the file.
     *
     * @throws ServerException
     *         if an error occurs
     * @see PropertyFilter
     */
    List<Property> getProperties(PropertyFilter filter) throws ServerException;

    /**
     * Updates properties of the file.
     *
     * @param properties
     *         list of properties to update
     * @param lockToken
     *         lock token. This parameter is required if the file is locked
     * @return VirtualFile after updating properties
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>at least one property can't be updated cause to any constraint, e.g. property is read only</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile updateProperties(List<Property> properties, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Gets value of property. If property has multiple values this method returns the first value in the set.
     *
     * @throws ServerException
     *         if an error occurs
     * @see #getPropertyValues(String)
     */
    String getPropertyValue(String name) throws ServerException;

    /**
     * Gets multiple values of property.
     *
     * @throws ServerException
     *         if an error occurs
     */
    String[] getPropertyValues(String name) throws ServerException;

    /**
     * Copies this file to the new parent.
     *
     * @throws ForbiddenException
     *         if specified {@code parent} doesn't denote a folder or user doesn't have write permission to the specified {@code parent}
     * @throws ConflictException
     *         if {@code parent} already contains item with the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    VirtualFile copyTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Copies this file to the new parent.
     *
     * @param parent
     *         the new parent
     * @param name
     *         a new name for the moved source, can be left {@code null} or empty {@code String} for current source name
     * @param overWrite
     *         should the destination be overwritten, set to true to overwrite, false otherwise
     * @return reference to copy
     * @throws ForbiddenException
     *         if specified {@code parent} doesn't denote a
     *         folder or user doesn't have write permission to the specified
     *         {@code parent}
     * @throws ConflictException
     *         if {@code parent} already contains item with
     *         the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    @Beta
    VirtualFile copyTo(VirtualFile parent, String name, boolean overWrite) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Moves this file to the new parent.
     *
     * @param parent
     *         parent to move
     * @param lockToken
     *         lock token. This parameter is required if the file is locked
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>specified {@code parent} doesn't denote a folder</li>
     *         <li>user doesn't have write permission to the specified {@code parent} or this item</li>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or doesn't match</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parent} already contains item with the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    @Beta
    VirtualFile moveTo(VirtualFile parent, String lockToken) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Moves this VirtualFile under new parent.
     *
     * @param parent
     *         parent to move
     * @param name
     *         a new name for the moved source, can be left {@code null} or empty {@code String} for current source name
     * @param overWrite
     *         should the destination be overwritten, set to true to overwrite, false otherwise
     * @param lockToken
     *         lock token. This parameter is required if the file is
     *         locked
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>specified {@code parent} doesn't denote a folder</li>
     *         <li>user doesn't have write permission to the specified {@code parent} or
     *         this item</li>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or
     *         doesn't match</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parent} already contains item with
     *         the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    VirtualFile moveTo(VirtualFile parent, String name, boolean overWrite, String lockToken)
            throws ForbiddenException, ConflictException, ServerException;

    /**
     * Renames and (or) update media type of this VirtualFile.
     *
     * @param newName
     *         new name
     * @param newMediaType
     *         new media type, may be {@code null} if need change name only
     * @param lockToken
     *         lock token. This parameter is required if the file is locked
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if parent of this item already contains other item with {@code newName}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile rename(String newName, String newMediaType, String lockToken) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Deletes this VirtualFile.
     *
     * @param lockToken
     *         lock token. This parameter is required if the file is locked
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file and {@code lockToken} is {code null} or doesn't match or if this item is folder that contains
     *         at least one locked file</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    void delete(String lockToken) throws ForbiddenException, ServerException;

    /**
     * Gets zipped content of folder denoted by this VirtualFile. All child items that user doesn't have read permission are not added in
     * result archive.
     *
     * @param filter
     *         filter of file. Only files that are matched to the filter are added in the zip archive
     * @return zipped content of folder denoted by this VirtualFile
     * @throws ForbiddenException
     *         if this item doesn't denote a folder
     * @throws ServerException
     *         if other error occurs
     */
    ContentStream zip(VirtualFileFilter filter) throws ForbiddenException, ServerException;

    /**
     * Imports ZIP content to the folder denoted by this VirtualFile.
     *
     * @param zipped
     *         ZIP content
     * @param overwrite
     *         overwrite or not existing files
     * @param stripNumber
     *         strip number leading components from file names on extraction.
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>if this item doesn't denote a folder</li>
     *         <li>user which perform operation doesn't have write permissions (include children)</li>
     *         <li>this folder contains at least one locked child</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code overwrite} is {@code false} and any item in zipped content causes name conflict
     * @throws ServerException
     *         if other error occurs
     */
    void unzip(InputStream zipped, boolean overwrite, int stripNumber) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Locks this VirtualFile.
     *
     * @param timeout
     *         lock timeout in milliseconds, pass {@code 0} to create lock without timeout
     * @return lock token. User should pass this token when tries update, delete or unlock locked file
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this VirtualFile doesn't denote a regular file</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if this file already locked
     * @throws ServerException
     *         if other error occurs
     */
    String lock(long timeout) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Unlocks this VirtualFile.
     *
     * @param lockToken
     *         lock token
     * @return VirtualFile after unlock
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code lockToken} is {@code null} or does not match</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if this item isn't locked
     * @throws ServerException
     *         if any other errors occur
     */
    VirtualFile unlock(String lockToken) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Tests whether this VirtualFile is locked.
     *
     * @throws ServerException
     *         if an error occurs
     */
    boolean isLocked() throws ServerException;

    /**
     * Gets permissions of this VirtualFile.
     *
     * @throws ServerException
     *         if an error occurs
     */
    Map<Principal, Set<String>> getPermissions() throws ServerException;

    /**
     * Gets ACL.
     *
     * @return ACL
     * @throws ServerException
     *         if an error occurs
     */
    List<AccessControlEntry> getACL() throws ServerException;

    /**
     * Updates ACL.
     *
     * @param acl
     *         ACL
     * @param override
     *         if {@code true} clear old ACL and apply new ACL, otherwise merge existed ACL and new one
     * @param lockToken
     *         lock token. This parameter is required if the file is locked
     * @return VirtualFile after updating ACL
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>{@code lockToken} is {@code null} or doesn't match</li>
     *         <li>user which perform operation doesn't have update_acl permissions</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile updateACL(List<AccessControlEntry> acl, boolean override, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Get all versions of this VirtualFile. If versioning isn't supported this iterator always contains just one item which denotes this
     * VirtualFile.
     *
     * @param filter
     *         virtual files filter
     * @return iterator over all versions
     * @throws ForbiddenException
     *         if this VirtualFile isn't regular file
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    LazyIterator<VirtualFile> getVersions(VirtualFileFilter filter) throws ForbiddenException, ServerException;

    /**
     * Gets single version of VirtualFile. If versioning isn't supported this method should return {@code this} instance if specified
     * {@code versionId} equals to the value returned by method {@link #getVersionId()}. If versioning isn't supported and
     * {@code versionId} isn't equals to the version id of this file {@link org.eclipse.che.api.core.NotFoundException} should be thrown.
     *
     * @param versionId
     *         id of version
     * @return single version of VirtualFile
     * @throws NotFoundException
     *         if there is no version with {@code versionId}
     * @throws ForbiddenException
     *         if this VirtualFile isn't regular file
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile getVersion(String versionId) throws NotFoundException, ForbiddenException, ServerException;

    /**
     * Creates new VirtualFile which denotes regular file and use this one as parent folder.
     *
     * @param name
     *         name
     * @param content
     *         content. In case of {@code null} empty file is created
     * @return newly create VirtualFile
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this VirtualFile does not denote a folder</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if parent already contains item with specified {@code name}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile createFile(String name, InputStream content)
            throws ForbiddenException, ConflictException, ServerException;

    /**
     * Creates new VirtualFile which denotes folder and use this one as parent folder.
     *
     * @param name
     *         name. If name is string separated by '/' all nonexistent parent folders must be created.
     * @return newly create VirtualFile that denotes folder
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this VirtualFile doesn't denote a folder</li>
     *         <li>user which perform operation doesn't have write permissions</li>
     *         </ul>
     * @throws ConflictException
     *         if item with specified {@code name} already exists
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile createFolder(String name) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Gets {@link MountPoint} to which this VirtualFile belongs.
     *
     * @return MountPoint
     */
    MountPoint getMountPoint();

    /**
     * Accepts an {@code VirtualFileVisitor}. Calls the {@link VirtualFileVisitor#visit(VirtualFile)} method.
     *
     * @param visitor
     *         VirtualFileVisitor to be accepted
     * @throws ServerException
     *         if an error occurs
     */
    void accept(VirtualFileVisitor visitor) throws ServerException;

    /**
     * Traverses recursively all files in current folder and count md5sum for each file. Method returns {@code Pair&lt;String, String&gt;}
     * for each file, all folders are omitted. Each {@code Pair} contains following structure:
     * <pre>
     *     Pair&lt;String,String&gt; pair = ...
     *     pair.first // md5sum of file represented as HEX String
     *     pair.second // Path of file that is relative to this file
     * </pre>
     * If this VirtualFile isn't a folder this method returns empty iterator. Note: any order of items in the returned iterator isn't
     * guaranteed.
     *
     * @throws ServerException
     *         if any error occurs
     */
    LazyIterator<Pair<String, String>> countMd5Sums() throws ServerException;

    /** Returns instance of {@link java.io.File} */
    @Beta
    File getIoFile();
}
