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
package org.eclipse.che.ide.editor.macro;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * Provider which is responsible for retrieving the project type of the file from the opened editor.
 *
 * Macro provided: <code>${editor.current.project.type}</code>
 *
 * @see AbstractEditorMacroProvider
 * @see EditorAgent
 * @since 4.7.0
 */
@Beta
@Singleton
public class EditorCurrentProjectTypeProvider extends AbstractEditorMacroProvider {

    public static final String KEY = "${editor.current.project.type}";

    private PromiseProvider promises;

    @Inject
    public EditorCurrentProjectTypeProvider(EditorAgent editorAgent,
                                            PromiseProvider promises) {
        super(editorAgent);
        this.promises = promises;
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public Promise<String> getValue() {
        final EditorPartPresenter editor = getActiveEditor();

        if (editor == null) {
            return promises.resolve("");
        }

        final VirtualFile file = editor.getEditorInput().getFile();

        if (file instanceof Resource) {
            final Optional<Project> project = ((Resource)file).getRelatedProject();

            if (!project.isPresent()) {
                return promises.resolve("");
            }

            return promises.resolve(project.get().getType());
        }

        return promises.resolve("");
    }
}
