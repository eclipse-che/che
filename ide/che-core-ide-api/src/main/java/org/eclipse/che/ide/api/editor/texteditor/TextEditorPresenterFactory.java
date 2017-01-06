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
package org.eclipse.che.ide.api.editor.texteditor;

/**
 * Factory for {@link TextEditorPresenter} objects.
 *
 * @param <T>
 *         the type of the editor
 */
@Deprecated
public interface TextEditorPresenterFactory<T extends EditorWidget> {

    /**
     * Create an instance of {@link TextEditorPresenter}.
     *
     * @param editorWidgetFactory
     *         the {@link EditorWidget} factory tu use
     * @return a new {@link TextEditorPresenter}
     */
    TextEditorPresenter<T> createTextEditor(EditorWidgetFactory<T> editorWidgetFactory);
}
