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

/**
 * Interface for components that use a document handle.<br>
 * note: this is for component that use, not produce handles.
 */
public interface UseDocumentHandle {

  /**
   * Set the document handle.
   *
   * @param handle the handle
   */
  void setDocumentHandle(DocumentHandle handle);

  /**
   * Returns the document handle.
   *
   * @return the handle
   */
  DocumentHandle getDocumentHandle();
}
