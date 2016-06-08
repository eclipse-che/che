/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.document;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;

/**
 * Implementation of {@link DocumentStorage}.
 */
public class DocumentStorageImpl implements DocumentStorage {

    private final EventBus eventBus;

    @Inject
    public DocumentStorageImpl(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void getDocument(@NotNull final VirtualFile file, @NotNull final DocumentCallback callback) {
        file.getContent().then(new Operation<String>() {
            @Override
            public void apply(String result) throws OperationException {
                Log.debug(DocumentStorageImpl.class, "Document retrieved (" + file.getPath() + ").");
                try {
                    callback.onDocumentReceived(result);
                } catch (final Exception e) {
                    Log.warn(DocumentStorageImpl.class, "Exception during doc retrieve success callback: ", e);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                try {
                    callback.onDocumentLoadFailure(arg.getCause());
                } catch (final Exception e) {
                    Log.warn(DocumentStorageImpl.class, "Exception during doc retrieve failure callback: ", e);
                }
                Log.error(DocumentStorageImpl.class, "Could not retrieve document (" + file.getPath() + ").", arg.getCause());
            }
        });
    }

    @Override
    public void saveDocument(final EditorInput editorInput, @NotNull final Document document,
                             final boolean overwrite, @NotNull final AsyncCallback<EditorInput> callback) {
        final VirtualFile file = editorInput.getFile();

        file.updateContent(document.getContents()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                Log.debug(DocumentStorageImpl.class, "Document saved (" + file.getPath() + ").");
                DocumentStorageImpl.this.eventBus.fireEvent(new FileEvent(file, FileEvent.FileOperation.SAVE));
                try {
                    callback.onSuccess(editorInput);
                } catch (final Exception e) {
                    Log.warn(DocumentStorageImpl.class, "Exception during save success callback: ", e);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(DocumentStorageImpl.class, "Document save failed (" + file.getPath() + ").", arg.getCause());
                try {
                    callback.onFailure(arg.getCause());
                } catch (final Exception e) {
                    Log.warn(DocumentStorageImpl.class, "Exception during save failure callback: ", e);
                }
            }
        });
    }

    @Override
    public void documentClosed(final Document document) {
    }
}
