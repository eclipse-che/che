/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.ui.wizards.datatransfer;

import java.io.InputStream;
import java.util.List;

/**
 * Interface which can provide structure and content information for an element (for example, a file
 * system element). Used by the import wizards to abstract the commonalities between importing from
 * the file system and importing from an archive.
 */
public interface IImportStructureProvider {
  /**
   * Returns a collection with the children of the specified structured element.
   *
   * @param element the element for which to compute the children
   * @return the list of child elements
   */
  List getChildren(Object element);

  /**
   * Returns the contents of the specified structured element, or <code>null</code> if there is a
   * problem determining the element's contents.
   *
   * <p><strong>Note:</strong>: The client is responsible for closing the stream when finished.
   *
   * @param element a structured element
   * @return the contents of the structured element, or <code>null</code>
   */
  InputStream getContents(Object element);

  /**
   * Returns the full path of the specified structured element.
   *
   * @param element a structured element
   * @return the display label of the structured element
   */
  String getFullPath(Object element);

  /**
   * Returns the display label of the specified structured element.
   *
   * @param element a structured element
   * @return the display label of the structured element
   */
  String getLabel(Object element);

  /**
   * Returns a boolean indicating whether the passed structured element represents a container
   * element (as opposed to a leaf element).
   *
   * @return boolean
   * @param element java.lang.Object
   */
  boolean isFolder(Object element);
}
