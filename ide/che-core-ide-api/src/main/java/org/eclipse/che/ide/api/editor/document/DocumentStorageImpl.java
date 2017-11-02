/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

/** Implementation of {@link DocumentStorage}. */
public class DocumentStorageImpl implements DocumentStorage {

  private final EventBus eventBus;

  @Inject
  public DocumentStorageImpl(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void getDocument(
      @NotNull final VirtualFile file, @NotNull final DocumentCallback callback) {
    file.getContent()
        .then(
            new Operation<String>() {
              @Override
              public void apply(String result) throws OperationException {
                Log.debug(
                    DocumentStorageImpl.class, "Document retrieved (" + file.getLocation() + ").");
                try {
                  callback.onDocumentReceived(result);
                } catch (final Exception e) {
                  Log.warn(
                      DocumentStorageImpl.class,
                      "Exception during doc retrieve success callback: ",
                      e);
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                try {
                  callback.onDocumentLoadFailure(arg.getCause());
                } catch (final Exception e) {
                  Log.warn(
                      DocumentStorageImpl.class,
                      "Exception during doc retrieve failure callback: ",
                      e);
                }
                Log.error(
                    DocumentStorageImpl.class,
                    "Could not retrieve document (" + file.getLocation() + ").",
                    arg.getCause());
              }
            });
  }

  @Override
  public Promise<String> getDocument(@NotNull VirtualFile file) {
    return file.getContent();
  }

  @Override
  public void saveDocument(
      final EditorInput editorInput,
      @NotNull final Document document,
      final boolean overwrite,
      @NotNull final AsyncCallback<EditorInput> callback) {
    final VirtualFile file = editorInput.getFile();

    file.updateContent(document.getContents())
        .then(
            new Operation<Void>() {
              @Override
              public void apply(Void arg) throws OperationException {
                Log.debug(
                    DocumentStorageImpl.class, "Document saved (" + file.getLocation() + ").");
                DocumentStorageImpl.this.eventBus.fireEvent(FileEvent.createFileSavedEvent(file));
                try {
                  callback.onSuccess(editorInput);
                } catch (final Exception e) {
                  Log.warn(
                      DocumentStorageImpl.class, "Exception during save success callback: ", e);
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                Log.error(
                    DocumentStorageImpl.class,
                    "Document save failed (" + file.getLocation() + ").",
                    arg.getCause());
                try {
                  callback.onFailure(arg.getCause());
                } catch (final Exception e) {
                  Log.warn(
                      DocumentStorageImpl.class, "Exception during save failure callback: ", e);
                }
              }
            });
  }

  @Override
  public void documentClosed(final Document document) {}
}
