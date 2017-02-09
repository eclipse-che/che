/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

/**
 * ContentFormatter implementation
 *
 * @author Roman Nikitenko
 */
public class JavaFormatter implements ContentFormatter {

    private JavaCodeAssistClient service;
    private EditorAgent          editorAgent;

    @Inject
    public JavaFormatter(JavaCodeAssistClient service,
                         EditorAgent editorAgent) {
        this.service = service;
        this.editorAgent = editorAgent;
    }

    @Override
    public void format(final Document document) {
        int offset = document.getSelectedLinearRange().getStartOffset();
        int length = document.getSelectedLinearRange().getLength();

        if (length <= 0 || offset < 0) {
            offset = 0;
            length = document.getContentsCharCount();
        }

        Promise<List<Change>> changesPromise = service.format(offset, length, document.getContents());
        changesPromise.then(new Operation<List<Change>>() {
            @Override
            public void apply(List<Change> changes) throws OperationException {
                applyChanges(changes, document);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(getClass(), arg.getCause());
            }
        });
    }

    @Override
    public void install(TextEditor editor) {
        // does nothing by default
    }

    private void applyChanges(List<Change> changes, Document document) {
        HandlesUndoRedo undoRedo = null;
        EditorPartPresenter editorPartPresenter = editorAgent.getActiveEditor();
        if (editorPartPresenter instanceof UndoableEditor) {
            undoRedo = ((UndoableEditor)editorPartPresenter).getUndoRedo();
        }
        try {
            if (undoRedo != null) {
                undoRedo.beginCompoundChange();
            }
            for (Change change : changes) {
                document.replace(change.getOffset(), change.getLength(), change.getText());
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            if (undoRedo != null) {
                undoRedo.endCompoundChange();
            }
        }
    }
}
