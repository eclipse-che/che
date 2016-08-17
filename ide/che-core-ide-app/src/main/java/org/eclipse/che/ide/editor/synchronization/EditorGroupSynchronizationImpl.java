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
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.api.editor.events.DocumentChangeHandler;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EditorGroupSynchronizationImpl implements EditorGroupSynchronization, DocumentChangeHandler {
    private final EditorAgent editorAgent;
    private final Map<EditorPartPresenter, HandlerRegistration> synchronizedEditors = new HashMap<>();

    @Inject
    EditorGroupSynchronizationImpl(EditorAgent editorAgent,
                                   @Assisted List<EditorPartPresenter> editorsToSync) {
        this.editorAgent = editorAgent;
        for (EditorPartPresenter editor : editorsToSync) {
            addEditor(editor);
        }
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
    public void removeEditor(EditorPartPresenter editor) {
        editor.doSave();
        HandlerRegistration handlerRegistration = synchronizedEditors.remove(editor);
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
    }

    @Override
    public void unInstall() {
        for (HandlerRegistration handlerRegistration : synchronizedEditors.values()) {
            handlerRegistration.removeHandler();
        }
    }

    @Override
    public Set<EditorPartPresenter> getSynchronizedEditors() {
        return synchronizedEditors.keySet();
    }

    @Override
    public void onDocumentChange(DocumentChangeEvent event) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        DocumentHandle activeEditorDocumentHandle = getDocumentHandleFor(activeEditor);
        if (activeEditorDocumentHandle == null || !event.getDocument().isSameAs(activeEditorDocumentHandle)) {
            return;
        }

        for (EditorPartPresenter editor : synchronizedEditors.keySet()) {
            if (editor == activeEditor) {
                continue;
            }

            DocumentHandle documentHandle = getDocumentHandleFor(editor);
            if (documentHandle != null) {
                documentHandle.getDocument().replace(event.getOffset(), event.getRemoveCharCount(), event.getText());
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
}
