/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * A text file buffer is a file buffer for text files. The contents of a text file buffer is given
 * in the form of a document and an associated annotation model. Also, the text file buffer provides
 * methods to manage the character encoding used to read and write the buffer's underlying text
 * file.
 *
 * <p>Clients are not supposed to implement that interface. Instances of this type are obtained from
 * a {@link org.eclipse.core.filebuffers.ITextFileBufferManager}.
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ITextFileBuffer extends IFileBuffer {

  /**
   * Returns the document of this text file buffer.
   *
   * @return the document of this text file buffer
   */
  IDocument getDocument();

  /**
   * Returns the character encoding to be used for reading and writing the buffer's underlying file.
   *
   * <p><strong>Note:</strong> The encoding used to write the file might differ from the encoding
   * returned by this method if no encoding has been explicitly set and the content type of the file
   * is derived from the content (e.g. an XML file).
   *
   * @return the character encoding
   */
  String getEncoding();

  /**
   * Sets the character encoding to be used for reading and writing the buffer's underlying file.
   *
   * @param encoding the encoding
   */
  void setEncoding(String encoding);

  /**
   * Returns the annotation model of this text file buffer.
   *
   * @return the annotation model of this text file buffer, might be <code>null</code> if called
   *     when disconnected
   */
  IAnnotationModel getAnnotationModel();
}
