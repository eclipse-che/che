/**
 * ***************************************************************************** Copyright (c) 2008
 * Symbian Software Systems, IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Andrew Ferguson (Symbian) - Initial implementation - [api] enable document setup
 * participants to customize behaviour based on resource being opened -
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=208881
 * *****************************************************************************
 */
package org.eclipse.core.filebuffers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

/**
 * Extension interface for {@link org.eclipse.core.filebuffers.IDocumentSetupParticipant}.
 *
 * <p>This interface is additionally implemented by {@link IDocumentSetupParticipant}'s which would
 * like to alter their behavior based on the location of the file being opened.
 *
 * <p>Note that when participants implement this interface, the original method from {@link
 * IDocumentSetupParticipant} will never be called.
 *
 * @see org.eclipse.core.filebuffers.IDocumentSetupParticipant
 * @since 3.4
 */
public interface IDocumentSetupParticipantExtension {

  /**
   * Sets up the document to be ready for use by a text file buffer.
   *
   * @param document the document to be set up
   * @param location a path of the resource backing the new document
   * @param locationKind the kind of the given location
   */
  void setup(IDocument document, IPath location, LocationKind locationKind);
}
