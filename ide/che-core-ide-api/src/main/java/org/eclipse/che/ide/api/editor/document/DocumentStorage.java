/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.document;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.resources.VirtualFile;

/** Interface for file retrieval and storage operations. */
@ImplementedBy(DocumentStorageImpl.class)
public interface DocumentStorage {

  /**
   * Retrieves the file content.
   *
   * @param file the file
   * @param callback operation to do when the content is ready
   */
  void getDocument(@NotNull VirtualFile file, @NotNull final DocumentCallback callback);

  /**
   * Retrieves the file content.
   *
   * @param file the file
   * @return the promise which should return content
   */
  @NotNull
  Promise<String> getDocument(@NotNull VirtualFile file);

  /**
   * Saves the file content.
   *
   * @param editorInput the editor input
   * @param document the document
   * @param overwrite
   * @param callback operation to do when the content is ready
   */
  void saveDocument(
      @Nullable final EditorInput editorInput,
      @NotNull Document document,
      boolean overwrite,
      @NotNull final AsyncCallback<EditorInput> callback);

  /**
   * Action taken when the document is closed.
   *
   * @param document the document
   */
  void documentClosed(@NotNull Document document);

  /** Action taken when retrieve action is successful. */
  interface DocumentCallback {
    /**
     * Action taken when retrieve action is successful.
     *
     * @param content the content that was received
     */
    void onDocumentReceived(String content);

    /**
     * Action taken when retrieve action fails.
     *
     * @param caught the exception
     */
    void onDocumentLoadFailure(Throwable caught);
  }
}
