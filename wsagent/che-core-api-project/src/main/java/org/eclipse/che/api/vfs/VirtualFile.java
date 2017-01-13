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
package org.eclipse.che.api.vfs;

import com.google.common.annotations.Beta;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Item of Virtual Filesystem.
 *
 * @author andrew00x
 */
public interface VirtualFile extends Comparable<VirtualFile> {
    /**
     * Gets name.
     */
    String getName();

    /**
     * Gets internal representation of path of item.
     */
    Path getPath();

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
     * Gets files in this folder. If this VirtualFile is not a folder this method returns empty list.
     *
     * @param filter
     *         virtual files filter
     * @throws ServerException
     *         if an error occurs
     */
    List<VirtualFile> getChildren(VirtualFileFilter filter) throws ServerException;

    /**
     * Gets files in this folder. If this VirtualFile is not a folder this method returns empty list.
     *
     * @throws ServerException
     *         if an error occurs
     */
    List<VirtualFile> getChildren() throws ServerException;

    boolean hasChild(Path path) throws ServerException;

    /**
     * Gets child by relative path. If this VirtualFile is not folder this method returns {@code null}.
     *
     * @param path
     *         child item path
     * @return child or {@code null} if path does not exist
     * @throws ServerException
     *         if an error occurs
     */
    VirtualFile getChild(Path path) throws ServerException;

    /**
     * Gets content of the file.
     *
     * @return content ot he file
     * @throws ForbiddenException
     *         if this item is not a file
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    InputStream getContent() throws ForbiddenException, ServerException;

    /**
     * Gets content of the file as bytes.
     *
     * @return content ot he file
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>size of file is too big and might not be retrieved as bytes</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    byte[] getContentAsBytes() throws ForbiddenException, ServerException;

    /**
     * Gets content of the file as String decoding bytes using the platform's default charset.
     *
     * @return content ot he file
     * @throws ForbiddenException
     *         if this item is not a file
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    String getContentAsString() throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or does not match</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(InputStream content, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>this item is locked file</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(InputStream content) throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or does not match</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(byte[] content, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>this item is locked file</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(byte[] content) throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or does not match</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(String content, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Updates content of the file.
     *
     * @param content
     *         content
     * @return VirtualFile after updating content
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is not a file</li>
     *         <li>this item is locked file</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     * @see #isFile()
     */
    VirtualFile updateContent(String content) throws ForbiddenException, ServerException;

    /**
     * Get length of content of the file. Always returns {@code 0} for folders.
     *
     * @throws ServerException
     *         if an error occurs
     */
    long getLength() throws ServerException;

    /**
     * Gets properties of the file. Updating of map returned by this method does not effect state of this file.
     *
     * @throws ServerException
     *         if an error occurs
     */
    Map<String, String> getProperties() throws ServerException;

    /**
     * Gets value of property.
     *
     * @throws ServerException
     *         if an error occurs
     */
    String getProperty(String name) throws ServerException;

