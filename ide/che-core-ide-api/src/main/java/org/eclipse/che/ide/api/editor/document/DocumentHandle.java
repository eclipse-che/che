/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.document;

/** Handle on an editor document. */
public interface DocumentHandle {

  /**
   * tells if the handles point to the same document.
   *
   * @param documentHandle the other document handle to compare
   * @return true iff the pointed document is the same.
   */
  boolean isSameAs(DocumentHandle documentHandle);

  /**
   * Returns the private event bus for the pointed editor.
   *
   * @return the private event bus
   */
  DocumentEventBus getDocEventBus();

  /**
   * Returns the pointed document
   *
   * @return the document
   */
  Document getDocument();
}
