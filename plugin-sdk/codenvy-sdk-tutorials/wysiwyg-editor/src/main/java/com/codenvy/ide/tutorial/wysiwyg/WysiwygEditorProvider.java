/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.tutorial.wysiwyg;

import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.EditorProvider;
import com.codenvy.ide.tutorial.wysiwyg.editor.WysiwygEditor;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

/** @author Evgen Vidolob */
public class WysiwygEditorProvider implements EditorProvider {
    private final ProjectServiceClient projectServiceClient;
    private final DialogFactory        dialogFactory;

    public WysiwygEditorProvider(ProjectServiceClient projectServiceClient, DialogFactory dialogFactory) {
        this.projectServiceClient = projectServiceClient;
        this.dialogFactory = dialogFactory;
    }

    @Override
    public String getId() {
        return "demoWYSIWYGEditor";
    }

    @Override
    public String getDescription() {
        return "Demo WYSIWYG Editor";
    }

    /** {@inheritDoc} */
    @Override
    public EditorPartPresenter getEditor() {
        return new WysiwygEditor(projectServiceClient, dialogFactory);
    }
}
