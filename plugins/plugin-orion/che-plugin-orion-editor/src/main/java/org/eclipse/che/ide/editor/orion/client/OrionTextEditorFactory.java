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
package org.eclipse.che.ide.editor.orion.client;

import javax.inject.Inject;

import org.eclipse.che.ide.api.editor.texteditor.EditorWidget;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidgetFactory;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenterFactory;

/** Editor presenter factory that produces orion-based editors. */
public class OrionTextEditorFactory {

    /** The {@link EditorWidget} factory. */
    @Inject
    private EditorWidgetFactory<OrionEditorWidget> editorWidgetFactory;

    /** The base {@link TextEditorPresenter} factory. */
    @Inject
    private TextEditorPresenterFactory<OrionEditorWidget> presenterFactory;

    public TextEditorPresenter<OrionEditorWidget> createTextEditor() {
        return this.presenterFactory.createTextEditor(this.editorWidgetFactory);
    }
}
