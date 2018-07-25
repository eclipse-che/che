/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.reconciler;

import org.eclipse.che.ide.api.editor.document.UseDocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangedHandler;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

/**
 * An <code>Reconciler</code> defines and maintains a model of the content of the text view document
 * in the presence of changes applied to this document. Reconciler have a list of {@link
 * ReconcilingStrategy} objects each of which is registered for a particular document content type.
 * The reconciler uses the strategy objects to react on the changes applied to the text view
 * document.
 *
 * @author Evgen Vidolob
 */
public interface Reconciler extends UseDocumentHandle, DocumentChangedHandler {

  /**
   * Installs the reconciler on the given text view. After this method has been finished, the
   * reconciler is operational, i.e., it works without requesting further client actions until
   * <code>uninstall</code> is called.
   */
  void install(TextEditor editor);

  /** Removes the reconciler from the text view it has previously been installed on. */
  void uninstall();

  /**
   * Returns the reconciling strategy registered with the reconciler for the specified content type.
   *
   * @param contentType the content type for which to determine the reconciling strategy
   * @return the reconciling strategy registered for the given content type, or <code>null</code> if
   *     there is no such strategy
   */
  ReconcilingStrategy getReconcilingStrategy(String contentType);

  /**
   * Returns the partitioning this reconciler is using.
   *
   * @return the partitioning this reconciler is using
   */
  String getDocumentPartitioning();

  void addReconcilingStrategy(String contentType, ReconcilingStrategy strategy);
}
