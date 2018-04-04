/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation James Blackburn (Broadcom
 * Corp.) - ongoing development
 * *****************************************************************************
 */
package org.eclipse.core.resources;

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * Files are leaf resources which contain data. The contents of a file resource is stored as a file
 * in the local file system.
 *
 * <p>Files, like folders, may exist in the workspace but not be local; non-local file resources
 * serve as place-holders for files whose content and properties have not yet been fetched from a
 * repository.
 *
 * <p>Files implement the <code>IAdaptable</code> interface; extensions are managed by the
 * platform's adapter manager.
 *
 * @see Platform#getAdapterManager()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IFile extends IResource, IEncodedStorage, IAdaptable {
  /**
   * Character encoding constant (value 0) which identifies files that have an unknown character
   * encoding scheme.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_UNKNOWN = 0;
  /**
   * Character encoding constant (value 1) which identifies files that are encoded with the US-ASCII
   * character encoding scheme.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_US_ASCII = 1;
  /**
   * Character encoding constant (value 2) which identifies files that are encoded with the
   * ISO-8859-1 character encoding scheme, also known as ISO-LATIN-1.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_ISO_8859_1 = 2;
  /**
   * Character encoding constant (value 3) which identifies files that are encoded with the UTF-8
   * character encoding scheme.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_UTF_8 = 3;
  /**
   * Character encoding constant (value 4) which identifies files that are encoded with the UTF-16BE
   * character encoding scheme.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_UTF_16BE = 4;
  /**
   * Character encoding constant (value 5) which identifies files that are encoded with the UTF-16LE
   * character encoding scheme.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_UTF_16LE = 5;
  /**
   * Character encoding constant (value 6) which identifies files that are encoded with the UTF-16
   * character encoding scheme.
   *
   * @see IFile#getEncoding()
   * @deprecated see getEncoding for details
   */
  @Deprecated public int ENCODING_UTF_16 = 6;

  /**
   * Appends the entire contents of the given stream to this file.
   *
   * <p>This is a convenience method, fully equivalent to:
   *
   * <pre>
   *   appendContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
   * </pre>
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's content have been changed.
   *
   * <p>This method is long-running; progress and cancelation are provided by the given progress
   * monitor.
   *
   * @param source an input stream containing the new contents of the file
   * @param force a flag controlling how to deal with resources that are not in sync with the local
   *     file system
   * @param keepHistory a flag indicating whether or not to store the current contents in the local
   *     history
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and <code>force </code> is <code>false</code>.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The file modification validator disallowed the change.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see #appendContents(java.io.InputStream,int,IProgressMonitor)
   */
  public void appendContents(
      InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Appends the entire contents of the given stream to this file. The stream, which must not be
   * <code>null</code>, will get closed whether this method succeeds or fails.
   *
   * <p>The <code>FORCE</code> update flag controls how this method deals with cases where the
   * workspace is not completely in sync with the local file system. If <code>FORCE</code> is not
   * specified, the method will only attempt to overwrite a corresponding file in the local file
   * system provided it is in sync with the workspace. This option ensures there is no unintended
   * data loss; it is the recommended setting. However, if <code>FORCE</code> is specified, an
   * attempt will be made to write a corresponding file in the local file system, overwriting any
   * existing one if need be. In either case, if this method succeeds, the resource will be marked
   * as being local (even if it wasn't before).
   *
   * <p>If this file is non-local then this method will always fail. The only exception is when
   * <code>FORCE</code> is specified and the file exists in the local file system. In this case the
   * file is made local and the given contents are appended.
   *
   * <p>The <code>KEEP_HISTORY</code> update flag controls whether or not a copy of current contents
   * of this file should be captured in the workspace's local history (properties are not recorded
   * in the local history). The local history mechanism serves as a safety net to help the user
   * recover from mistakes that might otherwise result in data loss. Specifying <code>KEEP_HISTORY
   * </code> is recommended except in circumstances where past states of the files are of no
   * conceivable interest to the user. Note that local history is maintained with each individual
   * project, and gets discarded when a project is deleted from the workspace. This flag is ignored
   * if the file was not previously local.
   *
   * <p>Update flags other than <code>FORCE</code> and <code>KEEP_HISTORY</code> are ignored.
   *
   * <p>Prior to modifying the contents of this file, the file modification validator (if provided
   * by the VCM plug-in), will be given a chance to perform any last minute preparations. Validation
   * is performed by calling <code>IFileModificationValidator.validateSave</code> on this file. If
   * the validation fails, then this operation will fail.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's content have been changed.
   *
   * <p>This method is long-running; progress and cancelation are provided by the given progress
   * monitor.
   *
   * @param source an input stream containing the new contents of the file
   * @param updateFlags bit-wise or of update flag constants (<code>FORCE</code> and <code>
   *     KEEP_HISTORY</code>)
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and <code>FORCE</code> is not specified.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The file modification validator disallowed the change.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResourceRuleFactory#modifyRule(IResource)
   * @since 2.0
   */
  public void appendContents(InputStream source, int updateFlags, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Creates a new file resource as a member of this handle's parent resource.
   *
   * <p>This is a convenience method, fully equivalent to:
   *
   * <pre>
   *   create(source, (force ? FORCE : IResource.NONE), monitor);
   * </pre>
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that the file has been added to its parent.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param source an input stream containing the initial contents of the file, or <code>null</code>
   *     if the file should be marked as not local
   * @param force a flag controlling how to deal with resources that are not in sync with the local
   *     file system
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource already exists in the workspace.
   *       <li>The parent of this resource does not exist.
   *       <li>The parent of this resource is a virtual folder.
   *       <li>The project of this resource is not accessible.
   *       <li>The parent contains a resource of a different type at the same path as this resource.
   *       <li>The name of this resource is not valid (according to <code>IWorkspace.validateName
   *           </code>).
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The corresponding location in the local file system is occupied by a file and <code>
   *           force </code> is <code>false</code>.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   */
  public void create(InputStream source, boolean force, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Creates a new file resource as a member of this handle's parent resource. The resource's
   * contents are supplied by the data in the given stream. This method closes the stream whether it
   * succeeds or fails. If the stream is <code>null</code> then a file is not created in the local
   * file system and the created file resource is marked as being non-local.
   *
   * <p>The {@link IResource#FORCE} update flag controls how this method deals with cases where the
   * workspace is not completely in sync with the local file system. If {@link IResource#FORCE} is
   * not specified, the method will only attempt to write a file in the local file system if it does
   * not already exist. This option ensures there is no unintended data loss; it is the recommended
   * setting. However, if {@link IResource#FORCE} is specified, this method will attempt to write a
   * corresponding file in the local file system, overwriting any existing one if need be.
   *
   * <p>The {@link IResource#DERIVED} update flag indicates that this resource should immediately be
   * set as a derived resource. Specifying this flag is equivalent to atomically calling {@link
   * IResource#setDerived(boolean)} with a value of <code>true</code> immediately after creating the
   * resource.
   *
   * <p>The {@link IResource#TEAM_PRIVATE} update flag indicates that this resource should
   * immediately be set as a team private resource. Specifying this flag is equivalent to atomically
   * calling {@link IResource#setTeamPrivateMember(boolean)} with a value of <code>true</code>
   * immediately after creating the resource.
   *
   * <p>The {@link IResource#HIDDEN} update flag indicates that this resource should immediately be
   * set as a hidden resource. Specifying this flag is equivalent to atomically calling {@link
   * IResource#setHidden(boolean)} with a value of <code>true</code> immediately after creating the
   * resource.
   *
   * <p>Update flags other than those listed above are ignored.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that the file has been added to its parent.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param source an input stream containing the initial contents of the file, or <code>null</code>
   *     if the file should be marked as not local
   * @param updateFlags bit-wise or of update flag constants ({@link IResource#FORCE}, {@link
   *     IResource#DERIVED}, and {@link IResource#TEAM_PRIVATE})
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource already exists in the workspace.
   *       <li>The parent of this resource does not exist.
   *       <li>The parent of this resource is a virtual folder.
   *       <li>The project of this resource is not accessible.
   *       <li>The parent contains a resource of a different type at the same path as this resource.
   *       <li>The name of this resource is not valid (according to <code>IWorkspace.validateName
   *           </code>).
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The corresponding location in the local file system is occupied by a file and <code>
   *           FORCE</code> is not specified.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResourceRuleFactory#createRule(IResource)
   * @since 2.0
   */
  public void create(InputStream source, int updateFlags, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Creates a new file resource as a member of this handle's parent resource. The file's contents
   * will be located in the file specified by the given file system path. The given path must be
   * either an absolute file system path, or a relative path whose first segment is the name of a
   * workspace path variable.
   *
   * <p>The {@link IResource#ALLOW_MISSING_LOCAL} update flag controls how this method deals with
   * cases where the local file system file to be linked does not exist, or is relative to a
   * workspace path variable that is not defined. If {@link IResource#ALLOW_MISSING_LOCAL} is
   * specified, the operation will succeed even if the local file is missing, or the path is
   * relative to an undefined variable. If {@link IResource#ALLOW_MISSING_LOCAL} is not specified,
   * the operation will fail in the case where the local file system file does not exist or the path
   * is relative to an undefined variable.
   *
   * <p>The {@link IResource#REPLACE} update flag controls how this method deals with cases where a
   * resource of the same name as the prospective link already exists. If {@link IResource#REPLACE}
   * is specified, then the existing linked resource's location is replaced by localLocation's
   * value. This does <b>not</b> cause the underlying file system contents of that resource to be
   * deleted. If {@link IResource#REPLACE} is not specified, this method will fail if an existing
   * resource exists of the same name.
   *
   * <p>The {@link IResource#HIDDEN} update flag indicates that this resource should immediately be
   * set as a hidden resource. Specifying this flag is equivalent to atomically calling {@link
   * IResource#setHidden(boolean)} with a value of <code>true</code> immediately after creating the
   * resource.
   *
   * <p>Update flags other than those listed above are ignored.
   *
   * <p>This method synchronizes this resource with the local file system at the given location.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that the file has been added to its parent.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param localLocation a file system path where the file should be linked
   * @param updateFlags bit-wise or of update flag constants ({@link IResource#ALLOW_MISSING_LOCAL},
   *     {@link IResource#REPLACE} and {@link IResource#HIDDEN})
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource already exists in the workspace.
   *       <li>The workspace contains a resource of a different type at the same path as this
   *           resource.
   *       <li>The parent of this resource does not exist.
   *       <li>The parent of this resource is not an open project
   *       <li>The name of this resource is not valid (according to <code>IWorkspace.validateName
   *           </code>).
   *       <li>The corresponding location in the local file system does not exist, or is relative to
   *           an undefined variable, and <code>ALLOW_MISSING_LOCAL</code> is not specified.
   *       <li>The corresponding location in the local file system is occupied by a directory (as
   *           opposed to a file).
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The team provider for the project which contains this folder does not permit linked
   *           resources.
   *       <li>This folder's project contains a nature which does not permit linked resources.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResource#isLinked()
   * @see IResource#ALLOW_MISSING_LOCAL
   * @see IResource#REPLACE
   * @see IResource#HIDDEN
   * @since 2.1
   */
  public void createLink(IPath localLocation, int updateFlags, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Creates a new file resource as a member of this handle's parent resource. The file's contents
   * will be located in the file specified by the given URI. The given URI must be either absolute,
   * or a relative URI whose first path segment is the name of a workspace path variable.
   *
   * <p>The <code>ALLOW_MISSING_LOCAL</code> update flag controls how this method deals with cases
   * where the file system file to be linked does not exist, or is relative to a workspace path
   * variable that is not defined. If <code>ALLOW_MISSING_LOCAL</code> is specified, the operation
   * will succeed even if the local file is missing, or the path is relative to an undefined
   * variable. If <code>ALLOW_MISSING_LOCAL</code> is not specified, the operation will fail in the
   * case where the file system file does not exist or the path is relative to an undefined
   * variable.
   *
   * <p>The {@link IResource#REPLACE} update flag controls how this method deals with cases where a
   * resource of the same name as the prospective link already exists. If {@link IResource#REPLACE}
   * is specified, then any existing resource with the same name is removed from the workspace to
   * make way for creation of the link. This does <b>not</b> cause the underlying file system
   * contents of that resource to be deleted. If {@link IResource#REPLACE} is not specified, this
   * method will fail if an existing resource exists of the same name.
   *
   * <p>The {@link IResource#HIDDEN} update flag indicates that this resource should immediately be
   * set as a hidden resource. Specifying this flag is equivalent to atomically calling {@link
   * IResource#setHidden(boolean)} with a value of <code>true</code> immediately after creating the
   * resource.
   *
   * <p>Update flags other than those listed above are ignored.
   *
   * <p>This method synchronizes this resource with the file system at the given location.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that the file has been added to its parent.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param location a file system URI where the file should be linked
   * @param updateFlags bit-wise or of update flag constants ({@link IResource#ALLOW_MISSING_LOCAL},
   *     {@link IResource#REPLACE} and {@link IResource#HIDDEN})
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource already exists in the workspace.
   *       <li>The workspace contains a resource of a different type at the same path as this
   *           resource.
   *       <li>The parent of this resource does not exist.
   *       <li>The parent of this resource is not an open project
   *       <li>The name of this resource is not valid (according to <code>IWorkspace.validateName
   *           </code>).
   *       <li>The corresponding location in the file system does not exist, or is relative to an
   *           undefined variable, and <code>ALLOW_MISSING_LOCAL</code> is not specified.
   *       <li>The corresponding location in the file system is occupied by a directory (as opposed
   *           to a file).
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The team provider for the project which contains this folder does not permit linked
   *           resources.
   *       <li>This folder's project contains a nature which does not permit linked resources.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResource#isLinked()
   * @see IResource#ALLOW_MISSING_LOCAL
   * @see IResource#REPLACE
   * @see IResource#HIDDEN
   * @since 3.2
   */
  public void createLink(URI location, int updateFlags, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Deletes this file from the workspace.
   *
   * <p>This is a convenience method, fully equivalent to:
   *
   * <pre>
   *   delete((keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
   * </pre>
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this folder has been removed from its parent.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param force a flag controlling whether resources that are not in sync with the local file
   *     system will be tolerated
   * @param keepHistory a flag controlling whether files under this folder should be stored in the
   *     workspace's local history
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource could not be deleted for some reason.
   *       <li>This resource is out of sync with the local file system and <code>force</code> is
   *           <code>false</code>.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResource#delete(int,IProgressMonitor)
   * @see IResourceRuleFactory#deleteRule(IResource)
   */
  public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Returns the name of a charset to be used when decoding the contents of this file into
   * characters.
   *
   * <p>This refinement of the corresponding {@link IEncodedStorage} method is a convenience method,
   * fully equivalent to:
   *
   * <pre>
   *   getCharset(true);
   * </pre>
   *
   * <p><b>Note 1</b>: this method does not check whether the result is a supported charset name.
   * Callers should be prepared to handle <code>UnsupportedEncodingException</code> where this
   * charset is used.
   *
   * <p><b>Note 2</b>: this method returns a cached value for the encoding that may be out of date
   * if the file is not synchronized with the local file system and the encoding has since changed
   * in the file system.
   *
   * @return the name of a charset
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource could not be read.
   *       <li>This resource is not local.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *     </ul>
   *
   * @see IFile#getCharset(boolean)
   * @see IEncodedStorage#getCharset()
   * @see IContainer#getDefaultCharset()
   * @since 3.0
   */
  public String getCharset() throws CoreException;

  /**
   * Returns the name of a charset to be used when decoding the contents of this file into
   * characters.
   *
   * <p>If checkImplicit is <code>false</code>, this method will return the charset defined by
   * calling <code>setCharset</code>, provided this file exists, or <code>null</code> otherwise.
   *
   * <p>If checkImplicit is <code>true</code>, this method uses the following algorithm to determine
   * the charset to be returned:
   *
   * <ol>
   *   <li>the charset defined by calling #setCharset, if any, and this file exists, or
   *   <li>the charset automatically discovered based on this file's contents, if one can be
   *       determined, or
   *   <li>the default encoding for this file's parent (as defined by <code>
   *       IContainer#getDefaultCharset</code>).
   * </ol>
   *
   * <p><b>Note 1</b>: this method does not check whether the result is a supported charset name.
   * Callers should be prepared to handle <code>UnsupportedEncodingException</code> where this
   * charset is used.
   *
   * <p><b>Note 2</b>: this method returns a cached value for the encoding that may be out of date
   * if the file is not synchronized with the local file system and the encoding has since changed
   * in the file system.
   *
   * @return the name of a charset, or <code>null</code>
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource could not be read.
   *       <li>This resource is not local.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *     </ul>
   *
   * @see IEncodedStorage#getCharset()
   * @see IContainer#getDefaultCharset()
   * @since 3.0
   */
  public String getCharset(boolean checkImplicit) throws CoreException;

  /**
   * Returns the name of a charset to be used to encode the given contents when saving to this file.
   * This file does not have to exist. The character stream is <em>not</em> automatically closed.
   *
   * <p>This method uses the following algorithm to determine the charset to be returned:
   *
   * <ol>
   *   <li>if this file exists, the charset returned by IFile#getCharset(false), if one is defined,
   *       or
   *   <li>the charset automatically discovered based on the file name and the given contents, if
   *       one can be determined, or
   *   <li>the default encoding for the parent resource (as defined by <code>
   *       IContainer#getDefaultCharset</code>).
   * </ol>
   *
   * <p><b>Note</b>: this method does not check whether the result is a supported charset name.
   * Callers should be prepared to handle <code>UnsupportedEncodingException</code> where this
   * charset is used.
   *
   * @param reader a character stream containing the contents to be saved into this file
   * @return the name of a charset
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>The given character stream could not be read.
   *     </ul>
   *
   * @see #getCharset(boolean)
   * @see IContainer#getDefaultCharset()
   * @since 3.1
   */
  public String getCharsetFor(Reader reader) throws CoreException;

  /**
   * Returns a description for this file's current contents. Returns <code>null</code> if a
   * description cannot be obtained.
   *
   * <p>Calling this method produces a similar effect as calling <code>
   * getDescriptionFor(getContents(), getName(), IContentDescription.ALL)</code> on <code>
   * IContentTypeManager</code>, but provides better opportunities for improved performance.
   * Therefore, when manipulating <code>IFile</code>s, clients should call this method instead of
   * <code>IContentTypeManager.getDescriptionFor</code>.
   *
   * @return a description for this file's current contents, or <code>null</code>
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>This resource could not be read.
   *       <li>This resource is not local.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and {@link ResourcesPlugin#PREF_LIGHTWEIGHT_AUTO_REFRESH} is disabled.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *     </ul>
   *
   * @see IContentDescription
   * @see IContentTypeManager#getDescriptionFor(InputStream, String, QualifiedName[])
   * @since 3.0
   */
  public IContentDescription getContentDescription() throws CoreException;

  /**
   * Returns an open input stream on the contents of this file.
   *
   * <p>This refinement of the corresponding {@link IStorage} method is a convenience method
   * returning an open input stream. It's equivalent to:
   *
   * <pre>
   *   getContents(RefreshManager#PREF_LIGHTWEIGHT_AUTO_REFRESH);
   * </pre>
   *
   * <p>If lightweight auto-refresh is not enabled this method will throw a CoreException when
   * opening out-of-sync resources. The client is responsible for closing the stream when finished.
   *
   * @return an input stream containing the contents of the file
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>This resource is not local.
   *       <li>The file-system resource is not a file.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           (and {@link ResourcesPlugin#PREF_LIGHTWEIGHT_AUTO_REFRESH} is disabled).
   *     </ul>
   */
  public InputStream getContents() throws CoreException;

  /**
   * This refinement of the corresponding <code>IStorage</code> method returns an open input stream
   * on the contents of this file. The client is responsible for closing the stream when finished.
   * If force is <code>true</code> the file is opened and an input stream returned regardless of the
   * sync state of the file. The file is not synchronized with the workspace. If force is <code>
   * false</code> the method fails if not in sync.
   *
   * @param force a flag controlling how to deal with resources that are not in sync with the local
   *     file system
   * @return an input stream containing the contents of the file
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>This resource is not local.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and force is <code>false</code>.
   *     </ul>
   */
  public InputStream getContents(boolean force) throws CoreException;

  /**
   * Returns a constant identifying the character encoding of this file, or ENCODING_UNKNOWN if it
   * could not be determined. The returned constant will be one of the ENCODING_* constants defined
   * on IFile.
   *
   * <p>This method attempts to guess the file's character encoding by analyzing the first few bytes
   * of the file. If no identifying pattern is found at the beginning of the file, ENC_UNKNOWN will
   * be returned. This method will not attempt any complex analysis of the file to make a guess at
   * the encoding that is used.
   *
   * @return The character encoding of this file
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>This resource could not be read.
   *       <li>This resource is not local.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *     </ul>
   *
   * @deprecated use IFile#getCharset instead
   */
  @Deprecated
  public int getEncoding() throws CoreException;

  /**
   * Returns the full path of this file. This refinement of the corresponding <code>IStorage</code>
   * and <code>IResource</code> methods links the semantics of resource and storage object paths
   * such that <code>IFile</code>s always have a path and that path is relative to the containing
   * workspace.
   *
   * @see IResource#getFullPath()
   * @see IStorage#getFullPath()
   */
  public IPath getFullPath();

  /**
   * Returns a list of past states of this file known to this workspace. Recently added states
   * first.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @return an array of states of this file
   * @exception CoreException if this method fails.
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   */
  public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException;

  /**
   * Returns the name of this file. This refinement of the corresponding <code>IStorage</code> and
   * <code>IResource</code> methods links the semantics of resource and storage object names such
   * that <code>IFile</code>s always have a name and that name equivalent to the last segment of its
   * full path.
   *
   * @see IResource#getName()
   * @see IStorage#getName()
   */
  public String getName();

  /**
   * Returns whether this file is read-only. This refinement of the corresponding <code>IStorage
   * </code> and <code>IResource</code> methods links the semantics of read-only resources and
   * read-only storage objects.
   *
   * @see IResource#isReadOnly()
   * @see IStorage#isReadOnly()
   */
  public boolean isReadOnly();

  /**
   * Moves this resource to be at the given location.
   *
   * <p>This is a convenience method, fully equivalent to:
   *
   * <pre>
   *   move(destination, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
   * </pre>
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file has been removed from its parent and a new
   * file has been added to the parent of the destination.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param destination the destination path
   * @param force a flag controlling whether resources that are not in sync with the local file
   *     system will be tolerated
   * @param keepHistory a flag controlling whether files under this folder should be stored in the
   *     workspace's local history
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this resource could not be moved. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>This resource is not local.
   *       <li>The resource corresponding to the parent destination path does not exist.
   *       <li>The resource corresponding to the parent destination path is a closed project.
   *       <li>A resource at destination path does exist.
   *       <li>A resource of a different type exists at the destination path.
   *       <li>This resource is out of sync with the local file system and <code>force</code> is
   *           <code>false</code>.
   *       <li>The workspace and the local file system are out of sync at the destination resource
   *           or one of its descendents.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResource#move(IPath,int,IProgressMonitor)
   * @see IResourceRuleFactory#moveRule(IResource, IResource)
   */
  public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Sets the charset for this file. Passing a value of <code>null</code> will remove the charset
   * setting for this resource.
   *
   * @param newCharset a charset name, or <code>null</code>
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>An error happened while persisting this setting.
   *     </ul>
   *
   * @see #getCharset()
   * @since 3.0
   * @deprecated Replaced by {@link #setCharset(String, IProgressMonitor)} which is a workspace
   *     operation and reports changes in resource deltas.
   */
  @Deprecated
  public void setCharset(String newCharset) throws CoreException;

  /**
   * Sets the charset for this file. Passing a value of <code>null</code> will remove the charset
   * setting for this resource.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's encoding has changed.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param newCharset a charset name, or <code>null</code>
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>An error happened while persisting this setting.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See {@link IResourceChangeEvent} for more details.
   *     </ul>
   *
   * @see #getCharset()
   * @see IResourceRuleFactory#charsetRule(IResource)
   * @since 3.0
   */
  public void setCharset(String newCharset, IProgressMonitor monitor) throws CoreException;

  /**
   * Sets the contents of this file to the bytes in the given input stream.
   *
   * <p>This is a convenience method, fully equivalent to:
   *
   * <pre>
   *   setContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
   * </pre>
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's contents have been changed.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param source an input stream containing the new contents of the file
   * @param force a flag controlling how to deal with resources that are not in sync with the local
   *     file system
   * @param keepHistory a flag indicating whether or not store the current contents in the local
   *     history
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and <code>force </code> is <code>false</code>.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The file modification validator disallowed the change.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see #setContents(java.io.InputStream,int,IProgressMonitor)
   */
  public void setContents(
      InputStream source, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Sets the contents of this file to the bytes in the given file state.
   *
   * <p>This is a convenience method, fully equivalent to:
   *
   * <pre>
   *   setContents(source, (keepHistory ? KEEP_HISTORY : IResource.NONE) | (force ? FORCE : IResource.NONE), monitor);
   * </pre>
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's content have been changed.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param source a previous state of this resource
   * @param force a flag controlling how to deal with resources that are not in sync with the local
   *     file system
   * @param keepHistory a flag indicating whether or not store the current contents in the local
   *     history
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>The state does not exist.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and <code>force </code> is <code>false</code>.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The file modification validator disallowed the change.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see #setContents(IFileState,int,IProgressMonitor)
   */
  public void setContents(
      IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Sets the contents of this file to the bytes in the given input stream. The stream will get
   * closed whether this method succeeds or fails. If the stream is <code>null</code> then the
   * content is set to be the empty sequence of bytes.
   *
   * <p>The <code>FORCE</code> update flag controls how this method deals with cases where the
   * workspace is not completely in sync with the local file system. If <code>FORCE</code> is not
   * specified, the method will only attempt to overwrite a corresponding file in the local file
   * system provided it is in sync with the workspace. This option ensures there is no unintended
   * data loss; it is the recommended setting. However, if <code>FORCE</code> is specified, an
   * attempt will be made to write a corresponding file in the local file system, overwriting any
   * existing one if need be. In either case, if this method succeeds, the resource will be marked
   * as being local (even if it wasn't before).
   *
   * <p>The <code>KEEP_HISTORY</code> update flag controls whether or not a copy of current contents
   * of this file should be captured in the workspace's local history (properties are not recorded
   * in the local history). The local history mechanism serves as a safety net to help the user
   * recover from mistakes that might otherwise result in data loss. Specifying <code>KEEP_HISTORY
   * </code> is recommended except in circumstances where past states of the files are of no
   * conceivable interest to the user. Note that local history is maintained with each individual
   * project, and gets discarded when a project is deleted from the workspace. This flag is ignored
   * if the file was not previously local.
   *
   * <p>Update flags other than <code>FORCE</code> and <code>KEEP_HISTORY</code> are ignored.
   *
   * <p>Prior to modifying the contents of this file, the file modification validator (if provided
   * by the VCM plug-in), will be given a chance to perform any last minute preparations. Validation
   * is performed by calling <code>IFileModificationValidator.validateSave</code> on this file. If
   * the validation fails, then this operation will fail.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's content have been changed.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param source an input stream containing the new contents of the file
   * @param updateFlags bit-wise or of update flag constants (<code>FORCE</code> and <code>
   *     KEEP_HISTORY</code>)
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and <code>FORCE</code> is not specified.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The file modification validator disallowed the change.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResourceRuleFactory#modifyRule(IResource)
   * @since 2.0
   */
  public void setContents(InputStream source, int updateFlags, IProgressMonitor monitor)
      throws CoreException;

  /**
   * Sets the contents of this file to the bytes in the given file state.
   *
   * <p>The <code>FORCE</code> update flag controls how this method deals with cases where the
   * workspace is not completely in sync with the local file system. If <code>FORCE</code> is not
   * specified, the method will only attempt to overwrite a corresponding file in the local file
   * system provided it is in sync with the workspace. This option ensures there is no unintended
   * data loss; it is the recommended setting. However, if <code>FORCE</code> is specified, an
   * attempt will be made to write a corresponding file in the local file system, overwriting any
   * existing one if need be. In either case, if this method succeeds, the resource will be marked
   * as being local (even if it wasn't before).
   *
   * <p>The <code>KEEP_HISTORY</code> update flag controls whether or not a copy of current contents
   * of this file should be captured in the workspace's local history (properties are not recorded
   * in the local history). The local history mechanism serves as a safety net to help the user
   * recover from mistakes that might otherwise result in data loss. Specifying <code>KEEP_HISTORY
   * </code> is recommended except in circumstances where past states of the files are of no
   * conceivable interest to the user. Note that local history is maintained with each individual
   * project, and gets discarded when a project is deleted from the workspace. This flag is ignored
   * if the file was not previously local.
   *
   * <p>Update flags other than <code>FORCE</code> and <code>KEEP_HISTORY</code> are ignored.
   *
   * <p>Prior to modifying the contents of this file, the file modification validator (if provided
   * by the VCM plug-in), will be given a chance to perform any last minute preparations. Validation
   * is performed by calling <code>IFileModificationValidator.validateSave</code> on this file. If
   * the validation fails, then this operation will fail.
   *
   * <p>This method changes resources; these changes will be reported in a subsequent resource
   * change event, including an indication that this file's content have been changed.
   *
   * <p>This method is long-running; progress and cancellation are provided by the given progress
   * monitor.
   *
   * @param source a previous state of this resource
   * @param updateFlags bit-wise or of update flag constants (<code>FORCE</code> and <code>
   *     KEEP_HISTORY</code>)
   * @param monitor a progress monitor, or <code>null</code> if progress reporting is not desired
   * @exception CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>This resource does not exist.
   *       <li>The state does not exist.
   *       <li>The corresponding location in the local file system is occupied by a directory.
   *       <li>The workspace is not in sync with the corresponding location in the local file system
   *           and <code>FORCE</code> is not specified.
   *       <li>Resource changes are disallowed during certain types of resource change event
   *           notification. See <code>IResourceChangeEvent</code> for more details.
   *       <li>The file modification validator disallowed the change.
   *     </ul>
   *
   * @exception OperationCanceledException if the operation is canceled. Cancelation can occur even
   *     if no progress monitor is provided.
   * @see IResourceRuleFactory#modifyRule(IResource)
   * @since 2.0
   */
  public void setContents(IFileState source, int updateFlags, IProgressMonitor monitor)
      throws CoreException;
}
