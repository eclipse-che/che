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
package org.eclipse.che.ide.jseditor.client.texteditor;

/**
 * Factory for {@link EmbeddedTextEditorPresenter} objects.
 * @param <T> the type of the editor
 */
public interface EmbeddedTextEditorPresenterFactory<T extends EditorWidget> {
    /**
     * Create an instance of {@link EmbeddedTextEditorPresenter}.
     * @param editorWidgetFactory the {@link EditorWidget} factory tu use
     * @return a new {@link EmbeddedTextEditorPresenter}
     */
    EmbeddedTextEditorPresenter<T> createTextEditor(EditorWidgetFactory<T> editorWidgetFactory);
}