    /**
     * Updates properties of the file.
     *
     * @param properties
     *         map of properties to update
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @return VirtualFile after updating properties
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or does not match</li>
     *         <li>at least one property can't be updated cause to any constraint, e.g. property is read only</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile updateProperties(Map<String, String> properties, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Updates properties of the file.
     *
     * @param properties
     *         map of properties to update
     * @return VirtualFile after updating properties
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file</li>
     *         <li>at least one property can't be updated cause to any constraint, e.g. property is read only</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile updateProperties(Map<String, String> properties) throws ForbiddenException, ServerException;

    /**
     * Set property of the file.
     *
     * @param name
     *         name of property
     * @param value
     *         value of property
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @return VirtualFile after updating property
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or does not match</li>
     *         <li>property can't be updated cause to any constraint, e.g. property is read only</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile setProperty(String name, String value, String lockToken) throws ForbiddenException, ServerException;

    /**
     * Set property of the file.
     *
     * @param name
     *         name of property
     * @param value
     *         value of property
     * @return VirtualFile after updating property
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file</li>
     *         <li>property can't be updated cause to any constraint, e.g. property is read only</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile setProperty(String name, String value) throws ForbiddenException, ServerException;

    /**
     * Copies this file to the new parent.
     *
     * @throws ForbiddenException
     *         if specified {@code parent} does not denote a folder
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
     * @param overwrite
     *         should the destination be overwritten, set to true to overwrite, false otherwise
     * @return reference to copy
     * @throws ForbiddenException
     *         if specified {@code parent} does not denote a folder
     * @throws ConflictException
     *         if {@code parent} already contains item with the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    @Beta
    VirtualFile copyTo(VirtualFile parent, String name, boolean overwrite) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Moves this file to the new parent.
     *
     * @param parent
     *         parent to move
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>specified {@code parent} does not denote a folder</li>
     *         <li>this item is locked file</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parent} already contains item with the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    VirtualFile moveTo(VirtualFile parent) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Moves this VirtualFile under new parent.
     *
     * @param parent
     *         parent to move
     * @param name
     *         a new name for the moved source, can be left {@code null} or empty {@code String} for current source name
     * @param overwrite
     *         should the destination be overwritten, set to {@code true} to overwrite, {@code false} otherwise
     * @param lockToken
     *         lock token. This parameter is required if the file is
     *         locked
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>specified {@code parent} does not denote a folder</li>
     *         <li>this item is locked file and {@code lockToken} is {@code null} or does not match</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code parent} already contains item with the same name as this virtual file has
     * @throws ServerException
     *         if other error occurs
     * @see #isFolder()
     */
    VirtualFile moveTo(VirtualFile parent, String name, boolean overwrite, String lockToken) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Renames this VirtualFile.
     *
     * @param newName
     *         new name
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @throws ForbiddenException
     *         if this item is locked file and {@code lockToken} is {@code null} or does not match
     * @throws ConflictException
     *         if parent of this item already contains other item with {@code newName}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile rename(String newName, String lockToken) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Renames this VirtualFile.
     *
     * @param newName
     *         new name
     * @throws ForbiddenException
     *         if this item is locked file
     * @throws ConflictException
     *         if parent of this item already contains other item with {@code newName}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile rename(String newName) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Deletes this VirtualFile.
     *
     * @param lockToken
     *         lock token. This parameter is required if the file is locked otherwise might be {@code null}
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file and {@code lockToken} is {code null} or does not match</li>
     *         <li>this item is folder that contains at least one locked file</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    void delete(String lockToken) throws ForbiddenException, ServerException;

    /**
     * Deletes this VirtualFile.
     *
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item is locked file</li>
     *         <li>this item is folder that contains at least one locked file</li>
     *         </ul>
     * @throws ServerException
     *         if other error occurs
     */
    void delete() throws ForbiddenException, ServerException;

    /**
     * Gets content of folder denoted by this VirtualFile as zip archive.
     *
     * @return zipped content of folder denoted by this VirtualFile
     * @throws ForbiddenException
     *         if this item does not denote a folder
     * @throws ServerException
     *         if other error occurs
     */
    InputStream zip() throws ForbiddenException, ServerException;

    /**
     * Extracts zip archive to the folder denoted by this VirtualFile.
     *
     * @param zipped
     *         ZIP archive
     * @param overwrite
     *         overwrite existing files
     * @param stripNumber
     *         strip number leading components from file names on extraction.
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item does not denote a folder</li>
     *         <li>this folder contains at least one locked child that need to be updated</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code overwrite} is {@code false} and any item in zip archive causes name conflict
     * @throws ServerException
     *         if other error occurs
     */
    void unzip(InputStream zipped, boolean overwrite, int stripNumber) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Gets content of folder denoted by this VirtualFile as TAR archive.
     *
     * @return content of folder denoted by this VirtualFile as TAR archive
     * @throws ForbiddenException
     *         if this item does not denote a folder
     * @throws ServerException
     *         if other error occurs
     */
    InputStream tar() throws ForbiddenException, ServerException;

