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
package org.eclipse.che.ide.jseditor.client.defaulteditor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.jseditor.client.JsEditorExtension;

import javax.validation.constraints.NotNull;
import javax.inject.Named;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class EditorWithoutAutoSaveProvider implements EditorProvider {

    private EditorBuilder embeddedBuilder;


    @Inject
    public EditorWithoutAutoSaveProvider(@Named(JsEditorExtension.EMBEDDED_EDITOR_BUILDER) EditorBuilder embeddedBuilder) {
        this.embeddedBuilder = embeddedBuilder;
    }

    @Override
    public String getId() {
        return "EditorWithoutAutoSaveProvider";
    }

    @Override
    public String getDescription() {
        return "Editor without auto save";
    }

    @NotNull
    @Override
    public EditorPartPresenter getEditor() {
        return embeddedBuilder.buildEditor();
    }
}
