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
package org.eclipse.che.ide.editor.synchronization;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.document.DocumentStorage;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangeHandler;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.FileContentUpdateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

public class EditorGroupSynchronizationImpl implements EditorGroupSynchronization, DocumentChangeHandler, FileContentUpdateHandler {
    private final DocumentStorage     documentStorage;
    private final NotificationManager notificationManager;
    private final HandlerRegistration fileContentUpdateHandlerRegistration;
    private final Map<EditorPartPresenter, HandlerRegistration> synchronizedEditors = new HashMap<>();

    private EditorPartPresenter groupLeaderEditor;

    @Inject
    EditorGroupSynchronizationImpl(EventBus eventBus,
                                   DocumentStorage documentStorage,
                                   NotificationManager notificationManager) {
        this.documentStorage = documentStorage;
        this.notificationManager = notificationManager;
        fileContentUpdateHandlerRegistration = eventBus.addHandler(FileContentUpdateEvent.TYPE, this);
    }

    @Override
    public void addEditor(EditorPartPresenter editor) {
        DocumentHandle documentHandle = getDocumentHandleFor(editor);
        if (documentHandle != null) {
            HandlerRegistration handlerRegistration = documentHandle.getDocEventBus().addHandler(DocumentChangeEvent.TYPE, this);
            synchronizedEditors.put(editor, handlerRegistration);
        }
    }

    @Override
    public void onActiveEditorChanged(@NotNull EditorPartPresenter activeEditor) {
        groupLeaderEditor = activeEditor;
        resolveAutoSave();
    }

    @Override
    public void removeEditor(EditorPartPresenter editor) {
        editor.doSave();
        HandlerRegistration handlerRegistration = synchronizedEditors.remove(editor);
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }

        if (groupLeaderEditor == editor) {
            groupLeaderEditor = null;
        }
    }

    @Override
    public void unInstall() {
        for (HandlerRegistration handlerRegistration : synchronizedEditors.values()) {
            handlerRegistration.removeHandler();
        }

        if (fileContentUpdateHandlerRegistration != null) {
            fileContentUpdateHandlerRegistration.removeHandler();
        }
        groupLeaderEditor = null;
    }

    @Override
    public Set<EditorPartPresenter> getSynchronizedEditors() {
        return synchronizedEditors.keySet();
    }

    @Override
    public void onDocumentChange(DocumentChangeEvent event) {
        DocumentHandle activeEditorDocumentHandle = getDocumentHandleFor(groupLeaderEditor);
        if (activeEditorDocumentHandle == null || !event.getDocument().isSameAs(activeEditorDocumentHandle)) {
            return;
        }

        for (EditorPartPresenter editor : synchronizedEditors.keySet()) {
            if (editor == groupLeaderEditor) {
                continue;
            }

            DocumentHandle documentHandle = getDocumentHandleFor(editor);
            if (documentHandle != null) {
                documentHandle.getDocument().replace(event.getOffset(), event.getRemoveCharCount(), event.getText());
            }
        }
    }

    @Override
    public void onFileContentUpdate(final FileContentUpdateEvent event) {
        if (synchronizedEditors.keySet().isEmpty()) {
            return;
        }

        if (groupLeaderEditor == null) {
            groupLeaderEditor = synchronizedEditors.keySet().iterator().next();
            resolveAutoSave();
        }

        final VirtualFile virtualFile = groupLeaderEditor.getEditorInput().getFile();
        if (!event.getFilePath().equals(virtualFile.getLocation().toString())) {
            return;
        }

        if  (!(virtualFile instanceof File)) {
            return;
        }

        final File file = (File)virtualFile;

        documentStorage.getDocument(virtualFile, new DocumentStorage.DocumentCallback() {

            @Override
            public void onDocumentReceived(final String content) {
                updateContent(content, event.getModificationStamp(), file);
            }

            @Override
            public void onDocumentLoadFailure(final Throwable caught) {
                notificationManager.notify("", "Can not to update content for the file " + virtualFile.getDisplayName(), FAIL, EMERGE_MODE);
            }
        });
    }

    private void updateContent(String newContent, String modificationStamp, File file) {
        DocumentHandle documentHandle = getDocumentHandleFor(groupLeaderEditor);
        if (documentHandle == null) {
            return;
        }

        String oldContent = documentHandle.getDocument().getContents();
        TextPosition cursorPosition = documentHandle.getDocument().getCursorPosition();
        if (!newContent.equals(oldContent)) {
            documentHandle.getDocument().replace(0, oldContent.length(), newContent);
            documentHandle.getDocument().setCursorPosition(cursorPosition);

            if (notificationManager != null && modificationStamp != null && !modificationStamp.equals(file.getModificationStamp())) {
                notificationManager.notify("External operation", "File '" + file.getName() + "' is updated", SUCCESS, EMERGE_MODE);
            }
        }
    }

    @Nullable
    private DocumentHandle getDocumentHandleFor(EditorPartPresenter editor) {
        if (editor == null || !(editor instanceof TextEditor)) {
            return null;
        }
        return ((TextEditor)editor).getDocument().getDocumentHandle();
    }

    private void resolveAutoSave() {
        for (EditorPartPresenter editor : synchronizedEditors.keySet()) {
            resolveAutoSaveFor(editor);
        }
    }

    private void resolveAutoSaveFor(EditorPartPresenter editor) {
        if (!(editor instanceof EditorWithAutoSave)) {
            return;
        }

        EditorWithAutoSave editorWithAutoSave = (EditorWithAutoSave)editor;
        if (editorWithAutoSave == groupLeaderEditor) {
            editorWithAutoSave.enableAutoSave();
            return;
        }

        if (editorWithAutoSave.isAutoSaveEnabled()) {
            editorWithAutoSave.disableAutoSave();
        }
    }
}