    /**
     * Extracts tar archive to the folder denoted by this VirtualFile.
     *
     * @param tarArchive
     *         TAR archive
     * @param overwrite
     *         overwrite existing files
     * @param stripNumber
     *         strip number leading components from file names on extraction.
     * @throws ForbiddenException
     *         if any of following conditions are met:
     *         <ul>
     *         <li>this item does not denote a folder</li>
     *         <li>this folder contains at least one locked child that need to be updated</li>
     *         </ul>
     * @throws ConflictException
     *         if {@code overwrite} is {@code false} and any item in tar archive causes name conflict
     * @throws ServerException
     *         if other error occurs
     */
    void untar(InputStream tarArchive, boolean overwrite, int stripNumber) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Locks this VirtualFile.
     *
     * @param timeout
     *         lock timeout in milliseconds, pass {@code 0} to create lock without timeout
     * @return lock token. User should pass this token when tries update, delete, rename or unlock locked file
     * @throws ForbiddenException
     *         if this VirtualFile does not denote a regular file
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
     *         if {@code lockToken} is {@code null} or does not match
     * @throws ConflictException
     *         if this item is not locked
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
     * Creates new VirtualFile which denotes regular file and use this one as parent folder.
     *
     * @param name
     *         name
     * @param content
     *         content. In case of {@code null} empty file is created
     * @return newly create VirtualFile
     * @throws ForbiddenException
     *         if this VirtualFile does not denote a folder
     * @throws ConflictException
     *         if parent already contains item with specified {@code name}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile createFile(String name, InputStream content)
            throws ForbiddenException, ConflictException, ServerException;

    /**
     * Creates new VirtualFile which denotes regular file and use this one as parent folder.
     *
     * @param name
     *         name
     * @param content
     *         content. In case of {@code null} empty file is created
     * @return newly create VirtualFile
     * @throws ForbiddenException
     *         if this VirtualFile does not denote a folder
     * @throws ConflictException
     *         if parent already contains item with specified {@code name}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile createFile(String name, byte[] content)
            throws ForbiddenException, ConflictException, ServerException;

    /**
     * Creates new VirtualFile which denotes regular file and use this one as parent folder.
     *
     * @param name
     *         name
     * @param content
     *         content. In case of {@code null} empty file is created
     * @return newly create VirtualFile
     * @throws ForbiddenException
     *         if this VirtualFile does not denote a folder
     * @throws ConflictException
     *         if parent already contains item with specified {@code name}
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile createFile(String name, String content)
            throws ForbiddenException, ConflictException, ServerException;

    /**
     * Creates new VirtualFile which denotes folder and use this one as parent folder.
     *
     * @param name
     *         name. If name is string separated by '/' all nonexistent parent folders must be created.
     * @return newly create VirtualFile that denotes folder
     * @throws ForbiddenException
     *         if this VirtualFile does not denote a folder
     * @throws ConflictException
     *         if item with specified {@code name} already exists
     * @throws ServerException
     *         if other error occurs
     */
    VirtualFile createFolder(String name) throws ForbiddenException, ConflictException, ServerException;

    /**
     * Gets {@link VirtualFileSystem} to which this VirtualFile belongs.
     *
     * @return VirtualFileSystem
     */
    VirtualFileSystem getFileSystem();

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
     * If this VirtualFile is not a folder this method returns empty list. Note: any order of items in the returned list is not
     * guaranteed.
     *
     * @throws ServerException
     *         if any error occurs
     */
    List<Pair<String, String>> countMd5Sums() throws ServerException;

    /**
     * Gets java.io.File if implementation uses java.io.File as backend.
     *
     * @return java.io.File or {@code null} is java.io.File is not available
     */
    java.io.File toIoFile();
}
