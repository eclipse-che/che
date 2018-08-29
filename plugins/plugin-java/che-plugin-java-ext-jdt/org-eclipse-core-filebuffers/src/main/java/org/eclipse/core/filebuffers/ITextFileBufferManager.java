/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2009 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

/**
 * A text file buffer manager manages text file buffers for files whose contents is considered text.
 *
 * <p>Clients are not supposed to implement that interface.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITextFileBufferManager extends IFileBufferManager {

  /**
   * The default text file buffer manager.
   *
   * @since 3.3
   */
  ITextFileBufferManager DEFAULT = FileBuffersPlugin.getDefault().getFileBufferManager();

  //	/**
  //	 * Returns the text file buffer managed for the file at the given location
  //	 * or <code>null</code> if either there is no such text file buffer.
  //	 * <p>
  //	 * The provided location is either a full path of a workspace resource or
  //	 * an absolute path in the local file system. The file buffer manager does
  //	 * not resolve the location of workspace resources in the case of linked
  //	 * resources.
  //	 * </p>
  //	 *
  //	 * @param location the location
  //	 * @return the text file buffer managed for that location or <code>null</code>
  //	 * @deprecated As of 3.3, replaced by {@link #getTextFileBuffer(IPath, LocationKind)}
  //	 */
  //	ITextFileBuffer getTextFileBuffer(IPath location);

  /**
   * Returns the text file buffer managed for the file at the given location or <code>null</code> if
   * there is no such text file buffer.
   *
   * <p>The type of the provided location is specified by the given <code>locationKind</code>.
   *
   * @param location the location
   * @param locationKind the kind of the given location
   * @return the text file buffer managed for that location or <code>null</code>
   * @see LocationKind
   * @since 3.3
   */
  ITextFileBuffer getTextFileBuffer(IPath location, LocationKind locationKind);

  //	/**
  //	 * Returns the text file buffer managed for the given file store
  //	 * or <code>null</code> if there is no such text file buffer.
  //	 * <p>
  //	 * <strong>Note:</strong> This API must not be used if the given file
  //	 * store maps to a resource contained in the workspace. A file buffer
  //	 * that has been connected using a path will not be found.
  //	 * </p>
  //	 * <p>
  //	 * We had to use a different name than <code>getTextFileBuffer</code> for this method
  //	 * due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=148844
  //	 * </p>
  //	 *
  //	 * @param fileStore the file store
  //	 * @return the text file buffer managed for that file store or <code>null</code>
  //	 * @since 3.3
  //	 */
  //	ITextFileBuffer getFileStoreTextFileBuffer(IFileStore fileStore);

  /**
   * Returns the text file buffer managed for the given document or <code>null</code> if there is no
   * such text file buffer.
   *
   * <p><strong>Note:</strong> This method goes through the list of registered buffers and tests
   * whether its document matches the given one. Therefore this method should not be used in
   * performance critical code.
   *
   * @param document the document for which to find the text file buffer
   * @return the text file buffer managed for that document or <code>null</code>
   * @since 3.3
   */
  ITextFileBuffer getTextFileBuffer(IDocument document);

  /**
   * Returns the default encoding that is used to read the contents of text files if no other
   * encoding is specified.
   *
   * @return the default text file encoding
   */
  String getDefaultEncoding();

  /**
   * Creates a new empty document. The document is set up in the same way as it would be used in a
   * text file buffer for a file at the given location.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location used to set up the newly created document or <code>null</code> if
   *     unknown
   * @return a new empty document
   * @deprecated As of 3.3, replaced by {@link #createEmptyDocument(IPath, LocationKind)}
   */
  IDocument createEmptyDocument(IPath location);

  /**
   * Creates a new empty document. The document is set up in the same way as it would be used in a
   * text file buffer for a file at the given location.
   *
   * <p>The type of the provided location is specified by the given <code>locationKind</code>.
   *
   * @param location the location used to set up the newly created document or <code>null</code> if
   *     unknown
   * @param locationKind the kind of the given location
   * @return a new empty document
   * @since 3.3
   */
  IDocument createEmptyDocument(IPath location, LocationKind locationKind);

  //	/**
  //	 * Creates a new annotation for the given location.
  //	 * <p>
  //	 * The provided location is either a full path of a workspace resource or an
  //	 * absolute path in the local file system. The file buffer manager does not
  //	 * resolve the location of workspace resources in the case of linked
  //	 * resources.
  //	 * </p>
  //	 *
  //	 * @param location the location used to create the new annotation model
  //	 * @return the newly created annotation model
  //	 * @deprecated As of 3.3, replaced by {@link #createAnnotationModel(IPath, LocationKind)}
  //	 */
  //	IAnnotationModel createAnnotationModel(IPath location);
  //
  //	/**
  //	 * Creates a new annotation for the given location.
  //	 * <p>
  //	 * The type of the provided location is specified by the given
  //	 * <code>locationKind</code>.
  //	 * </p>
  //	 *
  //	 * @param location the location used to create the new annotation model
  //	 * @param locationKind the kind of the given location
  //	 * @return the newly created annotation model
  //	 * @since 3.3
  //	 */
  //	IAnnotationModel createAnnotationModel(IPath location, LocationKind locationKind);

  //	/**
  //	 * Returns whether a file at the given location is or can be considered a
  //	 * text file. If the file exists, the concrete content type of the file is
  //	 * checked. If the concrete content type for the existing file can not be
  //	 * determined, this method returns <code>true</code>. If the file does
  //	 * not exist, it is checked whether a text content type is associated with
  //	 * the given location. If no content type is associated with the location,
  //	 * this method returns <code>true</code>.
  //	 * <p>
  //	 * The provided location is either a full path of a workspace resource or an
  //	 * absolute path in the local file system. The file buffer manager does not
  //	 * resolve the location of workspace resources in the case of linked
  //	 * resources.
  //	 * </p>
  //	 *
  //	 * @param location the location to check
  //	 * @return <code>true</code> if the location is a text file location
  //	 * @since 3.1
  //	 * @deprecated As of 3.2, replaced by {@link #isTextFileLocation(IPath, boolean)}
  //	 */
  //	boolean isTextFileLocation(IPath location);
  /**
   * Returns whether a file at the given location is or can be considered a text file. If the file
   * exists, the concrete content type of the file is checked. If the concrete content type for the
   * existing file can not be determined, this method returns <code>!strict</code>. If the file does
   * not exist, it is checked whether a text content type is associated with the given location. If
   * no content type is associated with the location, this method returns <code>!strict</code>.
   *
   * <p>The provided location is either a full path of a workspace resource or an absolute path in
   * the local file system. The file buffer manager does not resolve the location of workspace
   * resources in the case of linked resources.
   *
   * @param location the location to check
   * @param strict <code>true</code> if a file with unknown content type is not treated as text
   *     file, <code>false</code> otherwise
   * @return <code>true</code> if the location is a text file location
   * @since 3.2
   */
  boolean isTextFileLocation(IPath location, boolean strict);
}
